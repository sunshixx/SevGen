package com.aichat.roleplay.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 */
@Configuration
@MapperScan("com.aichat.roleplay.mapper")
public class MyBatisPlusConfig {

    // 移除分页插件配置，简化配置
}