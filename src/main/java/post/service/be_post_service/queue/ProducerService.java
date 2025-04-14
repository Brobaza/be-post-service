package post.service.be_post_service.queue;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import post.service.be_post_service.configuration.Queue.KafkaProperties;

@Service
public class ProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ProducerService(KafkaTemplate<String, String> kafkaTemplate, KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message, String topic) {
        System.out.println("Sending message to topic: " + topic);
        kafkaTemplate.send(topic, message);
    }
}
