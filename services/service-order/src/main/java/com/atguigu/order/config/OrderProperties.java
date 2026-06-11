package com.atguigu.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单配置属性类
 * 
 * 功能：自动绑定 Nacos 配置中心中的配置到 Java 对象
 * 
 * 关联说明：
 * - 配置来源：Nacos 配置中心 → common.properties（group=order）
 * - 配置格式：application-feign.yml 中通过 spring.config.import 导入
 * - 绑定规则：order.* 前缀的属性映射到对应字段
 * - 自动刷新：Nacos 配置变更时自动更新（无需 @RefreshScope）
 * 
 * 宽松绑定（支持以下格式）：
 * - order.timeout → timeout
 * - order.auto-confirm → autoConfirm（横杠转驼峰）
 * - order.AUTO_CONFIRM → autoConfirm（忽略大小写）
 * 
 * Nacos 配置示例：
 * # common.properties（在 Nacos 中）
 * order.timeout=30
 * order.auto-confirm=true
 * order.db-url=jdbc:mysql://localhost:3306/order_db
 * 
 * 关联：
 * - 监听器：OrderMainApplication.ApplicationRunner（监听配置变更）
 * - 使用方：OrderController.config()（展示配置）
 * 
 * @Component：注册为 Spring Bean，支持 @Autowired 注入
 * @ConfigurationProperties(prefix = "order")：绑定 order.* 前缀的配置
 * @Data：Lombok 自动生成 getter/setter
 */
@Component
@Data
@ConfigurationProperties(prefix = "order")
public class OrderProperties {
    
    /**
     * 订单超时时间（秒）
     * 对应配置：order.timeout
     */
    String timeout;
    
    /**
     * 是否自动确认订单
     * 对应配置：order.auto-confirm（自动转驼峰）
     */
    String autoConfirm;
    
    /**
     * 订单数据库连接URL
     * 对应配置：order.db-url（横杠自动转驼峰）
     */
    String dbUrl;
}
