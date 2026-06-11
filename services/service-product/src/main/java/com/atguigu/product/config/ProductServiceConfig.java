package com.atguigu.product.config;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用配置类，用于注册Spring容器中缺少的Bean
 */
@Configuration
public class ProductServiceConfig {
    /**
     * 注册RestTemplate Bean
     * Spring Boot不会自动配置RestTemplate，需要手动注册
     * 用于服务间远程调用（如订单服务调用商品服务）
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
