package post.service.be_post_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import post.service.be_post_service.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comment_stories", indexes = {
        @Index(name = "idx_comment_story_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_story_content", columnList = "content")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentStory extends BaseEntity<UUID> {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "story_id", nullable = false)
    private UUID storyId;
    @Transient
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "liked_user_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> likedUserIds = new ArrayList<>();
    @Transient
    @Column(name = "like_count")
    private int likeCount;

    @Transient
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "images")
    private List<String> images;
}