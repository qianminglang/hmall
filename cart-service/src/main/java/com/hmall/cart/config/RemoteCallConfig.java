package com.hmall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author qml
 * @date 2024/5/12 22:44
 */
@Configuration
public class RemoteCallConfig {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
