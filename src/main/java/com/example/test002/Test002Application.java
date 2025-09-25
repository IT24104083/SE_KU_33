package com.example.test002;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class Test002Application implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(Test002Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ Database connection successful during startup!");
            System.out.println("Database: " + connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    @GetMapping("/test-template")
    public String testTemplate() {
        return "test"; // This should resolve to src/main/resources/templates/test.html
    }
}