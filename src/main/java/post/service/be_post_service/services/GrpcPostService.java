package post.service.be_post_service.services;

import net.devh.boot.grpc.server.service.GrpcService;
import post.service.be_post_service.grpc.PostServiceGrpc;
import post.service.be_post_service.grpc.TestPostRequest;
import post.service.be_post_service.grpc.TestPostResponse;

@GrpcService
public class GrpcPostService extends PostServiceGrpc.PostServiceImplBase {

    @Override
    public void testPost(TestPostRequest request, io.grpc.stub.StreamObserver<TestPostResponse> responseObserver) {
        TestPostResponse response = TestPostResponse.newBuilder()
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setMessage(request.getMessage())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
