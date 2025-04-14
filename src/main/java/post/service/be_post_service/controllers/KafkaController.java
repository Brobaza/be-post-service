package post.service.be_post_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import post.service.be_post_service.enums.QueueTopics;
import post.service.be_post_service.queue.ProducerService;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private final ProducerService producerService;

    public KafkaController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message) {
        producerService.sendMessage(message, QueueTopics.TOPIC1_TOPIC);
        return ResponseEntity.ok("Message sent to Kafka: " + message);
    }
}
