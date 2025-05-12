package post.service.be_post_service.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import post.service.be_post_service.domain.*;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.entity.PostHastag;
import post.service.be_post_service.entity.PostLink;
import post.service.be_post_service.entity.PostUserTag;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreatePostRequest;


@Service
public class PostService {

    private final PostDomain postDomain;
    private final PostHastagDomain postHastagDomain ;
    private final PostLinkDomain postLinkDomain ;
    private final PostUserTagDomain postUserTagDomain ;
    private final PostReactionDomain postReactionDomain ;
    @Autowired
    private GrpcUserService grpcUserService;

    @Autowired
    public PostService(PostDomain postDomain, PostHastagDomain postHastagDomain,
                       PostLinkDomain postLinkDomain, PostUserTagDomain postUserTagDomain, PostReactionDomain postReactionDomain) {
        this.postDomain = postDomain;
        this.postHastagDomain = postHastagDomain;
        this.postLinkDomain = postLinkDomain;
        this.postUserTagDomain = postUserTagDomain;
        this.postReactionDomain = postReactionDomain;
    }
    public Post createPost(CreatePostRequest request) {
        validateRequest(request);

        Post post = new Post();
        post.setContent(request.getContent());
        post.setAuthorId(UUID.fromString(request.getAuthorId()));

        if (request.getPostParentId() != null && !request.getPostParentId().isBlank()) {
            UUID parentId = parseUUID(request.getPostParentId(), "Invalid postParentId");
            Post parentPost = postDomain.getParentPost(parentId);
            if (parentPost == null) {
                throw new IllegalArgumentException("Parent post not found: " + parentId);
            }
            post.setPostParentId(parentId);
        }

        List<UUID> images = safeParseUUIDList(request.getImagesList(), "Invalid image UUID");
        post.setImages(images);
        postDomain.create(post);

        createPostLinks(post.getId(), request.getLinksList());
        createPostHashtags(post.getId(), request.getHashtagsList());
        createPostUserTags(post.getId(), request.getTaggedUserIdsList());
        return post;
    }
    private void validateRequest(CreatePostRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Post content is required");
        }
        if (request.getAuthorId() == null || request.getAuthorId().isBlank()) {
            throw new IllegalArgumentException("Author ID is required");
        }
        parseUUID(request.getAuthorId(), "Invalid authorId");
    }

    private UUID parseUUID(String value, String errorMessage) {
        try {
            return UUID.fromString(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(errorMessage + ": " + value, e);
        }
    }

    private List<UUID> safeParseUUIDList(List<String> list, String errorMessagePrefix) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(s -> parseUUID(s, errorMessagePrefix))
                .collect(Collectors.toList());
    }

    private void createPostLinks(UUID postId, List<String> linksList) {
        if (linksList == null || linksList.isEmpty()) return;
        PostLink postLink = new PostLink();
        postLink.setContent(List.copyOf(linksList));
        postLink.setPostId(postId);
        postLinkDomain.create(postLink);
    }

    private void createPostHashtags(UUID postId, List<String> hashtagsList) {
        if (hashtagsList == null || hashtagsList.isEmpty()) return;
        PostHastag postHashtag = new PostHastag();
        postHashtag.setContent(List.copyOf(hashtagsList));
        postHashtag.setPostId(postId);
        postHastagDomain.create(postHashtag);
    }

    private void createPostUserTags(UUID postId, List<String> taggedUserIds) {
        if (taggedUserIds == null || taggedUserIds.isEmpty()) return;

        for (String userIdStr : taggedUserIds) {
            UUID userId = parseUUID(userIdStr, "Invalid user ID in tagged users");
            if (grpcUserService.getUser(userId.toString()) == null) {
                throw new IllegalArgumentException("Tagged user not found: " + userId);
            }
            PostUserTag postUserTag = new PostUserTag();
            postUserTag.setPost_id(postId);
            postUserTag.setUser_id(userId);
            postUserTagDomain.create(postUserTag);
        }
    }

}
