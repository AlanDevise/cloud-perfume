package com.alandevise.multitsdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MultiTsdbApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MultiTsdbApplication.class, args);
        System.out.println("==============================================");
        System.out.println("MultiTSDB Server started successfully!");
        System.out.println("Server port: 1124");
        System.out.println("Swagger UI: http://localhost:1124/swagger-ui.html");
        System.out.println("API Docs: http://localhost:1124/v2/api-docs");
        System.out.println("==============================================");
    }
}
