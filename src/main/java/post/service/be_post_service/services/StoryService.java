package post.service.be_post_service.services;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import post.service.be_post_service.base.BaseService;
import post.service.be_post_service.domain.CommentStoryDomain;
import post.service.be_post_service.domain.StoryDomain;
import post.service.be_post_service.domain.StoryReactionDomain;
import post.service.be_post_service.entity.CommentStory;
import post.service.be_post_service.entity.Story;
import post.service.be_post_service.entity.StoryReaction;
import post.service.be_post_service.enums.ReactionType;
import post.service.be_post_service.enums.StoryType;
import post.service.be_post_service.enums.ViewType;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreateCommentStoryRequest;
import post.service.be_post_service.grpc.CreateCommentStoryResponse;
import post.service.be_post_service.grpc.CreateStoryReactionRequest;
import post.service.be_post_service.grpc.CreateStoryReactionResponse;
import post.service.be_post_service.grpc.CreateStoryRequest;
import post.service.be_post_service.grpc.CreateStoryResponse;
import post.service.be_post_service.grpc.GetListStoryReactionResponse;
import post.service.be_post_service.grpc.GetListStoryReactionResponse;
import post.service.be_post_service.grpc.GetListStoryRequest;
import post.service.be_post_service.grpc.GetListStoryResponse;
import post.service.be_post_service.grpc.MetaData;
import post.service.be_post_service.grpc.ReactionStory;

@Service
public class StoryService {

    private final StoryDomain storyDomain;
    private final StoryReactionDomain storyReactionDomain;
    private final CommentStoryDomain commentStoryDomain;

    @Autowired
    private GrpcUserService grpcUserService;

    @Autowired
    public StoryService(StoryDomain storyDomain,
            StoryReactionDomain storyReactionDomain,
            CommentStoryDomain commentStoryDomain) {
        this.storyDomain = storyDomain;
        this.storyReactionDomain = storyReactionDomain;
        this.commentStoryDomain = commentStoryDomain;
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
            System.out.println("Checking user exists by ID: " + userId);
            var user = grpcUserService.getUser(userId);
            // Kiểm tra null và kiểm tra id thực sự tồn tại
            System.out.println("User found: " + user);
            return user != null && !user.getId().isEmpty() && user.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    private void validateUserId(String userId) {
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

    public MetaData deleteStory(post.service.be_post_service.grpc.DeleteStoryRequest request) {
        String storyId = request.getStoryId();
        String userId = request.getAuthorId();

        // Validate userId
        // validateUserId(userId);

        // Validate storyId
        if (storyId == null || storyId.isEmpty()) {
            throw new IllegalArgumentException("StoryId cannot be null or empty");
        }
        try {
            UUID.fromString(storyId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid storyId format, must be UUID", e);
        }

        // Kiểm tra quyền xóa
        Story story = storyDomain.findOneOrFail(UUID.fromString(storyId));
        if (!story.getAuthorId().toString().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to delete this story");
        }

        // Xóa mềm
        storyDomain.destroyById(UUID.fromString(storyId));

        return MetaData.newBuilder()
                .setRespcode("200")
                .setMessage("Story deleted successfully")
                .build();
    }

    public Story updateStory(String storyId, String userId, String viewType) {
        validateUserId(userId);
        if (storyId == null || storyId.isEmpty()) {
            throw new IllegalArgumentException("StoryId cannot be null or empty");
        }
        try {
            UUID.fromString(storyId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid storyId format, must be UUID", e);
        }
        Story existingStory = storyDomain.findOneOrFail(UUID.fromString(storyId));
        if (!existingStory.getAuthorId().toString().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to update this story");
        }
        if (viewType == null || viewType.isEmpty()) {
            throw new IllegalArgumentException("ViewType cannot be null or empty");
        }
        existingStory.setViewType(ViewType.valueOf(viewType));

        return storyDomain.saveOrUpdate(existingStory);
    }

    public CreateCommentStoryResponse createCommentStory(
            CreateCommentStoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        String storyId = request.getStoryId();
        String userId = request.getAuthorId();
        String content = request.getContent();

        // Validate storyId
        if (storyId == null || storyId.isEmpty()) {
            throw new IllegalArgumentException("StoryId cannot be null or empty");
        }
        try {
            UUID.fromString(storyId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid storyId format, must be UUID", e);
        }

        // Validate userId
        validateUserId(userId);

        // Validate content
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        Story story = storyDomain.findOneOrFail(UUID.fromString(storyId));
        if (story == null) {
            throw new IllegalArgumentException("Story does not exist");
        }

        CommentStory commentEntity = new CommentStory();
        commentEntity.setStoryId(UUID.fromString(storyId));
        commentEntity.setAuthorId(UUID.fromString(userId));
        commentEntity.setContent(content);
        commentStoryDomain.create(commentEntity);

        return CreateCommentStoryResponse.newBuilder()
                .setStoryId(storyId)
                .setAuthorId(userId)
                .setContent(content)
                .setMetaData(MetaData.newBuilder()
                        .setRespcode("200")
                        .setMessage("OK")
                        .build())
                .build();
    }

    public CreateStoryReactionResponse createStoryReaction(
            CreateStoryReactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        String storyId = request.getStoryId();
        String userId = request.getUserId();
        String reactionType = request.getReactionType();

        // Validate storyId
        if (storyId == null || storyId.isEmpty()) {
            throw new IllegalArgumentException("StoryId cannot be null or empty");
        }
        try {
            UUID.fromString(storyId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid storyId format, must be UUID", e);
        }

        // Validate userId
        validateUserId(userId);

        // Validate reactionType
        if (reactionType == null || reactionType.isEmpty()) {
            throw new IllegalArgumentException("ReactionType cannot be null or empty");
        }
        ReactionType type;
        try {
            type = ReactionType.valueOf(reactionType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reactionType value", e);
        }

        Story story = storyDomain.findOne(UUID.fromString(storyId));
        if (story == null) {
            throw new IllegalArgumentException("Story does not exist");
        }

        StoryReaction reaction = new StoryReaction();
        reaction.setStoryId(UUID.fromString(storyId));
        reaction.setUserId(UUID.fromString(userId));
        reaction.setReactionType(type);
        storyReactionDomain.create(reaction);

        return post.service.be_post_service.grpc.CreateStoryReactionResponse.newBuilder()

                .setMetaData(MetaData.newBuilder()
                        .setRespcode("200")
                        .setMessage("OK")
                        .build())
                .build();
    }

    public List<StoryReaction> getListStoryReaction(
            post.service.be_post_service.grpc.GetListStoryReactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        String storyId = request.getStoryId();
        String authorId = request.getUserId();
        validateUserId(authorId);
        if (storyId == null || storyId.isEmpty()) {
            throw new IllegalArgumentException("StoryId cannot be null or empty");
        }
        if (authorId == null || authorId.isEmpty()) {
            throw new IllegalArgumentException("AuthorId cannot be null or empty");
        }
        try {
            UUID.fromString(storyId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid storyId format, must be UUID", e);
        }
        try {
            UUID.fromString(authorId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid authorId format, must be UUID", e);
        }

        Story story = storyDomain.findOneOrFail(UUID.fromString(storyId));
        if (story == null) {
            throw new IllegalArgumentException("Story does not exist");
        }
        if (!story.getAuthorId().toString().equals(authorId)) {
            throw new IllegalArgumentException("AuthorId is not the author of this story");
        }

        List<StoryReaction> reactions = storyReactionDomain.getStoryReactionByStoryId(UUID.fromString(storyId));

        GetListStoryReactionResponse.Builder responseBuilder = GetListStoryReactionResponse.newBuilder();

        for (StoryReaction reaction : reactions) {
            ReactionStory.Builder reactionBuilder = ReactionStory.newBuilder();
            reactionBuilder.setUserId(reaction.getUserId() != null ? reaction.getUserId().toString() : "");
            reactionBuilder.setStoryId(reaction.getStoryId() != null ? reaction.getStoryId().toString() : "");
            reactionBuilder
                    .setReactionType(reaction.getReactionType() != null ? reaction.getReactionType().name() : "");
            responseBuilder.addReactionStories(reactionBuilder.build());
        }

        List<StoryReaction> responseList = new ArrayList<>();
        responseList.addAll(reactions);
        return responseList;
    }
}
