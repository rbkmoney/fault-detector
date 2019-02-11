package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.ServiceAvailability;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Map'ы сделаны только в рамках MVP. далее будет переведено на kafka
@Configuration
public class AppConfig {

    private final Map<String, ServiceAvailability> availabilityMap = new ConcurrentHashMap<>();

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap = new ConcurrentHashMap<>();

    @Bean
    public Map<String, ServiceAvailability> availabilityMap() {
        return availabilityMap;
    }

    @Bean
    public Map<String, Map<String, ServiceEvent>> serviceEventMap() {
        return serviceEventMap;
    }

}
