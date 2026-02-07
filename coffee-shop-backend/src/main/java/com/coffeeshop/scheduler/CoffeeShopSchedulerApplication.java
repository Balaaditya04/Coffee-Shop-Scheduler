package com.coffeeshop.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Coffee Shop Scheduler.
 * Uses Spring Boot with scheduling enabled for priority recalculation.
 */
@SpringBootApplication
@EnableScheduling
public class CoffeeShopSchedulerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CoffeeShopSchedulerApplication.class, args);
    }
}
