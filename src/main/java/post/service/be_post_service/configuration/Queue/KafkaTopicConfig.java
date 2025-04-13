package post.service.be_post_service.configuration.Queue;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaTopicConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public List<NewTopic> createTopics() {
        return kafkaProperties.getTopics().stream()
                .map(topic -> TopicBuilder.name(topic.getName())
                        .partitions(topic.getPartitions())
                        .replicas(topic.getReplicationFactor())
                        .build())
                .collect(Collectors.toList());
    }
}
