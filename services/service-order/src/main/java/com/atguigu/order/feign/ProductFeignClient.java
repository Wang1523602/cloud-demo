package com.atguigu.order.feign;

import com.atguigu.order.feign.fallback.ProductFeignClientFallback;
import com.atguigu.product.bean.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 商品服务 Feign 客户端接口
 * 
 * 关联说明：
 * - Feign 声明式 HTTP 客户端：简化 HTTP 调用
 * - 服务名：service-product（在 Nacos 注册中心）
 * - 负载均衡：自动使用 LoadBalancer 选择实例
 * - 兜底降级：ProductFeignClientFallback（服务不可用时）
 * - 请求拦截器：XTokenRequestInterceptor（添加 X-Token 请求头）
 * 
 * 工作流程：
 * 1. 调用 getProductById(productId)
 * 2. LoadBalancer 从 Nacos 获取 service-product 实例
 * 3. 使用轮询策略选择一个实例
 * 4. XTokenRequestInterceptor 拦截，添加 X-Token 请求头
 * 5. 发送 HTTP GET 请求到 http://实例IP:端口/product/{productId}
 * 6. 如果服务不可用或超时，自动调用 ProductFeignClientFallback
 * 7. 返回商品信息（正常或兜底）
 * 
 * 关联：
 * - 调用方：OrderIServicempl.CreateOrder()
 * - 目标服务：service-product（在 Nacos 注册中心）
 * - 目标接口：ProductController.getProduct()
 * - 兜底类：ProductFeignClientFallback
 * - 拦截器：XTokenRequestInterceptor
 * 
 * 依赖：spring-cloud-starter-openfeign
 */

@FeignClient(
    value = "service-product",  // 目标服务名（在 Nacos 注册中心）
    fallback = ProductFeignClientFallback.class  // 兜底降级类（服务不可用时）
)
public interface ProductFeignClient {
    
    /**
     * 根据商品ID查询商品信息
     * 
     * Spring MVC 注解的两套使用逻辑：
     * 1. 标注在 Controller 方法上：表示接收这样的请求
     * 2. 标注在 FeignClient 方法上：表示发送这样的请求
     * 
     * 请求示例：
     * GET http://service-product/product/100
     * Headers: X-Token: {随机UUID}（由 XTokenRequestInterceptor 添加）
     * 
     * 关联：
     * - 目标接口：ProductController.getProduct()
     * - 目标服务：service-product（在 Nacos 注册中心）
     * - 兜底降级：ProductFeignClientFallback.getProductById()
     * - 拦截器：XTokenRequestInterceptor.apply()
     * 
     * 重试机制：
     * - 最大重试次数：5次（OrderConfig.retryer 配置）
     * - 初始间隔：100ms
     * - 最大间隔：1s
     * - 超时时间：connect-timeout=3000ms, read-timeout=5000ms
     * 
     * 降级机制：
     * - 触发条件：服务不可用、超时、异常
     * - 降级逻辑：调用 ProductFeignClientFallback.getProductById()
     * - 返回数据：兜底商品（名称="未知商品"，价格=0，数量=0）
     * 
     * @param id 商品ID（从路径变量获取，如 /product/100）
     * @return Product 商品对象（正常或兜底）
     */
    @GetMapping("/product/{id}")  // 发送 GET 请求到 /product/{id}
    Product getProductById(@PathVariable("id") Long id);  // {id} 绑定到方法参数
}
