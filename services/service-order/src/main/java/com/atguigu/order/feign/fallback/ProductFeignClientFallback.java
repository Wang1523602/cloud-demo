package com.atguigu.order.feign.fallback;

import com.atguigu.order.feign.ProductFeignClient;
import com.atguigu.product.bean.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 商品服务 Feign 兜底降级类
 * 
 * 功能：当商品服务不可用、超时或异常时，返回默认商品信息
 * 
 * 关联说明：
 * - 实现接口：ProductFeignClient
 * - 触发条件：服务不可用、超时（超过 read-timeout）、异常
 * - 注册方式：@Component（注册为 Spring Bean）
 * - 配置位置：ProductFeignClient.fallback = ProductFeignClientFallback.class
 * 
 * 工作流程：
 * 1. ProductFeignClient.getProductById() 调用商品服务
 * 2. 如果服务不可用或超时，自动触发降级
 * 3. 调用 ProductFeignClientFallback.getProductById()
 * 4. 返回默认商品信息（名称="未知商品"，价格=0，数量=0）
 * 5. 业务流程继续执行（不会中断）
 * 
 * 关联：
 * - Feign 接口：ProductFeignClient
 * - 调用方：OrderIServicempl（通过 ProductFeignClient 调用）
 * - 目标服务：service-product（不可用时触发降级）
 * - Sentinel 限流：可配合 Sentinel 实现流控降级
 * 
 * 依赖：spring-cloud-starter-alibaba-sentinel
 * 
 * 注意：需要在 bootstrap.yml 或 application.yml 中启用 Sentinel（sentinel.enabled=true）
 */
@Component  // 注册为 Spring Bean，Feign 可以找到并使用
public class ProductFeignClientFallback implements ProductFeignClient {
    
    /**
     * 商品查询兜底方法
     * 
     * 触发场景：
     * 1. 商品服务未启动
     * 2. 商品服务超时（超过 read-timeout 配置的时间）
     * 3. 网络异常
     * 4. 商品服务返回 5xx 错误
     * 
     * 关联：
     * - Feign 接口：ProductFeignClient.getProductById()
     * - 调用方：OrderIServicempl.CreateOrder()
     * - 正常场景：ProductController.getProduct() 返回真实商品
     * - 降级场景：返回默认商品，保证业务流程不中断
     * 
     * 返回数据：
     * - id：传入的商品ID（保持不变）
     * - productName："未知商品"（兜底标识）
     * - price：0（兜底价格）
     * - num：0（兜底数量）
     * 
     * @param id 商品ID（从 Feign 接口传入）
     * @return Product 兜底商品对象
     */
    @Override
    public Product getProductById(Long id) {
        System.out.println("========== 兜底回调被触发！商品服务不可用 ==========");
        
        // 创建默认商品对象
        Product product = new Product();
        product.setId(id);  // 保留原始商品ID
        product.setPrice(new BigDecimal("0"));  // 兜底价格为0
        product.setProductName("未知商品(已降级)");  // 兜底标识
        product.setNum(0);  // 兜底数量为0
        
        return product;
    }
}
