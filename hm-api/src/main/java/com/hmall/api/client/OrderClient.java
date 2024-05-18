package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author qml
 * @date 2024/5/15 15:39
 */
@FeignClient("order-service")
public interface OrderClient {
}
