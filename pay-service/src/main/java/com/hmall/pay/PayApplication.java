package com.hmall.pay;

import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author qml
 * @date 2024/5/15 13:40
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hmall.api",defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("com.hmall.pay.mapper")
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class);
    }
}
