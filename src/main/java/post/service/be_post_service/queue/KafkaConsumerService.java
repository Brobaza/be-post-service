package post.service.be_post_service.queue;

import org.aspectj.weaver.ast.Test;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import post.service.be_post_service.dtos.CreateStoryReactionDto;
import post.service.be_post_service.dtos.TestDto;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "#{'${spring.kafka.topic1}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }

    @KafkaListener(topics = "test-post", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listenTestPost(String message) {
        System.out.println("Received message: " + message);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            TestDto testDto = objectMapper.readValue(message, TestDto.class);
            System.out.println("TestDto name: " + testDto.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @KafkaListener(topics = "create-story-reaction", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listenCreateStoryReaction(String message) {
        System.out.println("Received message: " + message);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            CreateStoryReactionDto createStoryReactionDto = objectMapper.readValue(message, CreateStoryReactionDto.class);
            System.out.println("CreateStoryReactionDto: " + createStoryReactionDto);
            System.out.println("User ID: " + createStoryReactionDto.getUserId());
            System.out.println("Story ID: " + createStoryReactionDto.getStoryId());
            System.out.println("Reaction Type: " + createStoryReactionDto.getReactionType());

        } catch (Exception e) {
            e.printStackTrace();
}
    }

    
}