package com.atypon.service;

import com.atypon.config.SpoonacularConfig;
import com.atypon.model.ExcludeRequest;
import com.atypon.model.Ingredient;
import com.atypon.model.Recipe;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpoonacularService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpoonacularService.class);

    private final RestTemplate restTemplate;
    private final SpoonacularConfig config;

    /**
     * @param query: represents the name of the recipe
     * @param cuisine: cuisine type - optional parameter
     * @return list of recipes matching passed parameter
     * **/
    public List<Recipe> searchRecipes(String query, String cuisine) {
        String url = String.format("%s/recipes/complexSearch?query=%s&cuisine=%s&apiKey=%s",
                config.getBaseUrl(), query, cuisine, config.getApiKey());
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
        if(response.getBody()==null){
            LOGGER.error("There are no results found for recipe {}", query);
        }
        List<Recipe> recipes = new ArrayList<>();
        response.getBody().get("results").forEach(node -> {
            Recipe recipe = new Recipe();
            recipe.setId(node.get("id").asInt());
            recipe.setTitle(node.get("title").asText());
            recipes.add(recipe);
        });
        return recipes;
    }

    /**
     * @param recipeId: a unique identifier of the recipe
     * @return a recipe matching passed recipeId
     * **/
    public Recipe getRecipeInfo(int recipeId) {
        String url = String.format("%s/recipes/%d/information?apiKey=%s&includeNutrition=false",
                config.getBaseUrl(), recipeId, config.getApiKey());
        ResponseEntity<Recipe> response = restTemplate.getForEntity(url, Recipe.class);

        if(response.getBody() == null){
            LOGGER.error("Failed to fetch information for recipeId {}", recipeId);
            throw new IllegalStateException("Failed to fetch recipe information");
        }
        return response.getBody();
    }

    /**
     * @param recipeId: a unique identifier of the recipe
     * @param excludeRequest: a list of ingredients to be excluded from calories calculation
     * @return number of calories
     * **/
    public double getCustomizedCalories(int recipeId, ExcludeRequest excludeRequest) {
        Recipe recipe = getRecipeInfo(recipeId);

        if (recipe == null) {
            LOGGER.error("Recipe not found for recipeId {}", recipeId);
            throw new IllegalStateException("Recipe not found for ID: " + recipeId);
        }
        double sum = 0;
        for(Ingredient ingredient : recipe.getExtendedIngredients()){
            final String currentIngredient = ingredient.getName();
            if(excludeRequest.getExcludeIngredients() == null || !excludeRequest.getExcludeIngredients().contains(currentIngredient)){
               sum += ingredient.getAmount();
            }
        }
        return sum;
    }

}

