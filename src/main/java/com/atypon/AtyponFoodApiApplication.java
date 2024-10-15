package com.atypon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration.class
})
public class AtyponFoodApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtyponFoodApiApplication.class, args);
	}

}
