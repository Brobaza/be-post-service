package post.service.be_post_service.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

import net.devh.boot.grpc.server.service.GrpcService;
import post.service.be_post_service.entity.Comment;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.grpc.*;
import post.service.be_post_service.services.CommentService;
import post.service.be_post_service.services.PostService;
import userProtoService.UserServiceOuterClass.GetUserResponse;

@GrpcService
public class GrpcPostService extends PostServiceGrpc.PostServiceImplBase {

    private final Logger logger = Logger.getLogger(GrpcPostService.class.getName());
    private final GrpcUserService grpcUserService;
    @Autowired
    private CommentService commentService;
    @Autowired
    public GrpcPostService(GrpcUserService grpcUserService) {
        this.grpcUserService = grpcUserService;
    }
    @Autowired
    private PostService postService;

    @Override
    public void testPost(TestPostRequest request, io.grpc.stub.StreamObserver<TestPostResponse> responseObserver) {
        TestPostResponse response = TestPostResponse.newBuilder()
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setMessage(request.getMessage())
                .build();

        GetUserResponse userResponse = grpcUserService
                .getUser(new StringBuilder().append(new String("74cf4138-d8ef-41c1-9b97-924d920abe49")).toString());

        logger.info("User information: " + userResponse.getName() + ", " + userResponse.getEmail());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    //post
    @Override
    public void createPost(CreatePostRequest request, io.grpc.stub.StreamObserver<CreatePostResponse> responseObserver) {
        try {
            Post post = postService.createPost(request);
            String authorIdStr = post.getAuthorId() != null ? post.getAuthorId().toString() : "";
            MetaData metaData = MetaData.newBuilder()
                    .setRespcode("200")
                    .setMessage("Post created successfully")
                    .build();
            CreatePostResponse response = CreatePostResponse.newBuilder()
                    .setAuthorId(authorIdStr)
                    .setContent(post.getContent() != null ? post.getContent() : "")
                    .addAllTaggedUserIds(request.getTaggedUserIdsList())
                    .addAllHashtags(request.getHashtagsList())
                    .addAllLinks(request.getLinksList())
                    .addAllImages(request.getImagesList())
                    .setPostParentId(request.getPostParentId())
                    .setPostType(post.getPostType() != null ? post.getPostType().name() : "")
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
    public void updatePost(UpdatePostRequest request, io.grpc.stub.StreamObserver<CreatePostResponse> responseObserver) {
        try {
            Post post = postService.updatePost(request);
            String authorIdStr = post.getAuthorId() != null ? post.getAuthorId().toString() : "";
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
                    .addAllHashtags(request.getHashtagsList())
                    .addAllLinks(request.getLinksList())
                    .addAllImages(request.getImagesList())
                    .setPostParentId(request.getPostParentId())
                    .setPostType(post.getPostType() != null ? post.getPostType().name() : "")
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
            List<Post> listPost = postService.getPostByUserID(UUID.fromString(request.getUserId()));
            List<CreatePostResponse> postResponse = mapPostsToResponses(listPost);
            responseObserver.onNext(buildSuccessResponse(postResponse));
        } catch (Exception e) {
            responseObserver.onNext(buildErrorResponse(e));
        } finally {
            responseObserver.onCompleted();
        }
    }
    @Override
    public void getListPostOnOtherUser(GetListPostOnOtherUserReq request, StreamObserver<ListPostResponse> responseObserver) {
        try {
            List<Post> listPost = postService.getPostOnWallOfOtherUser(
                    UUID.fromString(request.getUserId()),
                    UUID.fromString(request.getFriendId()));
            List<CreatePostResponse> postResponse = mapPostsToResponses(listPost);
            responseObserver.onNext(buildSuccessResponse(postResponse));
        } catch (Exception e) {
            responseObserver.onNext(buildErrorResponse(e));
        } finally {
            responseObserver.onCompleted();
        }
    }
    @Override
    public void getListPostOnDashBoard(GetPostOnDashBoardReq request, StreamObserver<ListPostResponse> responseObserver){
        try {
            List<Post> listPost = postService.getPostOnDashBoard(
                    UUID.fromString(request.getUserId()),
                    request.getLimit(), request.getPage());
            List<CreatePostResponse> postResponse = mapPostsToResponses(listPost);
            responseObserver.onNext(buildSuccessResponse(postResponse));
        } catch (Exception e) {
            responseObserver.onNext(buildErrorResponse(e));
        } finally {
            responseObserver.onCompleted();
        }
    }
    private List<CreatePostResponse> mapPostsToResponses(List<Post> listPost) {
        List<CreatePostResponse> postResponse = new ArrayList<>();
        for (Post post : listPost) {
            CreatePostResponse response = CreatePostResponse.newBuilder()
                    .setAuthorId(post.getAuthorId() != null ? post.getAuthorId().toString() : "")
                    .setContent(post.getContent() != null ? post.getContent() : "")
                    .addAllTaggedUserIds(post.getUserTags() != null ? post.getUserTags().stream()
                            .map(tag -> tag.getId().toString())
                            .collect(Collectors.toList())
                            : Collections.emptyList())
                    .addAllHashtags(post.getHashtags() != null ? post.getHashtags().getContent() : Collections.emptyList())
                    .addAllLinks(post.getPostLinks() != null ? post.getPostLinks().getContent() : Collections.emptyList())
                    .addAllImages(post.getImages() != null ? post.getImages() : Collections.emptyList())
                    .setPostParentId(post.getPostParentId() != null ? post.getPostParentId().toString() : "")
                    .setPostId(post.getId() != null ? post.getId().toString() : "")
                    .setPostType(post.getPostType() != null ? post.getPostType().toString() : "")
                    .build();

            postResponse.add(response);
        }
        return postResponse;
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
    private ListPostResponse buildErrorResponse(Exception e) {
        return ListPostResponse.newBuilder()
                .setMetadata(MetaData.newBuilder()
                        .setRespcode("500")
                        .setMessage("Failed to get posts: " + e.getMessage())
                        .build())
                .build();
    }
    @Override
    public void createReactionPost(CreatePostReactionRequest request, io.grpc.stub.StreamObserver<MetaData> responseObserver) {
        try {
            postService.CreatePostReaction(request);
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
    //comment
    @Override
    public void createComment(CreateCommentRequest request, io.grpc.stub.StreamObserver<CreateCommentResponse> responseObserver) {
        try {
            Comment comment = commentService.createComment(request);
            MetaData metaData=MetaData.newBuilder()
                    .setRespcode("200")
                    .setMessage("Comment created successfully")
                    .build();
            CreateCommentResponse response = CreateCommentResponse.newBuilder()
                    .setAuthorId(request.getAuthorId() != null ? request.getAuthorId() : "")
                    .setContent(request.getContent() != null ? request.getContent() : "")
                    .addAllTaggedUserIds(request.getTaggedUserIdsList())
                    .addAllHashtags(request.getHashtagsList())
                    .addAllLinks(request.getLinksList())
                    .addAllImages(request.getImagesList())
                    .setCommentParentId(request.getCommentParentId())
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
    public void updateComment(UpdateCommentRequest request, io.grpc.stub.StreamObserver<CreateCommentResponse> responseObserver) {
        try {
            Comment comment = commentService.updateComment(request);
            String commentIdStr = comment.getId() != null ? comment.getId().toString() : "";
            MetaData metaData=MetaData.newBuilder()
                    .setRespcode("200")
                    .setMessage("Comment created successfully")
                    .build();
            CreateCommentResponse response = CreateCommentResponse.newBuilder()
                    .setCommentId(commentIdStr)
                    .setAuthorId(request.getAuthorId() != null ? request.getAuthorId() : "")
                    .setContent(request.getContent() != null ? request.getContent() : "")
                    .addAllTaggedUserIds(request.getTaggedUserIdsList())
                    .addAllHashtags(request.getHashtagsList())
                    .addAllLinks(request.getLinksList())
                    .addAllImages(request.getImagesList())
                    .setCommentParentId(request.getCommentParentId())
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
    public void getListComment(GetListCommentRequest request, StreamObserver<ListCommentResponse> responseObserver) {
        try {
            List<Comment> comments = commentService.getListCommentByPostId(UUID.fromString(request.getPostId()));
            List<CreateCommentResponse> commentResponses = new ArrayList<>();

            for (Comment comment : comments) {
                CreateCommentResponse response = CreateCommentResponse.newBuilder()
                        .setCommentId(comment.getId() != null ? comment.getId().toString() : "")
                        .setAuthorId(comment.getAuthorId() != null ? comment.getAuthorId().toString() : "")
                        .setContent(comment.getContent() != null ? comment.getContent() : "")
                        .addAllTaggedUserIds(comment.getUserTags() != null ? comment.getUserTags().stream()
                                .map(tag -> tag.getId().toString())
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                        )
                        .addAllHashtags(comment.getHashtags() != null ? comment.getHashtags().getContent() : Collections.emptyList())
                        .addAllLinks(comment.getCommentLinks() != null ? comment.getCommentLinks().getContent() : Collections.emptyList())
                        .addAllImages(comment.getImages() != null ? comment.getImages() : Collections.emptyList())
                        .setCommentParentId(comment.getCommentParentId() != null ? comment.getCommentParentId().toString() : "")
                        .setPostId(comment.getPostId() != null ? comment.getPostId().toString() : "")
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
    public void createReactionComment(CreateCommentReactionRequest request,io.grpc.stub.StreamObserver<MetaData> responseObserver){
        try {
            commentService.createCommentReaction(request);
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
}
