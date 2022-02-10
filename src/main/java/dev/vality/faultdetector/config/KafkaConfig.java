package dev.vality.faultdetector.config;

import dev.vality.faultdetector.data.ServiceOperation;
import dev.vality.faultdetector.serializer.ServiceOperationDeserializer;
import dev.vality.faultdetector.serializer.ServiceOperationSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    @Value("${kafka.max-pool-records}")
    private String maxPoolRecords;

    @Value("${kafka.fetch-min-bytes}")
    private String fetchMinBytes;

    @Value("${kafka.fetch-max-wait-ms}")
    private String fetchMaxWaitMs;

    @Value("${kafka.ssl.truststore.location-config}")
    private String sslTruststoreLocationConfig;

    @Value("${kafka.ssl.truststore.password-config}")
    private String sslTruststorePasswordConfig;

    @Value("${kafka.ssl.truststore.type}")
    private String sslTruststoreType;

    @Value("${kafka.ssl.keystore.location-config}")
    private String sslKeystoreLocationConfig;

    @Value("${kafka.ssl.keystore.password-config}")
    private String sslKeystorePasswordConfig;

    @Value("${kafka.ssl.keystore.type}")
    private String sslKeystoreType;

    @Value("${kafka.ssl.key.password-config}")
    private String sslKeyPasswordConfig;

    @Value("${kafka.ssl.enable}")
    private boolean sslEnable;

    @Value("${kafka.consumer.concurrency}")
    private int concurrency;

    @Bean
    public ProducerFactory<String, ServiceOperation> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ServiceOperationSerializer.class);

        if (sslEnable) {
            addSslKafkaProps(configProps);
        }

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
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ServiceOperationDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPoolRecords);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinBytes);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs);

        if (sslEnable) {
            addSslKafkaProps(props);
        }

        return new DefaultKafkaConsumerFactory<>(props);
    }

    private void addSslKafkaProps(Map<String, Object> props) {
        // configure the following three settings for SSL Encryption/Decryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
        // The truststore stores all the certificates that the machine should trust
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, new File(sslTruststoreLocationConfig).getAbsolutePath());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePasswordConfig);
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, sslTruststoreType);

        // The keystore stores each machine's own identity
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, new File(sslKeystoreLocationConfig).getAbsolutePath());
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePasswordConfig);
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, sslKeystoreType);

        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPasswordConfig);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ServiceOperation> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ServiceOperation> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(serviceOperationConsumerFactory());
        factory.setBatchListener(false);
        factory.setConcurrency(concurrency);
        return factory;
    }

}