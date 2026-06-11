package com.atguigu.order.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.order.bean.Order;
import com.atguigu.order.feign.ProductFeignClient;
import com.atguigu.order.service.OrderService;
import com.atguigu.product.bean.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 订单服务实现类
 * 
 * 关联说明：
 * - 实现接口：OrderService
 * - 注入组件：ProductFeignClient（Feign 客户端）、RestTemplate（HTTP 客户端）
 * - Sentinel 限流：@SentinelResource(value = "createOrder") 资源名
 * - 日志记录：@Slf4j 自动生成 log 对象
 * 
 * 业务流程：
 * 1. 接收 Controller 传入的 productId 和 userId
 * 2. 通过 ProductFeignClient 远程调用商品服务
 * 3. 商品服务不可用时，自动调用 ProductFeignClientFallback 返回兜底数据
 * 4. 组装订单对象返回
 * 
 * 关联：
 * - 上层：OrderController（调用 CreateOrder 方法）
 * - Feign 调用：ProductFeignClient.getProductById() → ProductController.getProduct()
 * - 兜底降级：ProductFeignClientFallback（当商品服务不可用时）
 * - Sentinel：@SentinelResource 可在控制台配置流控规则
 * - 服务注册：在 Nacos 中注册为 service-order
 */
@Slf4j  // Lombok 自动生成 log 对象，用于日志记录
@Service  // 注册为 Spring Bean，支持 @Autowired 注入

public class OrderIServicempl implements OrderService {
    
    /**
     * 服务发现客户端（从 Nacos 获取服务实例列表）
     * 关联：用于获取 service-product 的所有实例 IP 和端口
     * 依赖：spring-cloud-starter-alibaba-nacos-discovery
     */
    @Autowired
    DiscoveryClient discoveryClient;
    
    /**
     * HTTP 客户端（已配置负载均衡）
     * 关联：OrderConfig 中通过 @LoadBalanced 注解配置
     * 使用场景：手动发起 HTTP 请求（见下方三种调用方式）
     * 负载均衡：自动使用轮询策略选择服务实例
     */
    @Autowired
    RestTemplate restTemplate;
    
    /**
     * 负载均衡客户端（手动选择服务实例）
     * 关联：配合 RestTemplate 使用，用于手动选择服务实例
     * 依赖：spring-cloud-starter-loadbalancer
     * 使用场景：getProductFromRemoteWithLoadBalance() 方法
     */
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    
    /**
     * 商品服务 Feign 客户端
     * 关联：
     * - 接口定义：ProductFeignClient.java
     * - 兜底降级：ProductFeignClientFallback（服务不可用时自动调用）
     * - 调用目标：service-product 服务（在 Nacos 注册中心）
     * - 拦截器：XTokenRequestInterceptor（添加 X-Token 请求头）
     * 
     * 工作流程：
     * 1. 调用 getProductById(productId)
     * 2. LoadBalancer 从 Nacos 获取 service-product 实例
     * 3. 选择一个实例（轮询策略）
     * 4. 发送 HTTP GET 请求到 /product/{productId}
     * 5. 如果服务不可用或超时，自动调用 ProductFeignClientFallback
     * 6. 返回商品信息（正常或兜底）
     * 
     * 依赖：spring-cloud-starter-openfeign
     */
    @Autowired
    ProductFeignClient productFeignClient;

    /**
     * 创建订单（核心业务方法）
     * 
     * 业务流程：
     * 1. 通过 Feign 调用商品服务获取商品信息
     * 2. 如果商品服务不可用，自动调用 Fallback 返回兜底商品
     * 3. 计算订单总金额（商品单价 × 商品数量）
     * 4. 组装订单对象（用户信息、商品列表、地址等）
     * 5. 返回订单对象
     * 
     * 关联：
     * - Feign 调用：ProductFeignClient.getProductById(productId)
     * - 目标服务：service-product（在 Nacos 注册中心）
     * - 目标接口：ProductController.getProduct()
     * - 兜底降级：ProductFeignClientFallback.getProductById()
     * - Sentinel 限流：@SentinelResource(value = "createOrder")
     * 
     * 示例：
     * - 正常场景：productId=100 → 返回"苹果100"，单价99，数量2 → 总金额198
     * - 降级场景：商品服务不可用 → 返回"未知商品"，单价0，数量0 → 总金额0
     * 
     * @param productId 商品ID
     * @param userId 用户ID
     * @return Order 订单对象
     */
    // Sentinel 限流资源名，可在 Dashboard 配置流控规则
    @SentinelResource(value = "createOrder",blockHandler = "creatOrderFallback")
    @Override
    public Order CreateOrder(Long productId, Long userId) {
        
        // ========== 方式1：使用 Feign 调用（推荐）==========
        // 优点：代码简洁，支持负载均衡，自动降级
        // Product product = productFeignClient.getProductById(productId);
        
        // ========== 方式2：使用 RestTemplate + @LoadBalanced 调用（推荐）==========
        // Product product = getProductFromRemoteWithLoadBalanceAnnotation(productId);
        
        // ========== 方式3：手动负载均衡调用（不推荐，仅作演示）==========
        // Product product = getProductFromRemoteWithLoadBalance(productId);
        
        // ========== 方式4：无负载均衡调用（不推荐，仅作演示）==========
        // Product product = getProductFromRemote(productId);
        
        // 当前使用 Feign 方式（最常用）
        Product product = productFeignClient.getProductById(productId);
        
        // 组装订单对象
        Order order = new Order();
        order.setId(1L);  // 订单ID（实际应使用分布式ID生成器，如雪花算法）
        
        // 计算总金额：商品单价 × 商品数量
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(product.getNum())));
        
        order.setUserId(userId);  // 用户ID
        order.setNickName("zhangsan");  // 用户昵称（实际应从用户服务获取）
        order.setAddress("尚硅谷");  // 收货地址（实际应从地址服务获取）
        
        // 商品列表（当前只有一个商品，实际可能多个）
        order.setProductList(Arrays.asList(product));
        
        return order;
    }
    //执行兜底回调
    public Order creatOrderFallback(Long productId, Long userId, BlockException e) {
        Order order = new Order();
        order.setId(0L);
        order.setTotalAmount(new BigDecimal("0"));
        order.setUserId(userId);
        order.setNickName("未知用户");
        order.setAddress("异常信息："+e.getClass());
        return order;
    }
    
    /**
     * 方式1：使用 LoadBalancerClient 手动负载均衡
     * 
     * 流程：
     * 1. loadBalancerClient.choose("service-product") 从 Nacos 获取实例
     * 2. LoadBalancer 使用轮询策略选择一个实例
     * 3. 手动拼接完整的 URL（http://ip:port/product/{productId}）
     * 4. 使用 RestTemplate 发送 HTTP GET 请求
     * 
     * 关联：
     * - 服务发现：DiscoveryClient（从 Nacos 获取实例列表）
     * - 负载均衡：LoadBalancerClient（选择实例）
     * - HTTP 客户端：RestTemplate（发送请求）
     * - 目标服务：service-product（在 Nacos 注册中心）
     * 
     * 缺点：代码冗余，需要手动处理 URL 拼接
     * 
     * @param productId 商品ID
     * @return Product 商品对象
     */
    private Product getProductFromRemoteWithLoadBalance(Long productId) {
        // 1. 获取商品服务的一个实例（LoadBalancer 自动选择）
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        
        // 2. 拼接完整的请求 URL
        String url = "http://" + choose.getHost() + ":" + choose.getPort() + "/product/" + productId;
        log.info("远程请求" + url);
        
        // 3. 发送 GET 请求并反序列化为 Product 对象
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
    
    /**
     * 方式2：无负载均衡（直接使用第一个实例）
     * 
     * 流程：
     * 1. discoveryClient.getInstances("service-product") 获取所有实例
     * 2. instances.get(0) 取第一个实例（无负载均衡）
     * 3. 手动拼接完整的 URL
     * 4. 使用 RestTemplate 发送请求
     * 
     * 关联：
     * - 服务发现：DiscoveryClient（从 Nacos 获取所有实例）
     * - 目标服务：service-product（在 Nacos 注册中心）
     * 
     * 缺点：没有负载均衡，第一个实例压力大
     * 
     * @param productId 商品ID
     * @return Product 商品对象
     */
    private Product getProductFromRemote(Long productId) {
        // 1. 获取商品服务的所有实例
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        
        // 2. 取第一个实例（无负载均衡）
        ServiceInstance instance = instances.get(0);
        
        // 3. 拼接完整的请求 URL
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/product/" + productId;
        log.info("远程请求" + url);
        
        // 4. 发送 GET 请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
    
    /**
     * 方式3：使用 @LoadBalanced 注解（推荐）
     * 
     * 流程：
     * 1. 使用服务名直接拼接 URL（http://service-product/product/{productId}）
     * 2. LoadBalancer 自动将 service-product 替换为实际实例的 IP:Port
     * 3. RestTemplate 发送请求
     * 
     * 关联：
     * - LoadBalanced：OrderConfig 中配置的 @LoadBalanced 注解
     * - 负载均衡：自动使用轮询策略选择实例
     * - 目标服务：service-product（在 Nacos 注册中心）
     * 
     * 优点：代码简洁，URL 中直接使用服务名
     * 
     * @param productId 商品ID
     * @return Product 商品对象
     */
    private Product getProductFromRemoteWithLoadBalanceAnnotation(Long productId) {
        // 直接使用服务名，LoadBalancer 会自动解析为实际实例的 IP:Port
        String url = "http://service-product/product/" + productId;
        log.info("远程请求" + url);
        
        // 发送 GET 请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
}
