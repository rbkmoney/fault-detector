package dev.vality.faultdetector.config;

import dev.vality.faultdetector.data.ServiceAggregates;
import dev.vality.faultdetector.data.ServiceOperations;
import dev.vality.faultdetector.data.ServicePreAggregates;
import dev.vality.faultdetector.data.ServiceSettings;
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

    @Value("${operations.schedulerPoolSize}")
    private int schedulerPoolSize;

    @Bean
    public Map<String, ServiceAggregates> aggregatesMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<String, ServiceSettings> serviceConfigMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ServicePreAggregates servicePreAggregates() {
        return new ServicePreAggregates();
    }

    @Bean
    public ServiceOperations serviceOperations() {
        return new ServiceOperations();
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(schedulerPoolSize);
        return taskScheduler;
    }

}
