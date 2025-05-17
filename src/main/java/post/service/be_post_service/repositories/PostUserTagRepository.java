package post.service.be_post_service.repositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.entity.PostUserTag;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PostUserTagRepository extends BaseRepository<PostUserTag, UUID> {
    @Query("select c from PostUserTag c "
            + "where c.post_id = :post_id"
    )
    List<PostUserTag> getByPostId(
            @Param("post_id") final UUID post_id
    );
}
