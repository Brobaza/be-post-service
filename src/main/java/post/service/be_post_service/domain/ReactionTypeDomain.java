package post.service.be_post_service.domain;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.ReactionType;
import post.service.be_post_service.repositories.ReactionTypeRepository;


@Domain
public class ReactionTypeDomain extends BaseDomain<ReactionType, UUID> {
    private final ReactionTypeRepository reactionTypeRepository;

    @Autowired
    public ReactionTypeDomain(ReactionTypeRepository reactionTypeRepository) {
        super(reactionTypeRepository);
        this.reactionTypeRepository = reactionTypeRepository;
    }

    public UUID createReactionType(ReactionType reactionType) {
        return reactionTypeRepository.save(reactionType).getId();
    }
}
