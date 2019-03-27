package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.serializer.ServiceOperationDeserializer;
import com.rbkmoney.faultdetector.serializer.ServiceOperationSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@TestConfiguration
@TestPropertySource("target/classes/application.yml")
@Profile({ "test" })
public class KafkaTestConfig {

    private static final String EARLIEST = "earliest";

    @Value("${kafka.bootstrap.servers}")
    private String servers;

    @Value("${kafka.client.id}")
    private String clientId;

    @Value("${kafka.group.id}")
    private String groupId;

    @Value("${kafka.topic}")
    private String topicName;

    @Value("${kafka.poll.timeout}")
    private String pollTimeout;

    @Value("${kafka.max-pool-records}")
    private String maxPoolRecords;

    @Value("${kafka.fetch-min-bytes}")
    private String fetchMinBytes;

    @Value("${kafka.fetch-max-wait-ms}")
    private String fetchMaxWaitMs;

    @Bean
    public ProducerFactory<String, ServiceOperation> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ServiceOperationSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ServiceOperation> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, ServiceOperation> serviceOperationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ServiceOperationDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPoolRecords);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinBytes);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ServiceOperation> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ServiceOperation> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(serviceOperationConsumerFactory());
        factory.setBatchListener(true);
        return factory;
    }

}
