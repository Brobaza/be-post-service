package post.service.be_post_service.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
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
            + "where c.id = :post_parent_id")
    Post getParentPost(
            @Param("post_parent_id") final UUID post_parent_id);

    @Query("select c from Post c "
            + "where c.id = :post_id")
    Post getPostById(
            @Param("post_id") final UUID post_id);

    @Query("SELECT p FROM Post p WHERE p.authorId = :user_id ORDER BY p.createdDate DESC")
    List<Post> getPostsByUserId(
            @Param("user_id") final UUID user_id, Pageable pageable);

    @Query("select c from Post c "
            + " where c.postType in :postType"
            + " and c.authorId = :user_id"
            + " order by c.createdDate desc")
    List<Post> getPostByPostType(
            @Param("user_id") final UUID user_id,
            @Param("postType") final List<PostType> postType,
            Pageable pageable);

    @Query("""
                select c
                from Post c
                where
                    (c.postType = 'PUBLIC')
                    or
                    (c.postType = 'FRIEND' and c.authorId in :userIds)
                order by c.createdDate desc
            """)
    List<Post> getPostOnDashBoard(
            @Param("userIds") List<UUID> userIds,
            Pageable pageable);
}
