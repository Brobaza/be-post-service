package post.service.be_post_service.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.vladmihalcea.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import post.service.be_post_service.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "index_comment_author_id", columnList = "author_id"),
        @Index(name = "index_comment_parent_id", columnList = "comment_parent_id"),
        @Index(name = "idx_comment_liked_user_ids", columnList = "liked_user_ids"),
        @Index(name = "idx_comment_post_id", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity<UUID> {
    @Transient
    private CommentLink commentLinks;
    @Transient
    private CommentHastag hashtags;
    @Transient
    private List<CommentUserTag> userTags;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;
    @Column(name = "comment_parent_id")
    private UUID commentParentId;
    @Column(name = "post_id", nullable = false)
    private UUID postId;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "liked_user_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> likedUserIds = new ArrayList<>();
    @Column(name = "like_count")
    private int likeCount;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "images")
    private List<String> images;
}
