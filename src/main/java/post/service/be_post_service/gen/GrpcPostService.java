package post.service.be_post_service.gen;

import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.grpc.stub.StreamObserver;
import io.lettuce.core.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.devh.boot.grpc.server.service.GrpcService;
import post.service.be_post_service.dtos.CreateCommentReactionRequestDto;
import post.service.be_post_service.dtos.CreateCommentRequestDto;
import post.service.be_post_service.dtos.CreatePostReactionRequestDto;
import post.service.be_post_service.dtos.TestDto;
import post.service.be_post_service.entity.Comment;
import post.service.be_post_service.entity.CommentUserTag;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.entity.Story;
import post.service.be_post_service.grpc.*;
import post.service.be_post_service.queue.ProducerService;
import post.service.be_post_service.services.CommentService;
import post.service.be_post_service.services.PostService;
import post.service.be_post_service.services.StoryService;
import userProtoService.UserServiceOuterClass.GetUserResponse;
@GrpcService
public class GrpcPostService extends PostServiceGrpc.PostServiceImplBase {

        private final Logger logger = Logger.getLogger(GrpcPostService.class.getName());
        private final GrpcUserService grpcUserService;
        @Autowired
        private CommentService commentService;

        @Autowired
        private ProducerService producerService;

        @Autowired
        private StoryService storyService;

        @Autowired
        public GrpcPostService(GrpcUserService grpcUserService, ProducerService producerService) {
                this.producerService = producerService;
                this.grpcUserService = grpcUserService;
        }

        @Autowired
        private PostService postService;
        @Value("${spring.kafka.create-comment}")
        private String createCommentTopic;
        @Value("${spring.kafka.reaction-post}")
        private String reactionPostTopic;
        @Value("${spring.kafka.reaction-comment}")
        private String reactionCommentTopic;
        @Value("${spring.kafka.mention-comment}")
        private String mentionCommentTopic;
        @Override
        public void testPost(TestPostRequest request, io.grpc.stub.StreamObserver<TestPostResponse> responseObserver) {
                TestPostResponse response = TestPostResponse.newBuilder()
                                .setName(request.getName())
                                .setEmail(request.getEmail())
                                .setMessage(request.getMessage())
                                .build();

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                try {
                        TestDto testDto = new TestDto(request);
                        String parsedValue = mapper.writeValueAsString(testDto);
                        System.out.println("Parsed value: " + parsedValue);

                        this.producerService.sendMessage(parsedValue, "test-post");

                } catch (JsonProcessingException e) {
                        logger.severe("Error processing JSON: " + e.getMessage());
                }

                GetUserResponse userResponse = grpcUserService
                                .getUser(new StringBuilder().append(new String("74cf4138-d8ef-41c1-9b97-924d920abe49"))
                                                .toString());

                logger.info("User information: " + userResponse.getName() + ", " + userResponse.getEmail());

                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        // post
        @Override
        public void createPost(CreatePostRequest request,
                io.grpc.stub.StreamObserver<CreatePostResponse> responseObserver) {
            try {
                Post post = postService.createPost(request);
                String authorIdStr = post.getAuthorId() != null ? post.getAuthorId().toString() : "";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = formatter.format(post.getCreatedDate());
                MetaData metaData = MetaData.newBuilder()
                        .setRespcode("200")
                        .setMessage("Post created successfully")
                        .build();
                CreatePostResponse response = CreatePostResponse.newBuilder()
                        .setAuthorId(authorIdStr)
                        .setPostId(post.getId() != null ? post.getId().toString() : "")
                        .setContent(post.getContent() != null ? post.getContent() : "")
                        .addAllTaggedUserIds(request.getTaggedUserIdsList())
                        .addAllHashtags(post.getHashtags() != null ? post.getHashtags().getContent() : Collections.emptyList())
                        .addAllLinks(post.getPostLinks() != null ? post.getPostLinks().getContent() : Collections.emptyList())
                        .addAllImages(request.getImagesList())
                        .setPostParentId(request.getPostParentId())
                        .setPostType(post.getPostType() != null ? post.getPostType().name() : "")
                        .setCreatedAt(dateString)
                        .setMetaData(metaData)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                MetaData errorMeta = MetaData.newBuilder()
                        .setRespcode("500")
                        .setMessage("Failed to create post: " + e.getMessage())
                        .build();
                CreatePostResponse errorResponse = CreatePostResponse.newBuilder()
                        .setMetaData(errorMeta)
                        .build();
                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
            }
        }

        @Override
        public void updatePost(UpdatePostRequest request,
                io.grpc.stub.StreamObserver<CreatePostResponse> responseObserver) {
            try {
                Post post = postService.updatePost(request);
                String authorIdStr = post.getAuthorId() != null ? post.getAuthorId().toString() : "";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = formatter.format(post.getLastModifiedDate());
                String postIdStr = post.getId() != null ? post.getId().toString() : "";
                MetaData metaData = MetaData.newBuilder()
                        .setRespcode("200")
                        .setMessage("Post updated successfully")
                        .build();
                CreatePostResponse response = CreatePostResponse.newBuilder()
                        .setPostId(postIdStr)
                        .setAuthorId(authorIdStr)
                        .setContent(post.getContent() != null ? post.getContent() : "")
                        .addAllTaggedUserIds(request.getTaggedUserIdsList())
                        .addAllHashtags(post.getHashtags() != null ? post.getHashtags().getContent() : Collections.emptyList())
                        .addAllLinks(post.getPostLinks() != null ? post.getPostLinks().getContent() : Collections.emptyList())
                        .addAllImages(request.getImagesList())
                        .setPostParentId(request.getPostParentId())
                        .setPostType(post.getPostType() != null ? post.getPostType().name() : "")
                        .setCreatedAt(dateString)
                        .setMetaData(metaData)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                MetaData errorMeta = MetaData.newBuilder()
                        .setRespcode("500")
                        .setMessage("Failed to update post: " + e.getMessage())
                        .build();
                CreatePostResponse errorResponse = CreatePostResponse.newBuilder()
                        .setMetaData(errorMeta)
                        .build();
                responseObserver.onNext(errorResponse);
                responseObserver.onCompleted();
            }
        }

        @Override
        public void getListPostByUserID(GetPostByUserIdRequest request, StreamObserver<ListPostResponse> responseObserver) {
            try {
                List<Post> listPost = postService.getPostByUserID(UUID.fromString(request.getUserId()),request.getLimit(),request.getPage());
                List<CreatePostResponse> postResponse = mapPostsToResponses(listPost);
                responseObserver.onNext(buildSuccessResponse(postResponse));
            } catch (Exception e) {
                responseObserver.onNext(buildErrorResponses(e));
            } finally {
                responseObserver.onCompleted();
            }
        }

        @Override
        public void getListPostOnOtherUser(GetListPostOnOtherUserReq request,
                StreamObserver<ListPostResponse> responseObserver) {
            try {
                List<Post> listPost = postService.getPostOnWallOfOtherUser(
                        UUID.fromString(request.getUserId()),
                        UUID.fromString(request.getFriendId()),request.getLimit(),request.getPage());
                List<CreatePostResponse> postResponse = mapPostsToResponses(listPost);
                responseObserver.onNext(buildSuccessResponse(postResponse));
            } catch (Exception e) {
                responseObserver.onNext(buildErrorResponses(e));
            } finally {
                responseObserver.onCompleted();
            }
        }

        @Override
        public void getListPostOnDashBoard(GetPostOnDashBoardReq request,
                StreamObserver<ListPostResponse> responseObserver) {
            try {
                List<Post> listPost = postService.getPostOnDashBoard(
                        UUID.fromString(request.getUserId()),
                        request.getLimit(), request.getPage());
                List<CreatePostResponse> postResponse = mapPostsToResponses(listPost);
                responseObserver.onNext(buildSuccessResponse(postResponse));
            } catch (Exception e) {
                responseObserver.onNext(buildErrorResponses(e));
            } finally {
                responseObserver.onCompleted();
            }
        }
        @Override
        public void getPostDetail(GetPostDetailRequest request, StreamObserver<CreatePostResponse> responseObserver) {
            try{
                Post post=postService.getPostById(UUID.fromString(request.getPostId()));
                CreatePostResponse response=mapPostToResponses(post);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
            responseObserver.onNext(buildErrorResponse(e));
        } finally {
            responseObserver.onCompleted();
        }
        }

        private List<CreatePostResponse> mapPostsToResponses(List<Post> listPost) {
            List<CreatePostResponse> postResponse = new ArrayList<>();

            for (Post post : listPost) {
                List<Comment> comments = commentService.get10Comment(post.getId());
                List<UUID> likedUser = post.getLikedUserIds();
                List<String> lastThreeAsString = new ArrayList<>();
                if (likedUser != null && !likedUser.isEmpty()) {
                    int size = likedUser.size();
                    List<UUID> lastThree = likedUser.subList(Math.max(size - 3, 0), size);
                    lastThreeAsString = lastThree.stream()
                            .map(UUID::toString)
                            .collect(Collectors.toList());
                }
                CreatePostResponse response = CreatePostResponse.newBuilder()
                        .setAuthorId(post.getAuthorId() != null ? post.getAuthorId().toString() : "")
                        .setContent(post.getContent() != null ? post.getContent() : "")
                        .addAllTaggedUserIds(post.getUserTags() != null ? post.getUserTags().stream()
                                .map(tag -> tag.getId().toString())
                                .collect(Collectors.toList())
                                : Collections.emptyList())
                        .addAllHashtags(
                                post.getHashtags() != null ? post.getHashtags().getContent() : Collections.emptyList())
                        .addAllLinks(
                                post.getPostLinks() != null ? post.getPostLinks().getContent() : Collections.emptyList())
                        .addAllImages(post.getImages() != null ? post.getImages() : Collections.emptyList())
                        .setPostParentId(post.getPostParentId() != null ? post.getPostParentId().toString() : "")
                        .setPostId(post.getId() != null ? post.getId().toString() : "")
                        .setPostType(post.getPostType() != null ? post.getPostType().toString() : "")
                        .addAllComment(comments != null ? mapCommentResponse(comments) : Collections.emptyList())
                        .setLikedCount(post.getLikeCount())
                        .setSharedCount(post.getShareCount())
                        .setCommandCount(post.getCommentCount())
                        .setCreatedAt(post.getCreatedDate() != null ? post.getCreatedDate().toString() : "")
                        .addAllListUserLikedIds(lastThreeAsString)
                        .build();

                postResponse.add(response);
            }
            return postResponse;
        }
        private CreatePostResponse mapPostToResponses(Post post) {
                List<Comment> comments = commentService.getAllCommentByPostId(post.getId());
            List<UUID> likedUser = post.getLikedUserIds();

            List<String> lastThreeAsString = new ArrayList<>();
            if (likedUser != null && !likedUser.isEmpty()) {
                int size = likedUser.size();
                List<UUID> lastThree = likedUser.subList(Math.max(size - 3, 0), size);
                lastThreeAsString = lastThree.stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList());
            }
                CreatePostResponse response = CreatePostResponse.newBuilder()
                        .setAuthorId(post.getAuthorId() != null ? post.getAuthorId().toString() : "")
                        .setContent(post.getContent() != null ? post.getContent() : "")
                        .addAllTaggedUserIds(post.getUserTags() != null ? post.getUserTags().stream()
                                .map(tag -> tag.getId().toString())
                                .collect(Collectors.toList())
                                : Collections.emptyList())
                        .addAllHashtags(
                                post.getHashtags() != null ? post.getHashtags().getContent() : Collections.emptyList())
                        .addAllLinks(
                                post.getPostLinks() != null ? post.getPostLinks().getContent() : Collections.emptyList())
                        .addAllImages(post.getImages() != null ? post.getImages() : Collections.emptyList())
                        .setPostParentId(post.getPostParentId() != null ? post.getPostParentId().toString() : "")
                        .setPostId(post.getId() != null ? post.getId().toString() : "")
                        .setPostType(post.getPostType() != null ? post.getPostType().toString() : "")
                        .addAllComment(comments != null ? mapCommentResponse(comments) : Collections.emptyList())
                        .setLikedCount(post.getLikeCount())
                        .setSharedCount(post.getShareCount())
                        .setCommandCount(post.getCommentCount())
                        .addAllListUserLikedIds(lastThreeAsString)
                        .setCreatedAt(post.getCreatedDate() != null ? post.getCreatedDate().toString() : "")
                        .build();
            return response;
        }
        private ListPostResponse buildSuccessResponse(List<CreatePostResponse> postResponse) {
            return ListPostResponse.newBuilder()
                    .addAllPostResponse(postResponse)
                    .setMetadata(MetaData.newBuilder()
                            .setRespcode("200")
                            .setMessage("Posts retrieved successfully")
                            .build())
                    .build();
        }

        private ListPostResponse buildErrorResponses(Exception e) {
            return ListPostResponse.newBuilder()
                    .setMetadata(MetaData.newBuilder()
                            .setRespcode("500")
                            .setMessage("Failed to get posts: " + e.getMessage())
                            .build())
                    .build();
        }
        private CreatePostResponse buildErrorResponse(Exception e) {
            return CreatePostResponse.newBuilder()
                    .setMetaData(MetaData.newBuilder()
                            .setRespcode("500")
                            .setMessage("Failed to get posts: " + e.getMessage())
                            .build())
                    .build();
        }
        private List<CreateCommentResponse> mapCommentResponse(List<Comment> comments){
            List<CreateCommentResponse> commentResponses = new ArrayList<>();
            for(Comment comment : comments){
                List<CommentUserTag> commentUserTags = comment.getUserTags();
                List<String> mentions=new ArrayList<>();
                if(commentUserTags != null && commentUserTags.size() > 0){
                    for (CommentUserTag user: commentUserTags){
                        String userName=grpcUserService.getUser(user.getUser_id().toString()).getName();
                        String metion="id: "+comment.getId()+" name:"+userName+" userId:"+user.getUser_id()+
                                " startIndex:"+user.getStart_index()+" endIndex:"+user.getEnd_index();
                        mentions.add(metion);
                    }
                }
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = formatter.format(comment.getCreatedDate());
            CreateCommentResponse response = CreateCommentResponse.newBuilder()
                    .setAuthorId(comment.getAuthorId() != null ? comment.getAuthorId().toString() : "")
                    .setContent(comment.getContent() != null ? comment.getContent() : "")
                    .addAllTaggedUserIds(comment.getUserTags() != null ? comment.getUserTags().stream()
                            .map(tag -> tag.getId().toString())
                            .collect(Collectors.toList())
                            : Collections.emptyList())
                    .addAllHashtags(comment.getHashtags() != null ? comment.getHashtags().getContent() : Collections.emptyList())
                    .addAllLinks(comment.getCommentLinks() != null ? comment.getCommentLinks().getContent() : Collections.emptyList())
                    .addAllImages(comment.getImages() != null ? comment.getImages() : Collections.emptyList())
                    .setCommentParentId(comment.getCommentParentId() != null ? comment.getCommentParentId().toString() : ""
                    )
                    .setCreateAt(dateString)
                    .addAllMention(mentions)
                    .build();
                commentResponses.add(response);
            }
            return commentResponses;
        }

        @Override
        public void createReactionPost(CreatePostReactionRequest request,
                        io.grpc.stub.StreamObserver<MetaData> responseObserver) {
                try {
                    postService.CreatePostReaction(request);
                    String userName=grpcUserService.getUser(request.getUserId()).getName();
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
                    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                    try {
                        CreatePostReactionRequestDto reactionPost = new CreatePostReactionRequestDto(request,userName);
                        String parsedValue = mapper.writeValueAsString(reactionPost);
                        System.out.println("Create successfully reaction post: " + parsedValue);
                        this.producerService.sendMessage(parsedValue, reactionPostTopic);

                    } catch (JsonProcessingException e) {
                        logger.severe("Error processing JSON: " + e.getMessage());
                    }
                        MetaData metaData = MetaData.newBuilder()
                                        .setRespcode("200")
                                        .setMessage("Reaction created successfully")
                                        .build();
                        responseObserver.onNext(metaData);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        MetaData errorMeta = MetaData.newBuilder()
                                        .setRespcode("500")
                                        .setMessage("Failed to create reaction: " + e.getMessage())
                                        .build();
                        responseObserver.onNext(errorMeta);
                        responseObserver.onCompleted();
                }
        }

        // comment
        @Override
    public void createComment(CreateCommentRequest request,
            io.grpc.stub.StreamObserver<CreateCommentResponse> responseObserver) {
        try {
            Comment comment= commentService.createComment(request);
            String userName=grpcUserService.getUser(request.getAuthorId()).getName();
            List<String> userTag = comment.getUserTags() != null
                    ? comment.getUserTags().stream()
                    .map(tag -> tag.getId().toString())
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            if(userTag.size() > 0){
                for (String tagId : userTag) {
                    try {
                        String message=comment.getAuthorId()+" đã nhắc tới bạn trong một bình luận:"+comment.getContent() +" "+tagId;
                        String parsedValue = mapper.writeValueAsString(message);
                        System.out.println("Comment created successfully: " + parsedValue);
                        this.producerService.sendMessage(parsedValue, mentionCommentTopic);

                    } catch (JsonProcessingException e) {
                        logger.severe("Error processing JSON: " + e.getMessage());
                    }
                }
            }
            try {
                CreateCommentRequestDto create_comment = new CreateCommentRequestDto(request,userName);
                String parsedValue = mapper.writeValueAsString(create_comment);
                System.out.println("Comment created successfully: " + parsedValue);
                this.producerService.sendMessage(parsedValue, createCommentTopic);

            } catch (JsonProcessingException e) {
                logger.severe("Error processing JSON: " + e.getMessage());
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(comment.getCreatedDate());
            MetaData metaData = MetaData.newBuilder()
                    .setRespcode("200")
                    .setMessage("Comment created successfully")
                    .build();
            CreateCommentResponse response = CreateCommentResponse.newBuilder()
                    .setAuthorId(request.getAuthorId() != null ? request.getAuthorId() : "")
                    .setCommentId(comment.getId() != null ? comment.getId().toString() : "")
                    .setContent(request.getContent() != null ? request.getContent() : "")
                    .addAllTaggedUserIds(userTag)
                    .addAllHashtags(comment.getHashtags() != null ? comment.getHashtags().getContent() : Collections.emptyList())
                    .addAllLinks(comment.getCommentLinks() != null ? comment.getCommentLinks().getContent() : Collections.emptyList())
                    .addAllImages(request.getImagesList())
                    .setCommentParentId(request.getCommentParentId())
                    .setPostId(request.getPostId())
                    .setCreateAt(dateString)
                    .setMetaData(metaData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            MetaData errorMeta = MetaData.newBuilder()
                    .setRespcode("500")
                    .setMessage("Failed to create comment: " + e.getMessage())
                    .build();
            CreateCommentResponse errorResponse = CreateCommentResponse.newBuilder()
                    .setMetaData(errorMeta)
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateComment(UpdateCommentRequest request,
            io.grpc.stub.StreamObserver<CreateCommentResponse> responseObserver) {
        try {
            Comment comment =commentService.updateComment(request);
            List<String> userTag = comment.getUserTags() != null
                    ? comment.getUserTags().stream()
                    .map(tag -> tag.getId().toString())
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(comment.getLastModifiedDate());
            String commentIdStr = comment.getId() != null ? comment.getId().toString() : "";
            MetaData metaData = MetaData.newBuilder()
                    .setRespcode("200")
                    .setMessage("Comment created successfully")
                    .build();
            CreateCommentResponse response = CreateCommentResponse.newBuilder()
                    .setCommentId(commentIdStr)
                    .setAuthorId(request.getAuthorId() != null ? request.getAuthorId() : "")
                    .setContent(request.getContent() != null ? request.getContent() : "")
                    .addAllTaggedUserIds(userTag)
                    .addAllHashtags(comment.getHashtags() != null ? comment.getHashtags().getContent() : Collections.emptyList())
                    .addAllLinks(comment.getCommentLinks() != null ? comment.getCommentLinks().getContent() : Collections.emptyList())
                    .addAllImages(request.getImagesList())
                    .setCommentParentId(request.getCommentParentId())
                    .setCreateAt(dateString)
                    .setPostId(request.getPostId())
                    .setMetaData(metaData)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            MetaData errorMeta = MetaData.newBuilder()
                    .setRespcode("500")
                    .setMessage("Failed to create comment: " + e.getMessage())
                    .build();
            CreateCommentResponse errorResponse = CreateCommentResponse.newBuilder()
                    .setMetaData(errorMeta)
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
        @Override
        public void getListComment(GetListCommentRequest request,
                        StreamObserver<ListCommentResponse> responseObserver) {
                try {
                        List<Comment> comments = commentService
                                        .getListCommentByPostId(UUID.fromString(request.getPostId()));
                        List<CreateCommentResponse> commentResponses = new ArrayList<>();

                        for (Comment comment : comments) {
                                CreateCommentResponse response = CreateCommentResponse.newBuilder()
                                                .setCommentId(comment.getId() != null ? comment.getId().toString() : "")
                                                .setAuthorId(comment.getAuthorId() != null
                                                                ? comment.getAuthorId().toString()
                                                                : "")
                                                .setContent(comment.getContent() != null ? comment.getContent() : "")
                                                .addAllTaggedUserIds(comment.getUserTags() != null
                                                                ? comment.getUserTags().stream()
                                                                                .map(tag -> tag.getId().toString())
                                                                                .collect(Collectors.toList())
                                                                : Collections.emptyList())
                                                .addAllHashtags(comment.getHashtags() != null
                                                                ? comment.getHashtags().getContent()
                                                                : Collections.emptyList())
                                                .addAllLinks(comment.getCommentLinks() != null
                                                                ? comment.getCommentLinks().getContent()
                                                                : Collections.emptyList())
                                                .addAllImages(comment.getImages() != null ? comment.getImages()
                                                                : Collections.emptyList())
                                                .setCommentParentId(
                                                                comment.getCommentParentId() != null
                                                                                ? comment.getCommentParentId()
                                                                                                .toString()
                                                                                : "")
                                                .setPostId(comment.getPostId() != null ? comment.getPostId().toString()
                                                                : "")
                                                .build();

                                commentResponses.add(response);
                        }

                        ListCommentResponse finalResponse = ListCommentResponse.newBuilder()
                                        .addAllComments(commentResponses)
                                        .setMetadata(MetaData.newBuilder()
                                                        .setRespcode("200")
                                                        .setMessage("Comments retrieved successfully")
                                                        .build())
                                        .build();

                        responseObserver.onNext(finalResponse);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        ListCommentResponse errorResponse = ListCommentResponse.newBuilder()
                                        .setMetadata(MetaData.newBuilder()
                                                        .setRespcode("500")
                                                        .setMessage("Failed to get comments: " + e.getMessage())
                                                        .build())
                                        .build();
                        responseObserver.onNext(errorResponse);
                        responseObserver.onCompleted();
                }
        }

        @Override
        public void createReactionComment(CreateCommentReactionRequest request,
                        io.grpc.stub.StreamObserver<MetaData> responseObserver) {
                try {
                    commentService.createCommentReaction(request);
                    String userName=grpcUserService.getUser(request.getUserId()).getName();
                    Comment comment = commentService.getCommentById(UUID.fromString(request.getCommentId()));
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
                    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    try {
                        CreateCommentReactionRequestDto reactionComment = new CreateCommentReactionRequestDto(request,userName,comment.getContent());
                        String parsedValue = mapper.writeValueAsString(reactionComment);
                        System.out.println("Reaction created successfully: " + parsedValue);
                        this.producerService.sendMessage(parsedValue, reactionCommentTopic);

                    } catch (JsonProcessingException e) {
                        logger.severe("Error processing JSON: " + e.getMessage());
                    }
                        MetaData metaData = MetaData.newBuilder()
                                        .setRespcode("200")
                                        .setMessage("Reaction created successfully")
                                        .build();
                        responseObserver.onNext(metaData);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        MetaData errorMeta = MetaData.newBuilder()
                                        .setRespcode("500")
                                        .setMessage("Failed to create reaction: " + e.getMessage())
                                        .build();
                        responseObserver.onNext(errorMeta);
                        responseObserver.onCompleted();
                }
        }

        // Story
        @Override
        public void createStory(CreateStoryRequest request,
                        io.grpc.stub.StreamObserver<CreateStoryResponse> responseObserver) {

                try {
                        Story story = storyService.createStory(request);
                        String storyIdStr = story.getId() != null ? story.getId().toString() : "";
                        MetaData metaData = MetaData.newBuilder()
                                        .setRespcode("200")
                                        .setMessage("Story created successfully")
                                        .build();

                        CreateStoryResponse response = CreateStoryResponse.newBuilder()
                                        .setStoryId(storyIdStr)
                                        .setMetaData(metaData)
                                        .build();

                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        CreateStoryResponse errorResponse = CreateStoryResponse.newBuilder()
                                        .setMetaData(MetaData.newBuilder()
                                                        .setRespcode("500")
                                                        .setMessage("Failed to create story: " + e.getMessage())
                                                        .build())
                                        .build();
                        responseObserver.onNext(errorResponse);
                        responseObserver.onCompleted();
                }
        }

        @Override
        public void getListStory(GetListStoryRequest request, StreamObserver<GetListStoryResponse> responseObserver) {
                try {
                        // Lấy danh sách story response từ service
                        GetListStoryResponse response = storyService.getListStory(request);

                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (Exception e) {
                        GetListStoryResponse errorResponse = GetListStoryResponse.newBuilder()
                                        .setMetaData(MetaData.newBuilder()
                                                        .setRespcode("500")
                                                        .setMessage("Failed to get stories: " + e.getMessage())
                                                        .build())
                                        .build();
                        responseObserver.onNext(errorResponse);
                        responseObserver.onCompleted();
                }
        }
 }
