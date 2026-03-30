package com.mailProject.email.config;

import com.mailProject.email.security.ClickupContext;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private String token;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            String token = ClickupContext.getToken();

            if (token != null) {
                requestTemplate.header("Authorization", token);
            }
        };
    }

}
