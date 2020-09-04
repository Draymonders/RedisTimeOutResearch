package com.yitu.sturnus.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步线程池配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

  @Value("${system.taskExecutor.corePoolSize}")
  private int sysCorePoolSize;

  @Value("${system.taskExecutor.maxPoolSize}")
  private int sysMaxPoolSize;

  @Value("${system.taskExecutor.queueCapacity}")
  private int sysQueueCapacity;

  @Bean(name = "threadPoolTaskExecutor")
  public Executor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(sysCorePoolSize);
    executor.setMaxPoolSize(sysMaxPoolSize);
    executor.setQueueCapacity(sysQueueCapacity);
    executor.setRejectedExecutionHandler(new CallerRunsPolicy());
    executor.initialize();
    return executor;
  }
}

