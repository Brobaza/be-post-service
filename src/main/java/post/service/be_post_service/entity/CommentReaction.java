package post.service.be_post_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import post.service.be_post_service.base.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "comment_reaction", indexes = {
        @Index(name = "idx_comment_reaction_id", columnList = "comment_id"),
        @Index(name = "idx_comment_reaction_user_id", columnList = "user_id"),
        @Index(name = "idx_comment_reaction_type_id", columnList = "reaction_type_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReaction extends BaseEntity<UUID> {
    @Column(name = "comment_id",nullable = false)
    private UUID commentId;
    @Column(name = "user_id",nullable = false)
    private UUID userId;
    @Column(name = "reaction_type_id",nullable = false)
    private UUID reactionTypeId;
}
