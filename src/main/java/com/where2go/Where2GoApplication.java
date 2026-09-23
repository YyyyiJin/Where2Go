package com.where2go;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Where2Go Spring Boot application.
 * This class initializes the application context and starts all services.
 */
@SpringBootApplication(scanBasePackages = "com.where2go")
public class Where2GoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Where2GoApplication.class, args);

        // If you ever want to force a full fetch on startup (NOT recommended,
        // because it consumes API quota), you can inject TomorrowFetcher here.
        //
        // var ctx = SpringApplication.run(Where2GoApplication.class, args);
        // ctx.getBean(TomorrowFetcher.class).fetchAndSaveAllConfiguredCities();

        System.out.println("🚀 Where2Go Weather Service started successfully.");
    }
}
