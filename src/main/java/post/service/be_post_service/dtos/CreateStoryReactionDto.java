package post.service.be_post_service.dtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import post.service.be_post_service.grpc.CreateStoryReactionRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoryReactionDto {
    private String userId;
    private String storyId;
    private String reactionType;

    public CreateStoryReactionDto(CreateStoryReactionRequest request) {
        this.userId = request.getUserId();
        this.storyId = request.getStoryId();
        this.reactionType = request.getReactionType();
    }
}
