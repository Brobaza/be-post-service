package post.service.be_post_service.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.enums.PostType;

@Repository
@Transactional(readOnly = true)
public interface PostRepository extends BaseRepository<Post, UUID> {
    @Query("select c from Post c "
            + "where c.id = :post_parent_id"
    )
    Post getParentPost(
            @Param("post_parent_id") final UUID post_parent_id
    );
    @Query("select c from Post c "
            + "where c.id = :post_id"
    )
    Post getPostById(
            @Param("post_id") final UUID post_id
    );
    @Query("select c from Post c "
            + "where c.authorId = :user_id"
    )
    List<Post> getPostsByUserId(
            @Param("user_id") final UUID user_id
    );
    @Query("select c from Post c "
            + " where c.postType in :postType"
            + " and c.authorId = :user_id"
            + " order by c.createdDate desc"
    )
    List<Post> getPostByPostType(
            @Param("user_id") final UUID user_id,
            @Param("postType") final List<PostType> postType
    );
}
