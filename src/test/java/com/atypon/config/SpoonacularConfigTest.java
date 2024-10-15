package com.atypon.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SpoonacularConfigTest {

    @Autowired
    private SpoonacularConfig spoonacularConfig;

    @Test
    void testConfigLoading() {
        assertNotNull(spoonacularConfig.getBaseUrl());
        assertNotNull(spoonacularConfig.getApiKey());
    }
}

