package post.service.be_post_service.repositories;

import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.Post;

@Repository
@Transactional(readOnly = true)
public interface PostRepository extends BaseRepository<Post, UUID> {
}
