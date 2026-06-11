package com.atguigu.order.controller;

import com.atguigu.order.bean.Order;
import com.atguigu.order.config.OrderProperties;
import com.atguigu.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/*@RequestMapping("/api/order")*/
@Slf4j
/**
 * 订单服务控制器
 * 
 * 关联说明：
 * - 注入服务：OrderService（业务逻辑）
 * - 注入配置：OrderProperties（从 Nacos 读取的配置）
 * - 自动刷新：@RefreshScope（配置变更时自动刷新 Bean）
 * 
 * 接口列表：
 * - GET /config：查看订单服务配置
 * - GET /create?productId=100&userId=2：创建订单
 * 
 * 关联：
 * - 上层：接收 HTTP 请求
 * - 服务层：OrderService → OrderIServicempl
 * - 远程调用：ProductFeignClient → ProductController
 * - 配置来源：Nacos 配置中心 → common.properties
 */
@RefreshScope  // 配置自动刷新，Nacos 配置变更时自动更新 OrderProperties
@RestController  // @Controller + @ResponseBody，返回 JSON 数据
public class OrderController {
    
    /**
     * 订单服务（业务逻辑层）
     * 关联：OrderIServicempl（实现类）
     * 注入方式：@Autowired（自动装配）
     */
    @Autowired
    OrderService orderService;
    
    /**
     * 订单配置属性（从 Nacos 读取）
     * 关联：OrderProperties（配置属性类）
     * 配置来源：Nacos 配置中心 → common.properties（group=order）
     * 字段：timeout、autoConfirm、dbUrl
     */
    @Autowired
    OrderProperties orderProperties;
    
    /**
     * 查看订单服务配置
     * 
     * 功能：展示从 Nacos 配置中心读取的订单配置
     * 
     * 关联：
     * - 配置类：OrderProperties
     * - 配置来源：Nacos → common.properties（group=order）
     * - 配置监听：OrderMainApplication.ApplicationRunner（配置变更时打印日志）
     * 
     * 配置示例：
     * # common.properties（在 Nacos 中）
     * order.timeout=30
     * order.auto-confirm=true
     * order.db-url=jdbc:mysql://localhost:3306/order_db
     * 
     * 访问示例：
     * GET http://localhost:8000/config
     * 返回：order.timeout:30:order.Auto confirm:true:order.db-url=jdbc:mysql://localhost:3306/order_db
     * 
     * @return 配置信息字符串（格式：key:value:key:value:key=value）
     */
    @GetMapping("/config")
    public String config(){
        return "order.timeout:" + orderProperties.getTimeout() + ":" +
               "order.Auto confirm:" + orderProperties.getAutoConfirm() + ":" +
               "order.db-url=" + orderProperties.getDbUrl();
    }

    /**
     * 创建订单
     * 
     * 功能：创建订单并返回订单详情
     * 
     * 业务流程：
     * 1. 接收查询参数 productId 和 userId
     * 2. 调用 OrderService.CreateOrder()
     * 3. OrderIServicempl 通过 ProductFeignClient 调用商品服务
     * 4. 商品服务返回商品信息（或 Fallback 返回兜底数据）
     * 5. 组装订单对象返回
     * 
     * 关联：
     * - 服务层：OrderService → OrderIServicempl
     * - Feign 调用：ProductFeignClient.getProductById(productId)
     * - 目标服务：service-product（在 Nacos 注册中心）
     * - 目标接口：ProductController.getProduct()
     * - 兜底降级：ProductFeignClientFallback（服务不可用时）
     * - Sentinel 限流：@SentinelResource(value = "createOrder")
     * 
     * 访问示例：
     * GET http://localhost:8000/create?productId=100&userId=2
     * 
     * 正常返回：
     * {
     *   "id": 1,
     *   "userId": 2,
     *   "nickName": "zhangsan",
     *   "address": "尚硅谷",
     *   "totalAmount": 198,
     *   "productList": [{
     *     "id": 100,
     *     "productName": "苹果100",
     *     "price": 99,
     *     "num": 2
     *   }]
     * }
     * 
     * 降级返回（商品服务不可用时）：
     * {
     *   "id": 1,
     *   "userId": 2,
     *   "nickName": "zhangsan",
     *   "address": "尚硅谷",
     *   "totalAmount": 0,
     *   "productList": [{
     *     "id": 100,
     *     "productName": "未知商品",
     *     "price": 0,
     *     "num": 0
     *   }]
     * }
     * 
     * @param productId 商品ID（通过 @RequestParam 绑定查询参数 ?productId=100）
     * @param userId 用户ID（通过 @RequestParam 绑定查询参数 ?userId=2）
     * @return Order 订单对象（JSON 格式）
     */
    @GetMapping("/create")
    public Order CreateOrder(@RequestParam("productId") Long productId,
                             @RequestParam("userId") Long userId){
        // 调用服务层创建订单
        Order order = orderService.CreateOrder(productId, userId);
        return order;
    }
    @GetMapping("/writeDb")
    public String writeDb(){
        return "writeDb success...";
    }
    @GetMapping("/readDb")
    public String readDb(){
        log.info("readDb...");
        return "readDb success...";
    }
}
