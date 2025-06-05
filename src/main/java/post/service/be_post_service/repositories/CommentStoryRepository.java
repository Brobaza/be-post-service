package post.service.be_post_service.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.CommentStory;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface CommentStoryRepository extends BaseRepository<CommentStory, UUID> {

    @Query("select c from CommentStory c where c.storyId = :storyId")
    List<CommentStory> getByStoryId(
            @Param("storyId") final UUID storyId
    );

    @Query("select c from CommentStory c where c.authorId = :userId")
    List<CommentStory> getByUserId(
            @Param("userId") final UUID userId
    );
}