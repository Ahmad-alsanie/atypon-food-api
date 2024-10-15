package com.atypon.model;

import lombok.Data;

import java.util.List;

@Data
public class ExcludeRequest {
    private List<String> excludeIngredients;
}
