package com.atypon.controller;

import com.atypon.model.ExcludeRequest;
import com.atypon.service.SpoonacularService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final SpoonacularService spoonacularService;

    public RecipeController(SpoonacularService spoonacularService) {
        this.spoonacularService = spoonacularService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchRecipes(
            @RequestParam String query,
            @RequestParam(required = false) String cuisine) {

        return ResponseEntity.ok(spoonacularService.searchRecipes(query, cuisine));
    }

    @GetMapping("/info")
    public ResponseEntity<?> getRecipeInfo(@RequestParam int id) {
        try{
            return ResponseEntity.ok(spoonacularService.getRecipeInfo(id));
        }catch (IllegalStateException e){
            return ResponseEntity.notFound().build();
        }

    }


    @PostMapping("/calories")
    public ResponseEntity<?> getCustomCalories(
            @RequestParam int recipeId,
            @RequestBody ExcludeRequest excludeRequest) {
        try {
            return ResponseEntity.ok(spoonacularService.getCustomizedCalories(recipeId, excludeRequest));
        }catch (IllegalStateException e){
            return ResponseEntity.badRequest().build();
        }

    }


}
