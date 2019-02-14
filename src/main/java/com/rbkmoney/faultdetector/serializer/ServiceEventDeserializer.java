package com.rbkmoney.faultdetector.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

@Slf4j
public class ServiceEventDeserializer implements Deserializer<ServiceEvent> {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public ServiceEvent deserialize(String topic, byte[] data) {
        try {
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return om.readValue(data, ServiceEvent.class);
        } catch (Exception e) {
            log.error("Error when deserialize service event data: {} ", data, e);
            return null;
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

}