package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceOperations;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import io.micrometer.core.instrument.Clock;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Bean
    @ConditionalOnMissingBean
    public GraphiteConfig fdGraphiteConfig(GraphiteProperties graphiteProperties) {
        return new GraphitePropertiesConfigAdapter(graphiteProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphiteMeterRegistry fdGraphiteMeterRegistry(GraphiteConfig fdGraphiteConfig,
                                                         Clock clock) {
        try {
            return new GraphiteMeterRegistry(fdGraphiteConfig, clock);
        } catch (Exception ex) {
            log.error("Received error in GraphiteMeterRegistry: ", ex);
            throw ex;
        }
    }


}
