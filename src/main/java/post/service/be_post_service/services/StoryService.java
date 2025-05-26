package post.service.be_post_service.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import post.service.be_post_service.base.BaseService;
import post.service.be_post_service.domain.StoryDomain;
import post.service.be_post_service.entity.Story;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreateStoryRequest;
import post.service.be_post_service.grpc.CreateStoryResponse;
import post.service.be_post_service.grpc.GetListStoryRequest;
import post.service.be_post_service.grpc.MetaData;

@Service
public class StoryService {

    private final StoryDomain storyDomain;

    @Autowired
    private GrpcUserService grpcUserService;

    @Autowired
    public StoryService(StoryDomain storyDomain) {
        this.storyDomain = storyDomain;
    }

    public Story createStory(CreateStoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        Story story = new Story();

        story.setAuthorId(UUID.fromString(request.getAuthorId()));

        List<String> images = request.getImagesList() != null ? request.getImagesList() : Collections.emptyList();
        story.setImages(images);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date expiredAt = sdf.parse(request.getExpiredAt());
            if (expiredAt.before(new Date())) {
                throw new IllegalArgumentException("ExpiredAt must be in the future");
            }
            story.setExpiredAt(new java.sql.Date(expiredAt.getTime()));
        } catch (java.text.ParseException e) {
            throw new IllegalArgumentException("Invalid date format for expiredAt", e);
        }

        story.setStoryType(request.getStoryType());
        storyDomain.create(story);

        return story;
    }

    public List<CreateStoryResponse> getListStory(GetListStoryRequest request) {
        
        List<Story> stories = storyDomain.getAllActiveStories(new java.sql.Date(new Date().getTime()));

        List<CreateStoryResponse> responses = new java.util.ArrayList<>();
        for (Story story : stories) {
            CreateStoryResponse response = CreateStoryResponse.newBuilder()
                    .setAuthorId(story.getAuthorId() != null ? story.getAuthorId().toString() : "")
                    .addAllImages(story.getImages() != null ? story.getImages() : Collections.emptyList())
                    .setStoryType(story.getStoryType() != null ? story.getStoryType() : "")
                    .setExpiredAt(story.getExpiredAt() != null ? story.getExpiredAt().toString() : "")
                    .setMetaData(MetaData.newBuilder()
                            .setRespcode("200")
                            .setMessage("OK")
                            .build())
                    .build();
            responses.add(response);
        }
        return responses;
    }
}
