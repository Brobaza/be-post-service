package post.service.be_post_service.gen;

import java.util.logging.Logger;

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
    @Override
    public void createPost(CreatePostRequest request, io.grpc.stub.StreamObserver<CreatePostResponse> responseObserver) {
        try {
            Post post = postService.createPost(request);
            String authorIdStr = post.getAuthorId() != null ? post.getAuthorId().toString() : "";
            String postIdStr = post.getId() != null ? post.getId().toString() : "";

            CreatePostResponse response = CreatePostResponse.newBuilder()
                    .setAuthorId(authorIdStr)
                    .setContent(post.getContent() != null ? post.getContent() : "")
                    .addAllTaggedUserIds(request.getTaggedUserIdsList())
                    .addAllHashtags(request.getHashtagsList())
                    .addAllLinks(request.getLinksList())
                    .addAllImages(request.getImagesList())
                    .setPostParentId(request.getPostParentId())
                    .setPostId(postIdStr)
                    .setRespcode("0")
                    .setMessage("Post created successfully.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            CreatePostResponse errorResponse = CreatePostResponse.newBuilder()
                    .setRespcode("9")
                    .setMessage("Failed to create post: " + e.getMessage())
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    @Override
    public void createComment(CreateCommentRequest request, io.grpc.stub.StreamObserver<CreateCommentResponse> responseObserver) {
        try {
            Comment comment = commentService.createComment(request);
            String commentIdStr = comment.getId() != null ? comment.getId().toString() : "";
            CreateCommentResponse response = CreateCommentResponse.newBuilder()
                    .setAuthorId(request.getAuthorId() != null ? request.getAuthorId() : "")
                    .setContent(request.getContent() != null ? request.getContent() : "")
                    .addAllTaggedUserIds(request.getTaggedUserIdsList())
                    .addAllHashtags(request.getHashtagsList())
                    .addAllLinks(request.getLinksList())
                    .addAllImages(request.getImagesList())
                    .setCommentParentId(request.getCommentParentId())
                    .setPostId(request.getPostId())
                    .setCommentId(commentIdStr)
                    .setRespcode("0")
                    .setMessage("Comment created successfully.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            CreateCommentResponse errorResponse = CreateCommentResponse.newBuilder()
                    .setRespcode("9")
                    .setMessage("Failed to create comment: " + e.getMessage())
                    .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
}
