package post.service.be_post_service.gen;


import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.grpc.CommentServiceGrpc;
import post.service.be_post_service.grpc.CreateCommentRequest;
import post.service.be_post_service.grpc.CreateCommentResponse;
import post.service.be_post_service.services.CommentService;

@GrpcService
public class GrpcCommentService extends CommentServiceGrpc.CommentServiceImplBase {
    @Autowired
    private CommentService commentService;
    @Override
    public void createComment(CreateCommentRequest request, io.grpc.stub.StreamObserver<CreateCommentResponse> responseObserver){
        commentService.createComment(request);
        CreateCommentResponse response = CreateCommentResponse.newBuilder()
                .setAuthorId(request.getAuthorId())
                .setContent(request.getContent())
                .addAllTaggedUserIds(request.getTaggedUserIdsList())
                .addAllHashtags(request.getHashtagsList())
                .addAllLinks(request.getLinksList())
                .addAllImages(request.getImagesList())
                .setCommentParentId(request.getCommentParentId())
                .setPostId(request.getPostId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
