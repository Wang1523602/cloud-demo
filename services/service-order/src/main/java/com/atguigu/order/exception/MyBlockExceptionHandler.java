package com.atguigu.order.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.common.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
@Slf4j
/**
 * Sentinel 全局流控/降级异常处理器
 * 
 * 功能：当 Sentinel 触发流控、降级、热点参数限流等规则时，自定义返回 JSON 格式的错误信息
 * 
 * 关联说明：
 * - 实现接口：BlockExceptionHandler（Spring Web MVC v6.x 适配器）
 * - 注册方式：@Component（自动注册为全局 Block 异常处理器）
 * - 触发时机：Sentinel 的流控规则（QPS、线程数）或降级规则被触发时
 * 
 * 工作流程：
 * 1. 用户请求进入 Controller（比如 /create?productId=100&userId=2）
 * 2. Sentinel 根据配置的规则判断是否需要限流
 * 3. 如果触发流控/降级 → Sentinel 抛出 BlockException
 * 4. 本处理器 handle() 方法被调用
 * 5. 返回 JSON 格式的错误信息（而非默认的错误页面）
 * 
 * 关联：
 * - Sentinel 客户端：spring-cloud-starter-alibaba-sentinel
 * - Sentinel Dashboard：可在控制台配置流控/降级规则
 * - 被保护资源：@SentinelResource(value = "createOrder")
 * - 返回封装类：com.atguigu.common.R（统一响应结果类）
 * 
 * 适配版本说明：
 * - Spring MVC 6.x → 使用 spring.webmvc_v6x 包
 * - Spring MVC 5.x → 使用 spring.webmvc 包
 * - 当前项目：Spring Boot 3.3.4 → Spring MVC 6.x → 使用 v6x 适配器
 * 
 * 依赖：
 * - spring-cloud-starter-alibaba-sentinel（提供 BlockExceptionHandler 接口）
 * - jackson-databind（提供 ObjectMapper，用于 JSON 序列化）
 * 
 * 示例请求（触发流控后）：
 * GET /create?productId=100&userId=2
 * 
 * 返回（JSON 格式）：
 * {
 *   "code": 500,
 *   "message": "createOrder被Sentinel限制了,原因:class com.alibaba.csp.sentinel.slots.block.flow.FlowException"
 * }
 */
@Component  // 注册为 Spring Bean，Sentinel 自动扫描并作为全局 Block 异常处理器
public class MyBlockExceptionHandler implements BlockExceptionHandler {

    /**
     * Jackson JSON 序列化/反序列化核心工具类实例
     * 
     * 功能：将 Java 对象（R）转换为 JSON 字符串
     * 
     * 关联：
     * - 响应封装：com.atguigu.common.R（统一响应结果类）
     * - 输出流：HttpServletResponse.getWriter()
     * 
     * 使用示例：
     * R error = R.erro(500, "错误信息");
     * String json = objectMapper.writeValueAsString(error);
     * // json = "{\"code\":500,\"message\":\"错误信息\"}"
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理 Sentinel Block 异常
     * 
     * 功能：拦截 Sentinel 的流控/降级异常，返回友好的 JSON 格式错误信息
     * 
     * 触发场景（在 Sentinel Dashboard 中配置规则）：
     * 1. 流控规则（FlowException）：QPS 超过阈值、线程数超过阈值
     * 2. 降级规则（DegradeException）：RT 过长、异常比例过高、异常数过多
     * 3. 热点参数限流规则（ParamFlowException）：对特定参数值限流
     * 4. 系统保护规则（SystemBlockException）：系统负载过高
     * 5. 授权规则（AuthorityException）：黑白名单控制
     * 
     * 关联：
     * - Sentinel Dashboard：http://localhost:8080（配置流控规则）
     * - 被保护资源：@SentinelResource(value = "createOrder")
     * - 统一响应类：com.atguigu.common.R
     * - 响应输出：HttpServletResponse → PrintWriter
     * 
     * 返回格式（JSON）：
     * {
     *   "code": 500,
     *   "message": "createOrder被Sentinel限制了,原因:FlowException"
     * }
     * 
     * @param Request HttpServletRequest（请求对象，包含 URI、参数等）
     * @param Response HttpServletResponse（响应对象，设置 Content-Type 和输出内容）
     * @param resourceName 被保护的资源名（如 "createOrder"，来自 @SentinelResource 注解）
     * @param e BlockException（具体的 Sentinel 异常类型）
     * @throws Exception 可能的异常（如 JSON 序列化异常）
     */
    @Override
    public void handle(HttpServletRequest Request,
                       HttpServletResponse Response,
                       String resourceName, 
                       BlockException e) throws Exception {
        Response.setStatus(429);//too many request

        // 1. 设置响应内容类型为 JSON，编码为 UTF-8
        // 确保客户端能正确解析返回的 JSON 数据
        Response.setContentType("application/json;charset=utf-8");
        
        // 2. 获取响应输出流
        // 用于向客户端写入响应的 JSON 数据
        PrintWriter writer = Response.getWriter();
        
        //3. 构建统一格式的错误响应
       /*   R.erro() 是项目自定义的统一响应结果类
         code: 500（服务器错误）
         message: 包含资源名和具体异常类型，方便排查
         e.getClass() 可获取具体异常类型（FlowException、DegradeException 等）*/
        R erro = R.erro(500, resourceName + "被Sentinel限制了,原因:" + e.getClass());
        
        // 4. 将 R 对象序列化为 JSON 字符串
        String json = objectMapper.writeValueAsString(erro);
        
        // 5. 将 JSON 字符串写入响应输出流，返回给客户端
        writer.write(json);
        
        // 6. 刷新输出流，确保数据写入
        writer.flush();
        writer.close();
    }
}
