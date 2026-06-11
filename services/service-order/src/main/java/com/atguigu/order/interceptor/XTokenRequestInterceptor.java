package com.atguigu.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Feign 请求拦截器
 * 
 * 功能：在每次 Feign 调用前，自动添加自定义请求头
 * 
 * 关联说明：
 * - 实现接口：feign.RequestInterceptor
 * - 注册方式：@Component（注册为 Spring Bean）
 * - 应用范围：所有 Feign 客户端（ProductFeignClient、WeatherFeignClient）
 * 
 * 工作流程：
 * 1. Feign 客户端准备发送请求
 * 2. XTokenRequestInterceptor 拦截请求
 * 3. apply() 方法被调用
 * 4. 为请求添加 X-Token 请求头（值为随机 UUID）
 * 5. 请求发送到目标服务
 * 6. 目标服务可以在 Controller 中通过 @RequestHeader 获取 X-Token
 * 
 * 关联：
 * - Feign 客户端：ProductFeignClient、WeatherFeignClient
 * - 目标服务：ProductController（可以通过 request.getHeader("X-Token") 获取）
 * - 使用场景：鉴权、追踪、身份验证等
 * 
 * 示例：
 * - 调用：ProductFeignClient.getProductById(100)
 * - 拦截：XTokenRequestInterceptor.apply()
 * - 添加请求头：X-Token: 550e8400-e29b-41d4-a716-446655440000
 * - 实际请求：GET http://service-product/product/100
 *             Headers: X-Token: 550e8400-e29b-41d4-a716-446655440000
 * 
 * 依赖：spring-cloud-starter-openfeign
 * 
 * 注意：
 * - 拦截器对所有 Feign 客户端生效
 * - 如果需要只对特定 Feign 客户端生效，需要在 @FeignClient 中配置
 */
@Component  // 注册为 Spring Bean，Feign 自动扫描并使用
public class XTokenRequestInterceptor implements RequestInterceptor {
    
    /**
     * 拦截并修改 Feign 请求
     * 
     * 功能：为每个 Feign 请求添加 X-Token 请求头
     * 
     * 关联：
     * - 调用时机：每次 Feign 客户端发送请求前
     * - 应用范围：ProductFeignClient、WeatherFeignClient
     * - 目标服务：ProductController（可通过 request.getHeader("X-Token") 获取）
     * 
     * 请求头说明：
     * - 名称：X-Token
     * - 值：随机生成的 UUID（每次请求不同）
     * - 用途：鉴权、请求追踪、身份验证等
     * 
     * 示例：
     * 调用：productFeignClient.getProductById(100)
     * 拦截后：X-Token: 550e8400-e29b-41d4-a716-446655440000
     * 实际请求：GET http://service-product/product/100
     *            Headers: X-Token: 550e8400-e29b-41d4-a716-446655440000
     * 
     * @param template Feign 请求模板（包含 URL、请求头、请求体等）
     */
    @Override
    public void apply(RequestTemplate template) {
        // 生成随机 UUID 作为 Token
        String token = UUID.randomUUID().toString();
        
        // 打印日志（便于调试）
        System.out.println("XTokenRequestInterceptor 添加 X-Token: " + token);
        System.out.println("请求目标: " + template.url());
        
        // 为请求添加 X-Token 请求头
        template.header("X-Token", token);
    }
}
