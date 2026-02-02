package com.mailProject.email.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "pk_100906060_3O93JIRRV7RJO4HU0UTX2UYB3TS8DZOJe");
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
