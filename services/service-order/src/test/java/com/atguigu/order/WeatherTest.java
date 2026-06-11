package com.atguigu.order;

import com.atguigu.order.feign.WeatherFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WeatherTest {
    @Autowired
    WeatherFeignClient weatherFeignClient;
    @Test
    void test01(){
        String weather = weatherFeignClient.getWeather(
                " application/x-www-form-urlencoded",
                " 9d7eb7d6cd3062293012e5668317a190",
                " 南阳"
        );
        System.out.println("weather=" + weather);
    }
}
