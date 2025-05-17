package post.service.be_post_service.entity;

import jakarta.persistence.*;
import lombok.*;
import post.service.be_post_service.base.BaseEntity;
import post.service.be_post_service.enums.ReactionType;

import java.util.UUID;

@Entity
@Table(name = "comment_reaction", indexes = {
        @Index(name = "idx_comment_reaction_id", columnList = "comment_id"),
        @Index(name = "idx_comment_reaction_user_id", columnList = "user_id")
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
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;
}
