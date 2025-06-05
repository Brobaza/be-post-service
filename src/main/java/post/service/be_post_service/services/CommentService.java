package post.service.be_post_service.services;
import java.net.URI;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import post.service.be_post_service.domain.*;
import post.service.be_post_service.entity.*;
import post.service.be_post_service.enums.ReactionType;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreateCommentReactionRequest;
import post.service.be_post_service.grpc.CreateCommentRequest;
import post.service.be_post_service.grpc.UpdateCommentRequest;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        comment.setCreatedDate(new Date());
        commentDomain.create(comment);

        String urlRegex = "(https?://[\\w\\-\\.\\?\\&\\=\\/%#]+)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(request.getContent());
        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group());
        }
        String hashtagRegex = "#[\\p{L}0-9_]+";
        Pattern hashtagPattern = Pattern.compile(hashtagRegex);
        Matcher hashtagMatcher = hashtagPattern.matcher(request.getContent());
        List<String> hashtags = new ArrayList<>();
        while (hashtagMatcher.find()) {
            hashtags.add(hashtagMatcher.group());
        }
        String userTagRegex = "@\\[.*?\\]\\((.*?)\\)";
        Pattern userTagPattern = Pattern.compile(userTagRegex);
        Matcher userTagMatcher = userTagPattern.matcher(request.getContent());
        List<CommentUserTag> userTags = new ArrayList<>();

        while (userTagMatcher.find()) {
            UUID userId = UUID.fromString(userTagMatcher.group(1));
            int startIndex = userTagMatcher.start(1);
            int endIndex = userTagMatcher.end(1) -1;
            userTags.add(new CommentUserTag(userId, comment.getId(), startIndex, endIndex));
        }
        createCommentLinks(comment.getId(),links);
        createCommentHashtags(comment.getId(),hashtags);
        createCommentUserTags( userTags);
        setComment(comment);
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
        comment.setLastModifiedDate(new Date());
        commentDomain.saveOrUpdate(comment);
        String urlRegex = "(https?://[\\w\\-\\.\\?\\&\\=\\/%#]+)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(request.getContent());
        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group());
        }
        String hashtagRegex = "#[\\p{L}0-9_]+";
        Pattern hashtagPattern = Pattern.compile(hashtagRegex);
        Matcher hashtagMatcher = hashtagPattern.matcher(request.getContent());
        List<String> hashtags = new ArrayList<>();
        while (hashtagMatcher.find()) {
            hashtags.add(hashtagMatcher.group());
        }
        String userTagRegex = "@\\[.*?\\]\\((.*?)\\)";
        Pattern userTagPattern = Pattern.compile(userTagRegex);
        Matcher userTagMatcher = userTagPattern.matcher(request.getContent());
        List<CommentUserTag> userTags = new ArrayList<>();

        while (userTagMatcher.find()) {
            UUID userId = UUID.fromString(userTagMatcher.group(1));
            int startIndex = userTagMatcher.start(1);
            int endIndex = userTagMatcher.end(1) -1;
            userTags.add(new CommentUserTag(userId, comment.getId(), startIndex, endIndex));
        }
        updateCommentLinks(comment.getId(),links);
        updateCommentHashtags(comment.getId(), hashtags);
        updateCommentUserTags(comment.getId(), userTags);
        setComment(comment);
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
        boolean isValidReaction=isValidReactionType(request.getReactionType());
        if(!isValidReaction){
            throw new IllegalArgumentException("Invalid reaction type: " + request.getReactionType());
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
    public Comment getCommentById(UUID commentId){
        Comment comment =commentDomain.findOne(commentId);
        if(comment==null){
            throw new IllegalArgumentException("Comment not found: " );
        }
       return comment;
    }
    private boolean isValidReactionType(String input) {
        try {
            ReactionType.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
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
    private void setComment(Comment comment){
        CommentLink links = commentLinkDomain.getByCommentId(comment.getId());
        CommentHastag hashtags = commentHastagDomain.getByCommentId(comment.getId());
        List<CommentUserTag> userTags = commentUserTagDomain.getByCommentId(comment.getId());
        comment.setCommentLinks(links);
        comment.setUserTags(userTags);
        comment.setHashtags(hashtags);
    }
    private void createCommentUserTags(List<CommentUserTag> usertag) {

        for (CommentUserTag user : usertag) {
            if (grpcUserService.getUser(user.getUser_id().toString()) == null) {
                throw new IllegalArgumentException("Tagged user not found: " + user.getUser_id());
            }
            commentUserTagDomain.create(user);
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

    private void updateCommentUserTags(UUID commentId, List<CommentUserTag> usertag) {
        List<CommentUserTag> userTags = commentUserTagDomain.getByCommentId(commentId);
        commentUserTagDomain.destroyAll(userTags.stream().map(CommentUserTag::getId).collect(Collectors.toList()));
        for (CommentUserTag user : usertag) {
            if (grpcUserService.getUser(user.getUser_id().toString()) == null) {
                throw new IllegalArgumentException("Tagged user not found: " + user.getUser_id());
            }
            commentUserTagDomain.saveOrUpdate(user);
        }
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

    private void validateUrls(List<String> urls) {
        if (urls != null) {
            for (String url : urls) {
                if (!isValidUrl(url)) {
                    throw new IllegalArgumentException("Invalid URL: " + url);
                }
            }
        }
    }
    public List<Comment> get10Comment(UUID postId){
        Pageable pageable = PageRequest.of(0, 10);
        List<Comment> comments = commentDomain.getNumberOfComment(postId, pageable);
        for(Comment comment:comments){
            CommentLink links = commentLinkDomain.getByCommentId(comment.getId());
            CommentHastag hashtags = commentHastagDomain.getByCommentId(comment.getId());
            List<CommentUserTag> userTags = commentUserTagDomain.getByCommentId(comment.getId());
            comment.setCommentLinks(links);
            comment.setUserTags(userTags);
            comment.setHashtags(hashtags);
        }
       return comments;
    }
    public List<Comment> getAllCommentByPostId(UUID postId){
        List<Comment> comments = commentDomain.getByPostId(postId);
        for(Comment comment:comments){
            CommentLink links = commentLinkDomain.getByCommentId(comment.getId());
            CommentHastag hashtags = commentHastagDomain.getByCommentId(comment.getId());
            List<CommentUserTag> userTags = commentUserTagDomain.getByCommentId(comment.getId());
            comment.setCommentLinks(links);
            comment.setUserTags(userTags);
            comment.setHashtags(hashtags);
        }
        return comments;
    }
}
