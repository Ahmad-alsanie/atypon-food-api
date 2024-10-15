package com.atypon.model;

import lombok.Data;

import java.util.List;

@Data
public class Recipe {
    private int id;  // for future improvements you can use UUID
    private String title;
    private List<Ingredient> extendedIngredients;
    private Nutrition nutrition;
}
