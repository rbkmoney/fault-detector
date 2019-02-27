package com.rbkmoney.faultdetector.config;

import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.serializer.ServiceOperationDeserializer;
import com.rbkmoney.faultdetector.serializer.ServiceOperationSerializer;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaConfig {

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

    @Bean
    public Producer<String, ServiceOperation> kafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ServiceOperationSerializer.class);
        return new KafkaProducer<>(props);
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public ProducerFactory<String, ServiceOperation> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
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
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ServiceOperation> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ServiceOperation> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(serviceOperationConsumerFactory());
        factory.setConcurrency(1);
        factory.setBatchListener(true);
        factory.getContainerProperties().setPollTimeout(5000);
        return factory;
    }

}