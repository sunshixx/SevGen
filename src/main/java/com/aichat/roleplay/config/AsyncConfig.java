package com.aichat.roleplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 异步任务配置类
 * 用于配置异步任务执行器，支持并行处理角色任务
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${async.executor.core-pool-size:5}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:25}")
    private int queueCapacity;

    @Value("${async.executor.thread-name-prefix:RolePlay-Async-}")
    private String threadNamePrefix;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // 设置拒绝策略
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("Task rejected, thread pool is full");
            throw new RuntimeException("Server is busy, please try again later");
        });
        
        executor.initialize();
        return executor;
    }

    @Bean
    public ExecutorService executorService() {
        log.debug("Creating ExecutorService");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // 设置拒绝策略
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("Task rejected, thread pool is full");
            throw new RuntimeException("Server is busy, please try again later");
        });
        
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }
}