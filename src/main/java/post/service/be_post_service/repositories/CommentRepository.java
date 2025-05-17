package post.service.be_post_service.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Comment;
import post.service.be_post_service.entity.Post;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface CommentRepository extends BaseRepository<Comment, UUID> {
    @Query("select c from Comment c "
            + "where c.id = :comment_parent_id"
    )
    Comment getParentComment(
            @Param("comment_parent_id") final UUID comment_parent_id
    );
    @Query("select c from Comment c "
            + "where c.postId = :postId"
    )
    List<Comment> getByPostId(
            @Param("postId") final UUID postId
    );

}
