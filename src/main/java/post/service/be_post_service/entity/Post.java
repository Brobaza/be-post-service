
package post.service.be_post_service.entity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import post.service.be_post_service.base.BaseEntity;
import post.service.be_post_service.enums.PostType;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "index_post_author_id", columnList = "author_id"),
        @Index(name = "index_post_parent_id", columnList = "post_parent_id"),
        @Index(name = "idx_post_liked_user_ids", columnList = "liked_user_ids"),
        @Index(name = "idx_post_shared_user_ids", columnList = "shared_user_ids")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity<UUID> {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;
    @Column(name = "post_parent_id")
    private UUID postParentId;

    @Column(name = "like_count")
    private int likeCount;
    @Column(name = "share_count")
    private int shareCount;
    @Column(name="commentCount")
    private int commentCount;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "liked_user_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> likedUserIds = new ArrayList<>();
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "shared_user_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> sharedUserIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "images")
    private List<String> images;

    @Enumerated(EnumType.STRING)
    private PostType postType;
    @Transient
    private PostLink postLinks;
    @Transient
    private PostHastag hashtags;
    @Transient
    private List<PostUserTag> userTags;

}