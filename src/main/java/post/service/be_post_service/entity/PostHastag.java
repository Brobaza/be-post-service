package post.service.be_post_service.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
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
@Table(name = "post_hastag", indexes = {
        @Index(name = "idx_post_hastag_id", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostHastag extends BaseEntity<UUID> {
    @Column(name = "post_id", nullable = false)
    private UUID postId;
    @JsonSubTypes.Type(UUIDArrayType.class)
    @Column(name = "content", columnDefinition = "uuid[]")
    @Builder.Default
    private List<UUID> content = new ArrayList<>();
}
