package com.atguigu.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 天气服务 Feign 客户端接口
 * 
 * 关联说明：
 * - 调用第三方 API：聚合数据天气查询接口
 * - 直接 URL：https://apis.juhe.cn（无需 Nacos 注册）
 * - 不需要负载均衡：直接调用固定地址
 * - 不需要兜底降级：第三方 API 不受自己控制
 * 
 * 工作流程：
 * 1. 调用 getWeather(contentType, key, city)
 * 2. 发送 POST 请求到 https://apis.juhe.cn/simpleWeather/query
 * 3. 返回 JSON 格式的天气数据
 * 
 * 关联：
 * - 第三方 API：聚合数据（https://apis.juhe.cn）
 * - 使用示例：WeatherTest.java（单元测试）
 * 
 * 依赖：spring-cloud-starter-openfeign
 * 
 * 注意：
 * - 调用第三方 API 时，需要先注册获取 key
 * - 参数需严格按照第三方 API 文档提供
 */
@FeignClient(
    value = "service-weather",  // 客户端名称（仅用于标识，无实际意义）
    url = "https://apis.juhe.cn"  // 第三方 API 的基础 URL
)
public interface WeatherFeignClient {
    
    /**
     * 查询天气信息
     * 
     * 功能：调用聚合数据天气查询 API
     * 
     * 请求示例：
     * POST https://apis.juhe.cn/simpleWeather/query
     * Headers: Content-Type: application/x-www-form-urlencoded
     * Body: key=9d7eb7d6cd3062293012e5668317a190&city=南阳
     * 
     * 关联：
     * - 第三方 API：聚合数据天气查询接口
     * - 使用示例：WeatherTest.java（单元测试）
     * 
     * 参数说明：
     * - contentType：请求头 Content-Type，值为 "application/x-www-form-urlencoded"
     * - key：API Key（需在聚合数据官网申请）
     * - city：城市名称（如"南阳"、"北京"）
     * 
     * 返回数据：
     * JSON 格式的天气数据，包含实时天气、未来天气预报等
     * 
     * @param contentType 请求头 Content-Type
     * @param key API Key
     * @param city 城市名称
     * @return String JSON 格式的天气数据
     */
    @PostMapping("/simpleWeather/query")  // 调用路径：https://apis.juhe.cn/simpleWeather/query
    String getWeather(
        @RequestHeader("Content-Type") String contentType,  // 请求头
        @RequestParam("key") String key,  // 请求参数：API Key
        @RequestParam("city") String city  // 请求参数：城市名称
    );
}
