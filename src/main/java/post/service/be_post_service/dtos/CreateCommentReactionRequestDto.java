package post.service.be_post_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import post.service.be_post_service.grpc.CreateCommentReactionRequest;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentReactionRequestDto {
    private String reactionType;
    private UUID commentId;
    private UUID userId;
    public CreateCommentReactionRequestDto(CreateCommentReactionRequest request) {
       this.reactionType = request.getReactionType();
       this.commentId=request.getCommentId()!=null&&!request.getCommentId().isEmpty()?UUID.fromString(request.getCommentId()):null;
       this.userId=request.getUserId()!=null&&!request.getUserId().isEmpty()?UUID.fromString(request.getUserId()):null;
    }
}
