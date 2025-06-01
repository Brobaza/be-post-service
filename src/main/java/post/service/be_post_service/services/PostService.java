package post.service.be_post_service.services;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import post.service.be_post_service.domain.*;
import post.service.be_post_service.entity.*;
import post.service.be_post_service.enums.PostType;
import post.service.be_post_service.enums.ReactionType;
import post.service.be_post_service.gen.GrpcUserService;
import post.service.be_post_service.grpc.CreatePostReactionRequest;
import post.service.be_post_service.grpc.CreatePostRequest;
import post.service.be_post_service.grpc.GetPostByUserIdRequest;
import post.service.be_post_service.grpc.UpdatePostRequest;
import userProtoService.UserServiceOuterClass;


@Service
public class PostService {

    private final PostDomain postDomain;
    private final PostHastagDomain postHastagDomain;
    private final PostLinkDomain postLinkDomain;
    private final PostUserTagDomain postUserTagDomain;
    private final PostReactionDomain postReactionDomain;
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
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        validateRequest(request.getContent(), request.getAuthorId());
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

        List<String> images = request.getImagesList() != null ? request.getImagesList() : Collections.emptyList();
        post.setImages(images);
        post.setPostType(PostType.valueOf(request.getPostType()));
        post.setCreatedDate(new Date());
        postDomain.create(post);
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
        createPostLinks(post.getId(), links);
        createPostHashtags(post.getId(),hashtags);
        createPostUserTags(post.getId(), request.getTaggedUserIdsList());
        post.setHashtags(postHastagDomain.getByPostId(post.getId()));
        post.setPostLinks(postLinkDomain.getByPostId(post.getId()));
        post.setUserTags(postUserTagDomain.getByPostId(post.getId()));
        return post;
    }

    public Post updatePost(UpdatePostRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        validateRequest(request.getContent(), request.getAuthorId());

        Post post = postDomain.findOne(UUID.fromString(request.getPostId()));
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

        List<String> images = request.getImagesList() != null ? request.getImagesList() : Collections.emptyList();
        post.setImages(images);
        post.setPostType(PostType.valueOf(request.getPostType()));
        post.setLastModifiedDate(new Date());
        postDomain.saveOrUpdate(post);

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
        updatePostLinks(post.getId(), links);
        updatePostHashtags(post.getId(),hashtags);
        updatePostUserTags(post.getId(), request.getTaggedUserIdsList());
        post.setHashtags(postHastagDomain.getByPostId(post.getId()));
        post.setPostLinks(postLinkDomain.getByPostId(post.getId()));
        post.setUserTags(postUserTagDomain.getByPostId(post.getId()));
        return post;
    }

    public List<Post> getPostByUserID(UUID userID,int limit,int page){
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdDate").descending());
        List<Post> listPost=postDomain.getPostsByUserId(userID,pageable);
        for(Post post:listPost){
            post.setHashtags(postHastagDomain.getByPostId(post.getId()));
            post.setPostLinks(postLinkDomain.getByPostId(post.getId()));
            post.setUserTags(postUserTagDomain.getByPostId(post.getId()));
        }
        return listPost;
    }

    public List<Post> getPostOnWallOfOtherUser(UUID userId, UUID otherUserId,int limit,int page) {
        boolean isFriend = grpcUserService.isOnFriendList(userId.toString(), otherUserId.toString());
        List<PostType> postTypes = new ArrayList<>();
        postTypes.add(PostType.PUBLIC);
        if (isFriend) {
            postTypes.add(PostType.FRIEND);
        }
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdDate").descending());
        List<Post> posts = postDomain.getByPostType(otherUserId, postTypes,pageable);
        for (Post post : posts) {
            post.setHashtags(postHastagDomain.getByPostId(post.getId()));
            post.setPostLinks(postLinkDomain.getByPostId(post.getId()));
            post.setUserTags(postUserTagDomain.getByPostId(post.getId()));
        }

        return posts;
    }

    public List<Post> getPostOnDashBoard(UUID userId, int limit, int page) {
        List<UserServiceOuterClass.GetUserResponse> listUser = grpcUserService.getListFriendRequest(String.valueOf(userId), limit, page);
        List<UUID> listUserId = listUser.stream().map(user -> UUID.fromString(user.getId())).collect(Collectors.toList());
        listUserId.add(userId);
        List<PostType> postType = new ArrayList<>();
        postType.add(PostType.PUBLIC);
        postType.add(PostType.FRIEND);
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdDate").descending());
        List<Post> listPost=postDomain.getPostOnDashBoard(listUserId, postType,pageable);
        for (Post post : listPost) {
            post.setHashtags(postHastagDomain.getByPostId(post.getId()));
            post.setPostLinks(postLinkDomain.getByPostId(post.getId()));
            post.setUserTags(postUserTagDomain.getByPostId(post.getId()));
        }

        return listPost;
    }
    public Post getPostById(UUID postId) {
        Post post = postDomain.getPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found: " + postId);
        }
        post.setHashtags(postHastagDomain.getByPostId(postId));
        post.setPostLinks(postLinkDomain.getByPostId(postId));
        post.setUserTags(postUserTagDomain.getByPostId(postId));
        return post;
    }
    public void CreatePostReaction(CreatePostReactionRequest request) {
        Post post = postDomain.getPostById(UUID.fromString(request.getPostId()));
        if (post == null) {
            throw new IllegalArgumentException("Post not found: ");
        }
        List<UUID> listReaction = post.getLikedUserIds();
        int likeCount = post.getLikeCount();
        if (listReaction.contains(UUID.fromString(request.getUserId()))) {
            return;
        } else {
            likeCount = +1;
            listReaction.add(UUID.fromString(request.getUserId()));
        }
        post.setLikeCount(likeCount);
        post.setLikedUserIds(listReaction);
        postDomain.saveOrUpdate(post);
        PostReaction postReaction = new PostReaction();
        postReaction.setPostId(UUID.fromString(request.getPostId()));
        postReaction.setUserId(UUID.fromString(request.getUserId()));
        postReaction.setReactionType(ReactionType.valueOf(request.getReactionType()));
        postReactionDomain.create(postReaction);
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

    private void createPostLinks(UUID postId, List<String> linksList) {
        if (linksList == null || linksList.isEmpty())
            return;
        PostLink postLink = new PostLink();
        postLink.setContent(List.copyOf(linksList));
        postLink.setPostId(postId);
        postLinkDomain.create(postLink);
    }

    private void createPostHashtags(UUID postId, List<String> hashtagsList) {
        if (hashtagsList == null || hashtagsList.isEmpty())
            return;
        PostHastag postHashtag = new PostHastag();
        postHashtag.setContent(List.copyOf(hashtagsList));
        postHashtag.setPostId(postId);
        postHastagDomain.create(postHashtag);
    }

    private void createPostUserTags(UUID postId, List<String> taggedUserIds) {
        if (taggedUserIds == null || taggedUserIds.isEmpty())
            return;

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

    private void updatePostLinks(UUID postId, List<String> linksList) {
        if (linksList == null || linksList.isEmpty())
            return;
        PostLink postLink = postLinkDomain.getByPostId(postId);
        postLink.setContent(List.copyOf(linksList));
        postLink.setPostId(postId);
        postLinkDomain.saveOrUpdate(postLink);
    }

    private void updatePostHashtags(UUID postId, List<String> hashtagsList) {
        if (hashtagsList == null || hashtagsList.isEmpty())
            return;
        PostHastag postHashtag = postHastagDomain.getByPostId(postId);
        postHashtag.setContent(List.copyOf(hashtagsList));
        postHashtag.setPostId(postId);
        postHastagDomain.saveOrUpdate(postHashtag);
    }

    private void updatePostUserTags(UUID postId, List<String> taggedUserIds) {
        if (taggedUserIds == null || taggedUserIds.isEmpty())
            return;
        List<PostUserTag> userTags = postUserTagDomain.getByPostId(postId);
        postUserTagDomain.destroyAll(userTags.stream().map(PostUserTag::getId).collect(Collectors.toList()));
        for (String userIdStr : taggedUserIds) {
            UUID userId = parseUUID(userIdStr, "Invalid user ID in tagged users");
            if (grpcUserService.getUser(userId.toString()) == null) {
                throw new IllegalArgumentException("Tagged user not found: " + userId);
            }
            PostUserTag postUserTag = new PostUserTag();
            postUserTag.setPost_id(postId);
            postUserTag.setUser_id(userId);
            postUserTagDomain.saveOrUpdate(postUserTag);
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
}
