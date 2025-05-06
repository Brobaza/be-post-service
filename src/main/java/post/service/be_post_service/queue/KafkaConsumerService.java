package post.service.be_post_service.queue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "#{'${spring.kafka.topic1}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}
