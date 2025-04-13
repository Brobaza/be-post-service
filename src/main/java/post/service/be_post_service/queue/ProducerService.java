package post.service.be_post_service.queue;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import post.service.be_post_service.configuration.Queue.KafkaProperties;

@Service
public class ProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public ProducerService(KafkaTemplate<String, String> kafkaTemplate, KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public void sendMessage(String message) {
        String topic = kafkaProperties.getTopics().get(0).getName();
        System.out.println("Sending message to topic: " + topic);
        kafkaTemplate.send(topic, message);
    }
}
