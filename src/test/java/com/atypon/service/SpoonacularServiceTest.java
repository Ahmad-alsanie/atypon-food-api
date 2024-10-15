package com.atypon.service;

import com.atypon.config.SpoonacularConfig;
import com.atypon.model.ExcludeRequest;
import com.atypon.model.Ingredient;
import com.atypon.model.Nutrition;
import com.atypon.model.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SpoonacularServiceTest {

    @MockBean
    private SpoonacularConfig spoonacularConfig;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private SpoonacularService spoonacularService;

    @BeforeEach
    void setUp() {
        when(spoonacularConfig.getBaseUrl()).thenReturn("https://api.spoonacular.com");
        when(spoonacularConfig.getApiKey()).thenReturn("api-key");
    }

    @Test
    void getRecipeInfo_ShouldReturnRecipe_WhenGivenValidId() {
        int recipeId = 123;
        Recipe mockRecipe = new Recipe();
        mockRecipe.setId(recipeId);
        mockRecipe.setTitle("Test Recipe");

        String url = "https://api.spoonacular.com/recipes/123/information?apiKey=api-key&includeNutrition=false";
        when(restTemplate.getForEntity(url, Recipe.class))
                .thenReturn(new ResponseEntity<>(mockRecipe, HttpStatus.OK));

        Recipe result = spoonacularService.getRecipeInfo(recipeId);

        assertNotNull(result);
        assertEquals(recipeId, result.getId());
        assertEquals("Test Recipe", result.getTitle());
    }

    @Test
    void getCustomizedCalories_ShouldExcludeIngredientsCorrectly() {
        int recipeId = 456;

        Recipe mockRecipe = new Recipe();
        Ingredient cheese = new Ingredient("Cheese", 1, "piece", new Nutrition(40));
        Ingredient pasta = new Ingredient("Pasta", 100, "grams", new Nutrition(140));
        mockRecipe.setExtendedIngredients(List.of(cheese, pasta));

        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(mockRecipe, HttpStatus.OK));

        ExcludeRequest excludeRequest = new ExcludeRequest();
        excludeRequest.setExcludeIngredients(List.of("Cheese"));

        double totalCalories = spoonacularService.getCustomizedCalories(recipeId, excludeRequest);

        assertEquals(100, totalCalories);
    }

    @Test
    void getRecipeInfo_ShouldReturn404_WhenRecipeIdDoesNotExist() {
        int invalidRecipeId = 999;

        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            spoonacularService.getRecipeInfo(invalidRecipeId);
        });

        assertEquals("Failed to fetch recipe information", exception.getMessage());
    }

    @Test
    void getCustomizedCalories_ShouldIncludeAllIngredients_WhenNoneAreExcluded() {
        int recipeId = 123;

        Ingredient cheese = new Ingredient("Cheese", 1, "piece", new Nutrition(40));
        Ingredient pasta = new Ingredient("Pasta", 100, "grams", new Nutrition(140));

        Recipe mockRecipe = new Recipe();
        mockRecipe.setExtendedIngredients(List.of(cheese, pasta));

        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(mockRecipe, HttpStatus.OK));

        ExcludeRequest excludeRequest = new ExcludeRequest();
        excludeRequest.setExcludeIngredients(List.of());

        double totalCalories = spoonacularService.getCustomizedCalories(recipeId, excludeRequest);

        assertEquals(101, totalCalories);
    }



}
