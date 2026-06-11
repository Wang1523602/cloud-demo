package com.gtguigu.geteway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Component
public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //每次响应前,返添加一个一次性令牌,支持uuid,jwt等各种格式
                return chain.filter(exchange).then(Mono.fromRunnable(()->{
                    ServerHttpResponse response = exchange.getResponse();
                    HttpHeaders headers = response.getHeaders();
                    String value = config.getValue();
                    if("uuid".equalsIgnoreCase( value)){
                        value= UUID.randomUUID().toString();
                    }
                    if("jwt".equalsIgnoreCase( value)){
                        value="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNzE4MTQzNjAwLCJleHAiOjE3MTgxNDcyMDAsInJvbGUiOiJ1c2VyIiwiaWQiOjEsImVtYWwiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.q3D8pU9M1z8x9k2Q7s6a5f4g3h2j1l0k9j8h7g6f5d4s3a2s1d";
                    }
                    headers.add(config.getName(),value);
                }));
            }
        };
    }
}
