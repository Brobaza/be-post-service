package post.service.be_post_service.gen;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import userProtoService.UserServiceGrpc;
import userProtoService.UserServiceOuterClass.GetUserRequest;
import userProtoService.UserServiceOuterClass.GetUserResponse;

@Service
public class GrpcUserService {
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public GrpcUserService(
        @Value("${microservices.user-service.url}") String userServiceHost,
        @Value("${microservices.user-service.port}") int userServicePort
    ) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(userServiceHost, userServicePort)
                .usePlaintext()
                .build();
        this.userStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public GetUserResponse getUser(String userId) {
        GetUserRequest request = GetUserRequest.newBuilder()
                .setId(userId)
                .build();
        return userStub.getUser(request);
    }
}

