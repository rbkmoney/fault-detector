package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    private final Map<String, ServiceAggregates> aggregatesMap = new ConcurrentHashMap<>();

    private final Map<String, ServiceSettings> serviceSettingsMap = new ConcurrentHashMap<>();

    private final ServicePreAggregates servicePreAggregates = new ServicePreAggregates();

    private final ServiceOperations serviceOperations = new ServiceOperations();

    @Value("${operations.scheduler-pool-size}")
    private int schedulerPoolSize;

    @Bean
    public Map<String, ServiceAggregates> aggregatesMap() {
        return aggregatesMap;
    }

    @Bean
    public Map<String, ServiceSettings> serviceConfigMap() {
        return serviceSettingsMap;
    }

    @Bean
    public ServicePreAggregates servicePreAggregates() {
        return servicePreAggregates;
    }

    @Bean
    public ServiceOperations serviceOperations() {
        return serviceOperations;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(schedulerPoolSize);
        return taskScheduler;
    }

}
