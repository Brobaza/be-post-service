package post.service.be_post_service.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.entity.PostLink;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PostLinkRepository extends BaseRepository<PostLink, UUID> {
    @Query("select c from PostLink c "
            + "where c.postId = :post_id"
    )
    PostLink getByPostId(
            @Param("post_id") final UUID post_id
    );
}
