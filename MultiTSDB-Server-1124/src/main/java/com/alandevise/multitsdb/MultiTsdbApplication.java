package com.alandevise.multitsdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MultiTsdbApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MultiTsdbApplication.class, args);
        System.out.println("==============================================");
        System.out.println("MultiTSDB Server started successfully!");
        System.out.println("Server port: 1124");
        System.out.println("System health: http://localhost:1124/api/system/health");
        System.out.println("IoTDB APIs: http://localhost:1124/api/iotdb/*");
        System.out.println("TDEngine APIs: http://localhost:1124/api/tdengine/*");
        System.out.println("==============================================");
    }
}
