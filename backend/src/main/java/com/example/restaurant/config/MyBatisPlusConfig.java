package com.example.restaurant.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.restaurant.mapper")
public class MyBatisPlusConfig {
}
