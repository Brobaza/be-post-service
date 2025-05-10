package post.service.be_post_service.repositories;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import post.service.be_post_service.base.BaseRepository;
import post.service.be_post_service.entity.CommentReaction;
import post.service.be_post_service.entity.Post;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface CommentReactionRepository extends BaseRepository<CommentReaction, UUID> {
}
