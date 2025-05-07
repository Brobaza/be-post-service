package post.service.be_post_service.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.vladmihalcea.hibernate.type.array.UUIDArrayType;

import post.service.be_post_service.base.BaseEntity;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "index_post_author_id", columnList = "author_id"),
        @Index(name = "idx_post_liked_user_ids", columnList = "liked_user_ids")
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

    @Column(name = "like_count")
    private int likeCount;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "liked_user_ids", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> likedUserIds = new ArrayList<>();

    @Type(UUIDArrayType.class)
    @Column(name = "images", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> images = new ArrayList<>();
}