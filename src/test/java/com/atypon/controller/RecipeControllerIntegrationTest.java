package com.atypon.controller;

import com.atypon.model.ExcludeRequest;
import com.atypon.model.Ingredient;
import com.atypon.model.Nutrition;
import com.atypon.model.Recipe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecipeControllerIntegrationTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getCustomizedCalories_ShouldReturnCaloriesExcludingIngredients() {
        Recipe recipe = new Recipe();
        Ingredient cheese = new Ingredient("Cheese", 1, "piece", new Nutrition(40));
        Ingredient pasta = new Ingredient("Pasta", 100, "grams", new Nutrition(140));
        recipe.setExtendedIngredients(List.of(cheese, pasta));

        ResponseEntity<Recipe> responseEntity = new ResponseEntity<>(recipe, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(responseEntity);

        ExcludeRequest excludeRequest = new ExcludeRequest();
        excludeRequest.setExcludeIngredients(List.of("Cheese"));

        webTestClient.post()
                .uri("/api/recipes/calories?recipeId=456")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(excludeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(100.0);
    }

    @Test
    void getCustomizedCalories_ShouldReturn5xx_WhenRecipeIdIsInvalid() {
        // Mock the RestTemplate to return a 404 Not Found response
        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        ExcludeRequest excludeRequest = new ExcludeRequest();
        excludeRequest.setExcludeIngredients(List.of("Cheese"));

        // Act & Assert: Make the request and expect a 404 status
        webTestClient.post()
                .uri("/api/recipes/calories?recipeId=999")  // Non-existent ID
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(excludeRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCustomizedCalories_ShouldReturnZero_WhenNoIngredientsInRecipe() {
        // Create an empty recipe (no ingredients)
        Recipe emptyRecipe = new Recipe();
        emptyRecipe.setExtendedIngredients(List.of());  // No ingredients

        // Mock the RestTemplate to return the empty recipe
        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(emptyRecipe, HttpStatus.OK));

        ExcludeRequest excludeRequest = new ExcludeRequest();
        excludeRequest.setExcludeIngredients(List.of("Cheese"));

        // Act & Assert: Expect 0 calories when no ingredients are present
        webTestClient.post()
                .uri("/api/recipes/calories?recipeId=456")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(excludeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(0.0);
    }

    @Test
    void getCustomizedCalories_ShouldCountAllIngredients_WhenNoIngredientsExcluded() {
        // Create a mock recipe with ingredients
        Recipe recipe = new Recipe();
        Ingredient cheese = new Ingredient("Cheese", 1, "piece", new Nutrition(1));
        Ingredient pasta = new Ingredient("Pasta", 100, "grams", new Nutrition(100));
        recipe.setExtendedIngredients(List.of(cheese, pasta));

        // Mock the RestTemplate to return the recipe
        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(recipe, HttpStatus.OK));

        // Empty exclude request means no ingredients are excluded
        ExcludeRequest excludeRequest = new ExcludeRequest();
        excludeRequest.setExcludeIngredients(List.of());  // No exclusion

        // Act & Assert: Expect all ingredients to be counted
        webTestClient.post()
                .uri("/api/recipes/calories?recipeId=456")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(excludeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(101.0);  // 1 (cheese) + 100 (pasta)
    }

    @Test
    void getCustomizedCalories_ShouldReturnCalories_WhenExcludeRequestIsEmpty() {
        // Create a mock recipe with ingredients
        Recipe recipe = new Recipe();
        Ingredient cheese = new Ingredient("Cheese", 40, "piece", new Nutrition(40));
        Ingredient pasta = new Ingredient("Pasta", 140, "grams", new Nutrition(140));
        recipe.setExtendedIngredients(List.of(cheese, pasta));

        // Mock the RestTemplate to return the recipe
        when(restTemplate.getForEntity(anyString(), eq(Recipe.class)))
                .thenReturn(new ResponseEntity<>(recipe, HttpStatus.OK));

        // Empty body for exclude request
        ExcludeRequest excludeRequest = new ExcludeRequest();

        // Act & Assert: Expect all ingredients to be counted
        webTestClient.post()
                .uri("/api/recipes/calories?recipeId=456")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(excludeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(180.0);  // 40 (cheese) + 140 (pasta)
    }

    @Test
    void getCustomizedCalories_ShouldReturn5xx_WhenInvalidRequestFormat() {
        // Send an invalid JSON request (e.g., missing fields)
        String invalidRequestBody = "{ \"exclude\": \"Cheese\" }";  // Invalid format

        // Act & Assert: Expect a 400 Bad Request due to invalid request format
        webTestClient.post()
                .uri("/api/recipes/calories?recipeId=456")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequestBody)
                .exchange()
                .expectStatus().is5xxServerError();
    }

}

