package post.service.be_post_service.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.entity.PostHastag;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PostHastagRepository extends BaseRepository<PostHastag, UUID> {
    @Query("select c from PostHastag c "
            + "where c.postId = :post_id"
    )
    PostHastag getByPostId(
            @Param("post_id") final UUID post_id
    );
}
