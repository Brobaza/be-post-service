package post.service.be_post_service.domain;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.PostHastag;
import post.service.be_post_service.repositories.PostHastagRepository;

@Domain
public class PostHastagDomain extends BaseDomain<PostHastag, UUID> {
    private final PostHastagRepository postHastagRepository;

    @Autowired
    public PostHastagDomain(PostHastagRepository postHastagRepository) {
        super(postHastagRepository);
        this.postHastagRepository = postHastagRepository;
    }

    public UUID createPostHashtag(PostHastag postHashtag) {
        return postHastagRepository.save(postHashtag).getId();
    }
}
