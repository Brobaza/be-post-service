package post.service.be_post_service.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.CommentUserTag;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.entity.PostUserTag;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface CommentUserTagRepository extends BaseRepository<CommentUserTag, UUID> {
    @Query("select c from CommentUserTag c "
            + "where c.comment_id = :comment_id"
    )
    List<CommentUserTag> getByCommentId(
            @Param("comment_id") final UUID comment_id
    );
}
