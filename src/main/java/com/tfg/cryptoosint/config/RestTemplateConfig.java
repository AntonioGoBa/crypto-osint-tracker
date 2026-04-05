package com.tfg.cryptoosint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    public RestTemplateConfig(RestTemplateLoggingInterceptor restTemplateLoggingInterceptor) {
        this.restTemplateLoggingInterceptor = restTemplateLoggingInterceptor;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
        );
        restTemplate.setInterceptors(List.of(restTemplateLoggingInterceptor));
        return restTemplate;
    }
}