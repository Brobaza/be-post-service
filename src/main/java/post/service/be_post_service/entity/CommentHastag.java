package post.service.be_post_service.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import post.service.be_post_service.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "comment_hastag", indexes = {
        @Index(name = "idx_comment_hastag_id", columnList = "comment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentHastag extends BaseEntity<UUID> {
    @Column(name = "comment_id", nullable = false)
    private UUID commentId;
    @JsonSubTypes.Type(StringArrayType.class)
    @Column(name = "content", columnDefinition = "text[]")
    @Builder.Default
    private List<String> content = new ArrayList<>();
}