package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendOperationHandler implements Handler<ServiceEvent> {

    @Autowired
    private KafkaTemplate<String, ServiceEvent> kafkaTemplate;

    @Value("${kafka.topic}")
    private String topicName;

    @Override
    public void handle(ServiceEvent serviceEvent) throws Exception {
        kafkaTemplate.send(topicName, serviceEvent);
    }

}
