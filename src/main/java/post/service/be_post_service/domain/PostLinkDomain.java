package post.service.be_post_service.domain;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.PostLink;
import post.service.be_post_service.repositories.PostLinkRepository;

@Domain
public class PostLinkDomain extends BaseDomain<PostLink, UUID> {
    private final PostLinkRepository postLinkRepository;

    @Autowired
    public PostLinkDomain(PostLinkRepository postLinkRepository) {
        super(postLinkRepository);
        this.postLinkRepository = postLinkRepository;
    }

    public UUID createPostLink(PostLink postLink) {
        return postLinkRepository.save(postLink).getId();
    }
}
