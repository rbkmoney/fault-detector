package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceOperations;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import io.micrometer.core.instrument.Clock;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdPropertiesConfigAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.micrometer.shaded.io.netty.util.internal.StringUtil.EMPTY_STRING;

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
    public StatsdConfig fdStatsdConfig(StatsdProperties statsdProperties) {
        String host = statsdProperties == null ? EMPTY_STRING : statsdProperties.getHost();
        Integer port = statsdProperties == null ? 0 : statsdProperties.getPort();
        log.info("Create new StatsdConfig by StatsdProperties for host {} and port {}", host, port);
        return new StatsdPropertiesConfigAdapter(statsdProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatsdMeterRegistry fdStatsdMeterRegistry(StatsdConfig fdStatsdConfig,
                                                     Clock clock) {
        try {
            log.info("Create new StatsdMeterRegistry by StatsdConfig: {}", fdStatsdConfig);
            return new StatsdMeterRegistry(fdStatsdConfig, clock);
        } catch (Exception ex) {
            log.error("Received error in StatsdMeterRegistry", ex);
            throw ex;
        }
    }

}
