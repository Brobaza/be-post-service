package post.service.be_post_service.services;

import java.net.URI;
import java.net.URL;
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
import post.service.be_post_service.enums.StoryType;
import post.service.be_post_service.enums.ViewType;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreateStoryRequest;
import post.service.be_post_service.grpc.CreateStoryResponse;
import post.service.be_post_service.grpc.GetListStoryRequest;
import post.service.be_post_service.grpc.GetListStoryResponse;
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

    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            uri.toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateImagesAreUrls(List<String> images) {
        if (images != null) {
            for (String img : images) {
                if (!isValidUrl(img)) {
                    throw new IllegalArgumentException("Invalid image URL: " + img);
                }
            }
        }
    }
    public boolean checkUserExistsById(String userId) {
    try {
        var user = grpcUserService.getUser(userId);
        // Kiểm tra null và kiểm tra id thực sự tồn tại
        return user != null && user.getId() != null && user.getId().equals(userId);
    } catch (Exception e) {
        return false;
    }
}
    public Story createStory(CreateStoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        Story story = new Story();

        story.setAuthorId(UUID.fromString(request.getAuthorId()));
        // Kiểm tra authorId có phải là userId hợp lệ không bằng RPC GetUser
        String authorId = request.getAuthorId();
        if (authorId == null || authorId.isEmpty()) {
            throw new IllegalArgumentException("AuthorId cannot be null or empty");
        }
        try {
            UUID.fromString(authorId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid authorId format, must be UUID", e);
        }        
        boolean userExists = checkUserExistsById(authorId);
        if (!userExists) {
            throw new IllegalArgumentException("AuthorId does not exist in user database");
        }

        List<String> images = request.getImagesList() != null ? request.getImagesList() : Collections.emptyList();
        validateImagesAreUrls(images);
        
        story.setImages(images);
        story.setStoryType(StoryType.valueOf(request.getStoryType()));
        story.setViewType(ViewType.valueOf(request.getViewType()));
        storyDomain.create(story);

        return story;
    }

    public GetListStoryResponse getListStory(GetListStoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        long nowMillis = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000L;
        Date threshold = new Date(nowMillis - oneDayMillis);
        List<Story> stories = storyDomain.getAllActiveStories(new java.sql.Date(threshold.getTime()));

        String userId = request.getUserId();
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        try {
            UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId format, must be UUID", e);
        }
        boolean userExists = checkUserExistsById(userId);
        if (!userExists) {
            throw new IllegalArgumentException("UserId does not exist in user database");
        }
        
        ViewType FRIENDS_ONLY = ViewType.FRIEND_ONLY;
        ViewType PUBLIC = ViewType.PUBLIC;

        // Lọc theo quyền xem story
        List<Story> filteredStories = stories.stream().filter(story -> {
            ViewType viewType = story.getViewType();
            String authorId = story.getAuthorId() != null ? story.getAuthorId().toString() : "";
            if (viewType == PUBLIC) {
                return true;
            }
            if (viewType == FRIENDS_ONLY) {
                return userId != null && !userId.isEmpty() &&
                        grpcUserService.isOnFriendListWithMetadata(authorId, userId).getConfirm();
            }

            return false;
        }).collect(Collectors.toList());

        GetListStoryResponse.Builder responseBuilder = GetListStoryResponse.newBuilder();
        for (Story story : filteredStories) {
            post.service.be_post_service.grpc.Story.Builder storyBuilder = post.service.be_post_service.grpc.Story
                    .newBuilder();
            storyBuilder.setStoryId(story.getId() != null ? story.getId().toString() : "");
            storyBuilder.setAuthorId(story.getAuthorId() != null ? story.getAuthorId().toString() : "");
            if (story.getImages() != null) {
                storyBuilder.addAllImages(story.getImages());
            }
            storyBuilder.setStoryType(story.getStoryType() != null ? story.getStoryType().name() : "");
            storyBuilder.setCreatedAt(story.getCreatedDate() != null ? story.getCreatedDate().toString() : "");
            storyBuilder.setViewType(story.getViewType() != null ? story.getViewType().name() : "");
            responseBuilder.addStory(storyBuilder.build());
        }
        responseBuilder.setMetaData(MetaData.newBuilder()
                .setRespcode("200")
                .setMessage("OK")
                .build());
        return responseBuilder.build();
    }

}
