package post.service.be_post_service.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.CommentHastag;
import post.service.be_post_service.entity.CommentLink;
import post.service.be_post_service.entity.Post;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface CommentHastagRepository extends BaseRepository<CommentHastag, UUID> {
    @Query("select c from CommentHastag c "
            + "where c.commentId = :comment_id"
    )
    CommentHastag getByCommentId(
            @Param("comment_id") final UUID comment_id
    );
}
