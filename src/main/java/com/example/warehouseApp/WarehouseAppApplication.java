package com.example.warehouseApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WarehouseAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseAppApplication.class, args);
    }

}
