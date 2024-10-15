package com.atypon.service;

import com.atypon.config.SpoonacularConfig;
import com.atypon.model.ExcludeRequest;
import com.atypon.model.Ingredient;
import com.atypon.model.Recipe;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpoonacularService {

    private final RestTemplate restTemplate;
    private final SpoonacularConfig config;

    public List<Recipe> searchRecipes(String query, String cuisine) {
        String url = String.format("%s/recipes/complexSearch?query=%s&cuisine=%s&apiKey=%s",
                config.getBaseUrl(), query, cuisine, config.getApiKey());
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

        List<Recipe> recipes = new ArrayList<>();
        response.getBody().get("results").forEach(node -> {
            Recipe recipe = new Recipe();
            recipe.setId(node.get("id").asInt());
            recipe.setTitle(node.get("title").asText());
            recipes.add(recipe);
        });
        return recipes;
    }

    public Recipe getRecipeInfo(int recipeId) {
        String url = String.format("%s/recipes/%d/information?apiKey=%s&includeNutrition=false",
                config.getBaseUrl(), recipeId, config.getApiKey());
        ResponseEntity<Recipe> response = restTemplate.getForEntity(url, Recipe.class);

        if(response.getBody() == null){
            throw new IllegalStateException("Failed to fetch recipe information");
        }
        return response.getBody();
    }

    public double getCustomizedCalories(int recipeId, ExcludeRequest excludeRequest) {
        Recipe recipe = getRecipeInfo(recipeId);

        if (recipe == null) {
            throw new IllegalStateException("Recipe not found for ID: " + recipeId);
        }
        double sum = 0;
        for(Ingredient ingredient : recipe.getExtendedIngredients()){
            final String currentIngredient = ingredient.getName();
            if(!excludeRequest.getExcludeIngredients().contains(currentIngredient)){
               sum += ingredient.getAmount();
            }
        }

        /*return recipe.getExtendedIngredients().stream()
                .filter(ingredient -> !excludeRequest.getExcludeIngredients()
                        .contains(ingredient.getName()))
                .mapToDouble(ingredient -> ingredient.getNutrition().getCalories())
                .sum();*/
        return sum;
    }

}

