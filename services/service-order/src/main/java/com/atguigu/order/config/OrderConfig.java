package com.atguigu.order.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 订单服务配置类
 * 
 * 关联说明：
 * - Retryer：配置 Feign 重试策略（关联 application-feign.yml 的超时配置）
 * - RestTemplate：配置负载均衡 RestTemplate（用于 HTTP 远程调用）
 * - Logger.Level：配置 Feign 日志级别（关联 application-feign.yml 的 logger-level）
 * 
 * 依赖：spring-cloud-starter-loadbalancer、spring-cloud-starter-openfeign
 */
@Configuration
public class OrderConfig {

    /**
     * Feign 重试策略配置
     * 
     * 关联说明：
     * - 重试器：Retryer.Default(初始间隔ms, 最大间隔s, 最大重试次数)
     * - 超时配置：application-feign.yml 中的 connect-timeout 和 read-timeout
     * 
     * 工作流程：
     * 1. Feign 调用失败（超时、异常）
     * 2. 等待初始间隔（100ms）
     * 3. 重试，每次间隔指数增长
     * 4. 达到最大间隔（1s）后保持不变
     * 5. 达到最大重试次数（5次）后抛出异常
     * 
     * 关联：
     * - Feign 客户端：ProductFeignClient、WeatherFeignClient
     * - 兜底类：ProductFeignClientFallback（重试失败后触发）
     * 
     * @return Feign 重试器实例
     */
    @Bean
    Retryer retryer(){
        // 参数含义：初始间隔100ms，最大间隔1秒，最多重试5次
        // 实际调用次数 = 1次原始请求 + 5次重试 = 6次
        return new Retryer.Default(100, 1, 5);
    }
    
    /**
     * RestTemplate 负载均衡配置
     * 
     * 关联说明：
     * - @LoadBalanced：启用负载均衡，RestTemplate 会使用 Spring Cloud LoadBalancer
     * - 服务发现：从 Nacos 获取服务实例列表
     * - 负载策略：默认使用轮询（RoundRobinLoadBalancer）
     * 
     * 使用场景：
     * - 需要手动发起 HTTP 请求时（非 Feign 调用）
     * - 参考：OrderIServicempl.java 中的三种调用方式
     * 
     * 关联：
     * - Nacos Discovery：从注册中心获取服务实例
     * - LoadBalancer：自动选择服务实例进行负载均衡
     * - 被调用方：ProductController（服务名：service-product）
     * 
     * @return 配置好负载均衡的 RestTemplate
     */
    @Bean
    @LoadBalanced  // 启用负载均衡，支持 http://service-product/product/1 这样的调用
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Feign 日志级别配置
     * 
     * 关联说明：
     * - Logger.Level.FULL：记录完整的请求和响应信息（包括请求头、响应体等）
     * - 其他级别：NONE（不记录）、BASIC（仅记录方法、URL、状态码）、HEADERS（记录头信息）
     * 
     * 日志位置：
     * - 控制台输出
     * - 日志配置：application-feign.yml 中的 logger-level: full
     * 
     * 关联：
     * - Feign 客户端：ProductFeignClient、WeatherFeignClient
     * - 请求拦截器：XTokenRequestInterceptor（会记录添加的请求头）
     * 
     * @return Feign 日志级别枚举
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // 记录最详细的日志
    }
}
