package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.ServiceOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendOperationHandler implements Handler<ServiceOperation> {

    @Autowired
    private KafkaTemplate<String, ServiceOperation> kafkaTemplate;

    @Value("${kafka.topic}")
    private String topicName;

    @Override
    public void handle(ServiceOperation serviceOperation) throws Exception {
        kafkaTemplate.send(topicName, serviceOperation);
    }

}
