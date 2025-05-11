package post.service.be_post_service.gen;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import net.devh.boot.grpc.server.service.GrpcService;
import post.service.be_post_service.grpc.*;
import post.service.be_post_service.services.PostService;
import userProtoService.UserServiceOuterClass.GetUserResponse;

@GrpcService
public class GrpcPostService extends PostServiceGrpc.PostServiceImplBase {

    private final Logger logger = Logger.getLogger(GrpcPostService.class.getName());
    private final GrpcUserService grpcUserService;

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
    public void createPost(CreatePostRequest request, io.grpc.stub.StreamObserver<CreatePostResponse> responseObserver){
        postService.createPost(request);
        CreatePostResponse response = CreatePostResponse.newBuilder()
                .setAuthorId(request.getAuthorId())
                .setContent(request.getContent())
                .addAllTaggedUserIds(request.getTaggedUserIdsList())
                .addAllHashtags(request.getHashtagsList())
                .addAllLinks(request.getLinksList())
                .addAllImages(request.getImagesList())
                .setPostParentId(request.getPostParentId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
