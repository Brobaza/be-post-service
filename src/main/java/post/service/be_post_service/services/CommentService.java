package post.service.be_post_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import post.service.be_post_service.domain.*;
import post.service.be_post_service.entity.*;
import post.service.be_post_service.enums.ReactionType;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreateCommentReactionRequest;
import post.service.be_post_service.grpc.CreateCommentRequest;
import post.service.be_post_service.grpc.UpdateCommentRequest;

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
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        validateRequest(request.getContent(), request.getAuthorId());
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthorId(UUID.fromString(request.getAuthorId()));
        Post post = postDomain.findOne(UUID.fromString(request.getPostId()));
        if(post!=null){
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

        List<String> images = request.getImagesList() != null ? request.getImagesList() : Collections.emptyList();
        comment.setImages(images);
        commentDomain.create(comment);

        createCommentLinks(comment.getId(), request.getLinksList());
        createCommentHashtags(comment.getId(), request.getHashtagsList());
        createCommentUserTags(comment.getId(), request.getTaggedUserIdsList());
        return comment;
    }
    public Comment updateComment(UpdateCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        validateRequest(request.getContent(), request.getAuthorId());
        Comment comment = commentDomain.findOne(UUID.fromString(request.getCommentId()));
        comment.setContent(request.getContent());
        comment.setAuthorId(UUID.fromString(request.getAuthorId()));
        if(postDomain.findOne(UUID.fromString(request.getPostId()))!=null && comment.getPostId().equals(UUID.fromString(request.getPostId()))){
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
            if(comment.getPostId()!=commentPost.getPostId()){
                throw new IllegalArgumentException("PostId of comment and commentParentId not match: " + parentId);
            }
            comment.setCommentParentId(parentId);
        }

        List<String> images = request.getImagesList() != null ? request.getImagesList() : Collections.emptyList();
        comment.setImages(images);
        commentDomain.saveOrUpdate(comment);

        updateCommentLinks(comment.getId(), request.getLinksList());
        updateCommentHashtags(comment.getId(), request.getHashtagsList());
        updateCommentUserTags(comment.getId(), request.getTaggedUserIdsList());
        return comment;
    }
    public List<Comment> getListCommentByPostId(UUID postID){
        List<Comment> listComment=commentDomain.getByPostId(postID);
        for(Comment comment:listComment){
            CommentLink links = commentLinkDomain.getByCommentId(comment.getId());
            CommentHastag hashtags = commentHastagDomain.getByCommentId(comment.getId());
            List<CommentUserTag> userTags = commentUserTagDomain.getByCommentId(comment.getId());
            comment.setCommentLinks(links);
            comment.setUserTags(userTags);
            comment.setHashtags(hashtags);
        }
        return listComment;
    }
    public void createCommentReaction(CreateCommentReactionRequest request){
        Comment comment =commentDomain.findOne(UUID.fromString(request.getCommentId()));
        if(comment==null){
            throw new IllegalArgumentException("Comment not found: " );
        }
        List<UUID> listReaction=comment.getLikedUserIds();
        int likeCount=comment.getLikeCount();
        if(listReaction.contains(UUID.fromString(request.getUserId()))){
            return;
        }
        else{
            likeCount =+1;
            listReaction.add(UUID.fromString(request.getUserId()));
            comment.setLikeCount(likeCount);
            comment.setLikedUserIds(listReaction);
            commentDomain.saveOrUpdate(comment);
            CommentReaction commentReaction=new CommentReaction();
            commentReaction.setCommentId(UUID.fromString(request.getCommentId()));
            commentReaction.setUserId(UUID.fromString(request.getUserId()));
            commentReaction.setReactionType(ReactionType.valueOf(request.getReactionType()));
            commentReactionDomain.create(commentReaction);
        }
    }
    private void validateRequest(String content, String authorId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content is required");
        }
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("Author ID is required");
        }
        parseUUID(authorId, "Invalid authorId");
    }

    private UUID parseUUID(String value, String errorMessage) {
        try {
            return UUID.fromString(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(errorMessage + ": " + value, e);
        }
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
    private void updateCommentLinks(UUID commentId,List<String> linksList) {
        if (linksList == null || linksList.isEmpty()) return;
        CommentLink commentLink = commentLinkDomain.getByCommentId(commentId);
        commentLink.setContent(List.copyOf(linksList));
        commentLink.setCommentId(commentId);
        commentLinkDomain.saveOrUpdate(commentLink);
    }

    private void updateCommentHashtags(UUID commentId, List<String> hashtagsList) {
        if (hashtagsList == null || hashtagsList.isEmpty()) return;
        CommentHastag commentHashtag =commentHastagDomain.getByCommentId(commentId);
        commentHashtag.setContent(List.copyOf(hashtagsList));
        commentHashtag.setCommentId(commentId);
        commentHastagDomain.saveOrUpdate(commentHashtag);
    }

    private void updateCommentUserTags(UUID commentId, List<String> taggedUserIds) {
        if (taggedUserIds == null || taggedUserIds.isEmpty()) return;
        List<CommentUserTag> userTags = commentUserTagDomain.getByCommentId(commentId);
        commentUserTagDomain.destroyAll(userTags.stream().map(CommentUserTag::getId).collect(Collectors.toList()));
        for (String userIdStr : taggedUserIds) {
            UUID userId = parseUUID(userIdStr, "Invalid user ID in tagged users");
            if (grpcUserService.getUser(userId.toString()) == null) {
                throw new IllegalArgumentException("Tagged user not found: " + userId);
            }
            CommentUserTag commentUserTag = new CommentUserTag();
            commentUserTag.setComment_id(commentId);
            commentUserTag.setUser_id(userId);
            commentUserTagDomain.saveOrUpdate(commentUserTag);
        }
    }
}
