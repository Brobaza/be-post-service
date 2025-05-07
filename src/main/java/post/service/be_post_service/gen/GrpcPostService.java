package post.service.be_post_service.gen;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import net.devh.boot.grpc.server.service.GrpcService;
import post.service.be_post_service.grpc.PostServiceGrpc;
import post.service.be_post_service.grpc.TestPostRequest;
import post.service.be_post_service.grpc.TestPostResponse;
import userProtoService.UserServiceOuterClass.GetUserResponse;

@GrpcService
public class GrpcPostService extends PostServiceGrpc.PostServiceImplBase {

    private final Logger logger = Logger.getLogger(GrpcPostService.class.getName());
    private final GrpcUserService grpcUserService;

    @Autowired
    public GrpcPostService(GrpcUserService grpcUserService) {
        this.grpcUserService = grpcUserService;
    }

    @Override
    public void testPost(TestPostRequest request, io.grpc.stub.StreamObserver<TestPostResponse> responseObserver) {
        TestPostResponse response = TestPostResponse.newBuilder()
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setMessage(request.getMessage())
                .build();

        GetUserResponse userResponse = grpcUserService
                .getUser(new StringBuilder().append(new String("253c9dc6-b65a-4c21-a5a0-85fd7ad8880c")).toString());

        logger.info("User information: " + userResponse.getName() + ", " + userResponse.getEmail());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
