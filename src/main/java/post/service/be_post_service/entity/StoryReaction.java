package post.service.be_post_service.entity;

import jakarta.persistence.*;
import lombok.*;
import post.service.be_post_service.base.BaseEntity;
import post.service.be_post_service.enums.ReactionType;



import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "story_reaction", indexes = {
        @Index(name = "idx_story_reaction_user_id", columnList = "user_id"),
        @Index(name = "idx_story_reaction_story_id", columnList = "story_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryReaction extends BaseEntity<UUID> {

    @Column(name = "story_id", nullable = false)
    private UUID storyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;
}