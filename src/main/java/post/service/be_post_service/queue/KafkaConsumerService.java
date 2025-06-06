package post.service.be_post_service.queue;

import org.aspectj.weaver.ast.Test;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import post.service.be_post_service.dtos.*;

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
    @KafkaListener(topics = "#{'${spring.kafka.create-comment}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listenCreateComment(String message) {
        System.out.println("Received message: " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CreateCommentRequestDto createComment = objectMapper.readValue(message, CreateCommentRequestDto.class);
            System.out.println("CreateCommentRequestDto: " + createComment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @KafkaListener(topics = "#{'${spring.kafka.reaction-post}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listenReactionPost(String message) {
        System.out.println("Received message: " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CreatePostReactionRequestDto reactionPost = objectMapper.readValue(message, CreatePostReactionRequestDto.class);
            System.out.println("CreatePostReactionRequestDto: " + reactionPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @KafkaListener(topics = "#{'${spring.kafka.reaction-comment}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listenReactionComment(String message) {
        System.out.println("Received message: " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CreateCommentReactionRequestDto reactionComment = objectMapper.readValue(message, CreateCommentReactionRequestDto.class);
            System.out.println("CreateCommentReactionRequestDto: " + reactionComment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @KafkaListener(topics = "#{'${spring.kafka.mention-comment}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")
    public void listenMentionComment(String message) {
        System.out.println("Received mention message: " + message);
    }
}
