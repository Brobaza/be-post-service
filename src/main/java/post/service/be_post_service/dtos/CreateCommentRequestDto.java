package post.service.be_post_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import post.service.be_post_service.grpc.CreateCommentRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequestDto {
    private String authorId;
    private String content;
    private List<String> hashtags;
    private List<String> links;
    private List<UUID> userTag;
    private List<String> images;
    private UUID commentParentId;
    private UUID postId;
    private String message;
    public CreateCommentRequestDto(CreateCommentRequest request,String userName) {
        this.authorId = request.getAuthorId();
        this.content = request.getContent();
        String urlRegex = "(https?://[\\w\\-\\.\\?\\&\\=\\/%#]+)";
        Pattern urlPattern = Pattern.compile(urlRegex);
        Matcher urlMatcher = urlPattern.matcher(request.getContent());
        List<String> links = new ArrayList<>();
        while (urlMatcher.find()) {
            links.add(urlMatcher.group());
        }
        String hashtagRegex = "#[\\p{L}0-9_]+"; // Hỗ trợ cả tiếng Việt
        Pattern hashtagPattern = Pattern.compile(hashtagRegex);
        Matcher hashtagMatcher = hashtagPattern.matcher(request.getContent());

        List<String> hashtags = new ArrayList<>();
        while (hashtagMatcher.find()) {
            hashtags.add(hashtagMatcher.group());
        }
        String userTagRegex = "@[\\p{L}0-9_]+";
        Pattern userTagPattern = Pattern.compile(userTagRegex);
        Matcher userTagMatcher = userTagPattern.matcher(request.getContent());
        List<UUID> userTags = new ArrayList<>();
        while (userTagMatcher.find()) {
            userTags.add(UUID.fromString(userTagMatcher.group().substring(1)));
        }
        this.hashtags = hashtags;
        this.links = links;
        this.userTag = userTags;
        this.images = request.getImagesList();

        this.commentParentId = request.getCommentParentId() != null && !request.getCommentParentId().isEmpty()
                ? UUID.fromString(request.getCommentParentId())
                : null;

        this.postId = request.getPostId() != null && !request.getPostId().isEmpty()
                ? UUID.fromString(request.getPostId())
                : null;
        this.message=userName+" đã bình luận vào bài viết của bạn:"+this.content;
    }
}
