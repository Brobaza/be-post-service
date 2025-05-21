package post.service.be_post_service.dtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import post.service.be_post_service.grpc.UpdateCommentRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequestDto {
    private String authorId;
    private String content;
    private List<String> hashtags;
    private List<String> links;
    private List<UUID> taggedUserIds;
    private List<String> images;
    private UUID commentParentId;
    private UUID postId;
    private UUID commentId;
    public UpdateCommentRequestDto(UpdateCommentRequest request) {
        this.authorId = request.getAuthorId();
        this.content = request.getContent();
        this.hashtags = request.getHashtagsList();
        this.links = request.getLinksList();

        this.taggedUserIds = request.getTaggedUserIdsList() != null
                ? request.getTaggedUserIdsList().stream()
                .filter(id -> id != null && !id.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toList())
                : null;

        this.images = request.getImagesList();

        this.commentParentId = request.getCommentParentId() != null && !request.getCommentParentId().isEmpty()
                ? UUID.fromString(request.getCommentParentId())
                : null;

        this.postId = request.getPostId() != null && !request.getPostId().isEmpty()
                ? UUID.fromString(request.getPostId())
                : null;
        this.commentId=request.getCommentId()!=null&&!request.getCommentId().isEmpty()?UUID.fromString(request.getCommentId()):null;
    }
}
