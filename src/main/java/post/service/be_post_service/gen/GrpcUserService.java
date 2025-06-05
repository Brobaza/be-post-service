package post.service.be_post_service.gen;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import post.service.be_post_service.grpc.CreateStoryResponse;
import post.service.be_post_service.grpc.MetaData;
import userProtoService.UserServiceGrpc;
import userProtoService.UserServiceOuterClass;
import userProtoService.UserServiceOuterClass.GetUserRequest;
import userProtoService.UserServiceOuterClass.GetUserResponse;

import java.util.List;

@Service
public class GrpcUserService {
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public GrpcUserService(
            @Value("${microservices.user-service.url}") String userServiceHost,
            @Value("${microservices.user-service.port}") int userServicePort) {
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

    public List<GetUserResponse> getListFriendRequest(String userId, int limit, int page) {
        UserServiceOuterClass.getListFriendRequestReq request = UserServiceOuterClass.getListFriendRequestReq
                .newBuilder()
                .setUserId(userId)
                .setLimit(limit)
                .setPage(page)
                .build();
        return userStub.getListFriendRequest(request).getFriendRequestsList();
    }

    public boolean isOnFriendList(String userId, String friendId) {
        UserServiceOuterClass.isOnFriendListReq request = UserServiceOuterClass.isOnFriendListReq.newBuilder()
                .setUserId(userId)
                .setFriendId(friendId)
                .build();
        UserServiceOuterClass.Metadata metaData = UserServiceOuterClass.Metadata.newBuilder()
                .setCode("200")
                .setMessage("This is friend")
                .build();
        try {
            UserServiceOuterClass.isOnFriendListRes response = UserServiceOuterClass.isOnFriendListRes.newBuilder()
                    .setMetadata(metaData)
                    .build();
        } catch (Exception e) {
            UserServiceOuterClass.Metadata errorMetaData = UserServiceOuterClass.Metadata.newBuilder()
                    .setCode("500")
                    .setMessage(e.getMessage())
                    .build();
            UserServiceOuterClass.isOnFriendListRes errorResponse = UserServiceOuterClass.isOnFriendListRes.newBuilder()
                    .setMetadata(errorMetaData)
                    .build();

            return false;
        }

        return userStub.isOnFriendList(request).getConfirm();
    }

    public UserServiceOuterClass.isOnFriendListRes isOnFriendListWithMetadata(String userId, String friendId) {
        UserServiceOuterClass.isOnFriendListReq request = UserServiceOuterClass.isOnFriendListReq.newBuilder()
                .setUserId(userId)
                .setFriendId(friendId)
                .build();
        // Gọi gRPC để lấy kết quả, bao gồm cả metadata
        UserServiceOuterClass.isOnFriendListRes response = userStub.isOnFriendList(request);
        // response đã bao gồm metadata
        return response;
    }

}
