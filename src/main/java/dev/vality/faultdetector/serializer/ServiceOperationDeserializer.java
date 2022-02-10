package dev.vality.faultdetector.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.faultdetector.data.ServiceOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

@Slf4j
public class ServiceOperationDeserializer implements Deserializer<ServiceOperation> {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public ServiceOperation deserialize(String topic, byte[] data) {
        try {
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return om.readValue(data, ServiceOperation.class);
        } catch (Exception e) {
            log.error("Error when deserialize service operation data: {} ", data, e);
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