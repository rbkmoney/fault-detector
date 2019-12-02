package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceOperations;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class AppConfig {

    private final Map<String, ServiceAggregates> aggregatesMap = new ConcurrentHashMap<>();

    private final Map<String, ServiceSettings> serviceSettingsMap = new ConcurrentHashMap<>();

    private final ServicePreAggregates servicePreAggregates = new ServicePreAggregates();

    private final ServiceOperations serviceOperations = new ServiceOperations();

    @Value("${operations.schedulerPoolSize}")
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
