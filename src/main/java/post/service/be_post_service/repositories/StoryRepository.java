package post.service.be_post_service.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Story;

@Repository
@Transactional(readOnly = true)
public interface StoryRepository extends BaseRepository<Story, UUID> {

    @Query("SELECT s FROM Story s WHERE s.id = :story_id")
    Story getStoryById(@Param("story_id") UUID storyId);

    @Query("SELECT s FROM Story s WHERE s.authorId = :user_id ORDER BY s.createdDate DESC")
    List<Story> getStoriesByUserId(@Param("user_id") UUID userId);

    @Query("SELECT s FROM Story s WHERE s.authorId IN :user_ids AND s.expiredAt > :now ORDER BY s.createdDate DESC")
    List<Story> getActiveStoriesByUserIds(
            @Param("user_ids") List<UUID> userIds,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT s FROM Story s WHERE s.expiredAt <= :now")
    List<Story> getExpiredStories(@Param("now") LocalDateTime now);
}
