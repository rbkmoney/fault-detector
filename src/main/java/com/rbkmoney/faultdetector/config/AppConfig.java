package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

    private final Map<String, ServiceAggregates> aggregatesMap = new ConcurrentHashMap<>();

    private final Map<String, Map<String, ServiceEvent>> serviceMap = new ConcurrentHashMap<>();

    private final Map<String, ServiceSettings> serviceSettingsMap = new ConcurrentHashMap<>();

    private final Map<String, Map<Long, ServicePreAggregates>> servicePreAggregatesMap = new ConcurrentHashMap<>();

    @Bean
    public Map<String, ServiceAggregates> aggregatesMap() {
        return aggregatesMap;
    }

    @Bean
    public Map<String, Map<String, ServiceEvent>> servicesMap() {
        return serviceMap;
    }

    @Bean
    public Map<String, ServiceSettings> serviceConfigMap() {
        return serviceSettingsMap;
    }

    @Bean
    public Map<String, Map<Long, ServicePreAggregates>> servicePreAggregatesMap() {
        return servicePreAggregatesMap;
    }

}
