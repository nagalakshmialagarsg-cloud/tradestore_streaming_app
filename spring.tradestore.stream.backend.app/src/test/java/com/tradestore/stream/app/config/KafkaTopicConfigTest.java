package com.tradestore.stream.app.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "spring.kafka.topic.name=test-topic")
class KafkaTopicConfigTest {

    @Autowired
    private KafkaTopicConfig kafkaTopicConfig;

    @Autowired
    private NewTopic newTopic;

    @Test
    void contextLoadsAndTopicIsAvailable() {
        assertThat(kafkaTopicConfig).isNotNull();
        assertThat(newTopic).isNotNull();
        assertThat(newTopic.name()).isEqualTo("test-topic");
    }
}
