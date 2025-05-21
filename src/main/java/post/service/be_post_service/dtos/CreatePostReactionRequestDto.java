package post.service.be_post_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import post.service.be_post_service.grpc.CreatePostReactionRequest;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostReactionRequestDto {
    private String reactionType;
    private UUID postId;
    private UUID userId;
    public CreatePostReactionRequestDto(CreatePostReactionRequest request) {
        this.reactionType = request.getReactionType();
        this.postId=request.getPostId()!=null&&!request.getPostId().isEmpty()?UUID.fromString(request.getPostId()):null;
        this.userId=request.getUserId()!=null&&!request.getUserId().isEmpty()?UUID.fromString(request.getUserId()):null;
    }
}
