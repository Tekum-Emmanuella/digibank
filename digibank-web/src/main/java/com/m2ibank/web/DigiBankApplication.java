package com.m2ibank.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.m2ibank")
@EntityScan(basePackages = "com.m2ibank")
@EnableJpaRepositories(basePackages = "com.m2ibank")
public class DigiBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigiBankApplication.class, args);
    }
}
