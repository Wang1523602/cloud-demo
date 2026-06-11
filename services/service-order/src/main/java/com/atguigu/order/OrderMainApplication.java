package com.atguigu.order;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 订单服务启动类
 * 
 * 关联说明：
 * - @EnableFeignClients：扫描并启用 Feign 客户端（ProductFeignClient、WeatherFeignClient）
 * - @EnableDiscoveryClient：启用服务发现，将服务注册到 Nacos
 * - ApplicationRunner：应用启动后监听 Nacos 配置变化
 * 
 * 依赖：spring-cloud-starter-alibaba-nacos-config、spring-cloud-starter-alibaba-nacos-discovery
 */
@EnableFeignClients  // 启用 Feign 客户端，扫描 @FeignClient 注解的接口
@EnableDiscoveryClient  // 启用服务发现，自动注册到 Nacos
@SpringBootApplication
public class OrderMainApplication {
    
    /**
     * 应用启动入口
     * 关联：启动后 Spring 容器会加载所有 Bean（包括 ApplicationRunner）
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderMainApplication.class, args);
    }

    /**
     * Nacos 配置监听器
     * 作用：监听 Nacos 配置中心的配置变化，实现配置自动刷新
     * 
     * 关联说明：
     * - 监听文件：service-order.properties（在 Nacos 配置中心）
     * - 监听组：DEFAULT_GROUP
     * - 执行线程池：固定大小 4 的线程池
     * 
     * 配置来源：Nacos 配置中心 → bootstrap.yml → OrderProperties
     * 
     * @param nacosConfigManager Nacos 配置管理器（由 spring-cloud-starter-alibaba-nacos-config 自动注入）
     * @return ApplicationRunner 启动后立即执行
     */
    @Bean
    ApplicationRunner applicationRunner(NacosConfigManager nacosConfigManager){
        return args-> {
            // 获取 Nacos 配置服务
            ConfigService configService = nacosConfigManager.getConfigService();
            
            // 添加配置监听器：监听 service-order.properties 文件的变化
            configService.addListener("service-order.properties",
                    "DEFAULT_GROUP", new Listener() {
                
                /**
                 * 配置变更回调的线程执行器
                 * 使用固定大小为4的线程池处理配置变更事件
                 */
                @Override
                public Executor getExecutor() {
                    return Executors.newFixedThreadPool(4);
                }

                /**
                 * 接收配置变更通知
                 * 当 Nacos 中的配置发生变化时，自动调用此方法
                 * 
                 * @param ConfigInfo 变更后的配置内容
                 * 关联：OrderProperties 会自动更新（无需 @RefreshScope）
                 */
                @Override
                public void receiveConfigInfo(String ConfigInfo) {
                    System.out.println("变化的配置信息:" + ConfigInfo);
                    System.out.println("邮件通知");  // 实际场景可发送邮件、钉钉等通知
                }
            });
            System.out.println("===========");
        };
    }
}
