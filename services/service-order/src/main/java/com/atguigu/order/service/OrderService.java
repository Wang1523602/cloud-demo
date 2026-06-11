package com.atguigu.order.service;

import com.atguigu.order.bean.Order;
import org.springframework.stereotype.Service;

/**
 * 订单服务接口
 * 
 * 关联说明：
 * - 实现类：OrderIServicempl（包含 Feign 调用和业务逻辑）
 * - 调用方：OrderController（通过 @Autowired 注入）
 * - Sentinel 限流：在实现类上使用 @SentinelResource 注解
 * 
 * 服务流程：
 * 1. OrderController 接收请求（/create?productId=100&userId=2）
 * 2. 调用 OrderService.CreateOrder()
 * 3. OrderIServicempl 通过 ProductFeignClient 调用商品服务
 * 4. 商品服务返回商品信息（或 Fallback 返回兜底数据）
 * 5. 组装订单对象返回
 * 
 * 关联：
 * - 上层：OrderController（接收用户请求）
 * - 实现：OrderIServicempl（业务逻辑）
 * - 远程调用：ProductFeignClient → ProductController
 * - 限流保护：@SentinelResource(value = "createOrder")
 * - 兜底降级：ProductFeignClientFallback（服务异常时返回默认商品）
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 
     * 业务流程：
     * 1. 通过 Feign 调用商品服务获取商品信息
     * 2. 计算订单总金额（商品单价 × 商品数量）
     * 3. 组装订单对象（用户信息、商品列表、地址等）
     * 4. 返回订单对象
     * 
     * 关联：
     * - Feign 调用：ProductFeignClient.getProductById(productId)
     * - 商品服务：service-product（在 Nacos 注册中心）
     * - 兜底机制：ProductFeignClientFallback（商品服务不可用时）
     * - 限流保护：@SentinelResource(value = "createOrder")
     * 
     * @param productId 商品ID（从查询参数获取，如 ?productId=100）
     * @param userId 用户ID（从查询参数获取，如 ?userId=2）
     * @return Order 订单对象（包含用户信息、商品列表、总金额等）
     */
    Order CreateOrder(Long productId, Long userId);
}
