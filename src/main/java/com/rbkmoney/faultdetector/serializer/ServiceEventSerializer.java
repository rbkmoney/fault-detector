package com.rbkmoney.faultdetector.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class ServiceEventSerializer implements Serializer<ServiceEvent> {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, ServiceEvent data) {
        try {
            SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter.serializeAllExcept("fieldMetaData");
            FilterProvider filters = new SimpleFilterProvider().addFilter("fault-detector", theFilter);
            return om.writer(filters).writeValueAsString(data).getBytes();
        } catch (Exception e) {
            log.error("Error when serialize service event serializer data: {} ", data, e);
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
