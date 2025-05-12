package post.service.be_post_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import post.service.be_post_service.domain.*;
import post.service.be_post_service.entity.*;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreateCommentRequest;
import post.service.be_post_service.grpc.CreatePostRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentDomain commentDomain;
    private final CommentHastagDomain commentHastagDomain ;
    private final CommentLinkDomain commentLinkDomain ;
    private final CommentUserTagDomain commentUserTagDomain ;
    private final CommentReactionDomain commentReactionDomain ;
    @Autowired
    private GrpcUserService grpcUserService;
    @Autowired
    private PostDomain postDomain;
    @Autowired
    public CommentService(CommentDomain commentDomain, CommentHastagDomain commentHastagDomain,
                       CommentLinkDomain commentLinkDomain, CommentUserTagDomain commentUserTagDomain , CommentReactionDomain commentReactionDomain) {
        this.commentDomain = commentDomain;
        this.commentHastagDomain = commentHastagDomain;
        this.commentLinkDomain = commentLinkDomain;
        this.commentUserTagDomain = commentUserTagDomain;
        this.commentReactionDomain = commentReactionDomain;
    }
    public Comment createComment(CreateCommentRequest request) {
        validateRequest(request);

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthorId(UUID.fromString(request.getAuthorId()));
        if(postDomain.findOne(UUID.fromString(request.getPostId()))!=null){
        comment.setPostId(UUID.fromString(request.getPostId()));}
        else{
            throw new IllegalArgumentException("Post not found: " );
        }
        if (request.getCommentParentId() != null && !request.getCommentParentId().isBlank()) {
            UUID parentId = parseUUID(request.getCommentParentId(), "Invalid commentParentId");
            Comment commentPost = commentDomain.getParentComment(parentId);
            if (commentPost == null) {
                throw new IllegalArgumentException("Parent comment not found: " + parentId);
            }
            comment.setCommentParentId(parentId);
        }

        List<UUID> images = safeParseUUIDList(request.getImagesList(), "Invalid image UUID");
        comment.setImages(images);
        commentDomain.create(comment);

        createCommentLinks(comment.getId(), request.getLinksList());
        createCommentHashtags(comment.getId(), request.getHashtagsList());
        createCommentUserTags(comment.getId(), request.getTaggedUserIdsList());
        return comment;
    }
    private void validateRequest(CreateCommentRequest request) {
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

    private void createCommentLinks(UUID commentId,List<String> linksList) {
        if (linksList == null || linksList.isEmpty()) return;
        CommentLink commentLink = new CommentLink();
        commentLink.setContent(List.copyOf(linksList));
        commentLink.setCommentId(commentId);
        commentLinkDomain.create(commentLink);
    }

    private void createCommentHashtags(UUID commentId, List<String> hashtagsList) {
        if (hashtagsList == null || hashtagsList.isEmpty()) return;
        CommentHastag commentHashtag = new CommentHastag();
        commentHashtag.setContent(List.copyOf(hashtagsList));
        commentHashtag.setCommentId(commentId);
        commentHastagDomain.create(commentHashtag);
    }

    private void createCommentUserTags(UUID commentId, List<String> taggedUserIds) {
        if (taggedUserIds == null || taggedUserIds.isEmpty()) return;

        for (String userIdStr : taggedUserIds) {
            UUID userId = parseUUID(userIdStr, "Invalid user ID in tagged users");
            if (grpcUserService.getUser(userId.toString()) == null) {
                throw new IllegalArgumentException("Tagged user not found: " + userId);
            }
            CommentUserTag commentUserTag = new CommentUserTag();
            commentUserTag.setComment_id(commentId);
            commentUserTag.setUser_id(userId);
            commentUserTagDomain.create(commentUserTag);
        }
    }
}
