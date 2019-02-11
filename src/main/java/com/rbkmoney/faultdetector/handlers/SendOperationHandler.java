package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendOperationHandler implements Handler<ServiceEvent> {

    private final Producer<String, ServiceEvent> producer;

    @Override
    public void handle(ServiceEvent serviceEvent) throws Exception {
        try {
            String topic = serviceEvent.getServiceId();
            String key = serviceEvent.getRequestId();
            ProducerRecord<String, ServiceEvent> producerRecord = new ProducerRecord<>(topic, key, serviceEvent);
            producer.send(producerRecord).get();
            producer.close();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Exception while adding new ServiceEvent to kafka", e);
            throw new Exception(e);
        }
    }

}
