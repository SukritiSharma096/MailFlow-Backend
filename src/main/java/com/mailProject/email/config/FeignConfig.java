package com.mailProject.email.config;

import com.mailProject.email.security.ClickupContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = ClickupContext.getToken();

            if (token != null && !token.isBlank()) {
                requestTemplate.header("Authorization", token);
            }
        };
    }
}