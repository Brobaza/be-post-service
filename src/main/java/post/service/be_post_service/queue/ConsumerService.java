package post.service.be_post_service.queue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    public ConsumerService() {
        System.out.println("✅ ConsumerService initialized");
    }

    @KafkaListener(topics = "topic1")
    public void consume(String message) {
        System.out.println("💬 Received from Kafka: " + message);
        if (message == null || message.isEmpty()) {
            System.err.println("⚠️ Received an empty or null message.");
        }
    }
}
