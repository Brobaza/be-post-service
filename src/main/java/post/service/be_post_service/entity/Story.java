package post.service.be_post_service.entity;

import java.sql.Date;
import java.time.LocalDateTime;
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
import post.service.be_post_service.enums.ReactionType;
import post.service.be_post_service.enums.StoryType;
import post.service.be_post_service.enums.ViewType;

@Entity
@Table(name = "stories", indexes = {
        @Index(name = "index_story_author_id", columnList = "author_id")
        
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story extends BaseEntity<UUID> {

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "images")
    private List<String> images = new ArrayList<>();

    // @Column(name = "is_expired", nullable = false)
    // private boolean isExpired;

    @Column(name = "view_count")
    private int viewCount;
    @Enumerated(EnumType.STRING)
    @Column(name = "story_type")
    private StoryType storyType;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "viewed_user_ids", columnDefinition = "uuid[]")
    private List<UUID> viewedUserIds ;

    @Column(name = "video_url")
    private String videoUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "view_type")
    private ViewType viewType;
   @Transient
   private List<StoryReaction> storyReactions ; 
   @Transient
    private List<CommentStory> commentStories; 
   
}
