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
}

