package post.service.be_post_service.domain;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.PostReaction;
import post.service.be_post_service.repositories.PostReactionRepository;

@Domain
public class PostReactionDomain extends BaseDomain<PostReaction, UUID> {
    private final PostReactionRepository postReactionRepository;

    @Autowired
    public PostReactionDomain(PostReactionRepository postReactionRepository) {
        super(postReactionRepository);
        this.postReactionRepository = postReactionRepository;
    }

    public UUID createPostReaction(PostReaction postReaction) {
        return postReactionRepository.save(postReaction).getId();
    }
}
