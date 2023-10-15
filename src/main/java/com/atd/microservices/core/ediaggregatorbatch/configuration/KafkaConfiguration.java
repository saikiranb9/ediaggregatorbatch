package com.atd.microservices.core.ediaggregatorbatch.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.atd.microservices.core.ediaggregatorbatch.domain.EDIDocGroupedResult;


@EnableKafka
@Configuration
public class KafkaConfiguration {
	
	@Value("${spring.application.name:creditcardtransactions}")
	private String applicationName;

	@Autowired
	private KafkaConfigConstants kafkaConfigConstants;
	
	@Bean
    public ProducerFactory<String, EDIDocGroupedResult> producerFactory() {
		Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigConstants.BOOTSTRAP_SERVER_URL);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, applicationName);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put("security.protocol", kafkaConfigConstants.KAFKA_SECURITY_PROTOCOL);
        if (kafkaConfigConstants.SSL_TRUSTSTORE_LOCATION != null && !StringUtils.isBlank(kafkaConfigConstants.SSL_TRUSTSTORE_LOCATION)) {
            config.put("ssl.truststore.location", kafkaConfigConstants.SSL_TRUSTSTORE_LOCATION);
        }
        config.put("ssl.truststore.password", kafkaConfigConstants.SSL_TRUSTSTORE_PASSWORD);
        config.put("ssl.endpoint.identification.algorithm", null);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean("ediAggregatorKafkaTemplate")
    public KafkaTemplate<String, EDIDocGroupedResult> ediAggregatorKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
