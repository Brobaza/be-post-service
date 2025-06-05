package post.service.be_post_service.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.lettuce.core.dynamic.annotation.Param;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.StoryReaction;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface StoryReactionRepository extends BaseRepository<StoryReaction, UUID> {
    @Query("SELECT sr FROM StoryReaction sr WHERE sr.storyId = :storyId")
    List<StoryReaction> findByStoryId(@Param("storyId") final UUID storyId);
}