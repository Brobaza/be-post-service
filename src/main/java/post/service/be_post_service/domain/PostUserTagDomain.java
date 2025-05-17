package post.service.be_post_service.domain;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.PostLink;
import post.service.be_post_service.entity.PostUserTag;
import post.service.be_post_service.repositories.PostUserTagRepository;

@Domain
public class PostUserTagDomain extends BaseDomain<PostUserTag, UUID> {
    private final PostUserTagRepository postUserTagRepository;

    @Autowired
    public PostUserTagDomain(PostUserTagRepository postUserTagRepository) {
        super(postUserTagRepository);
        this.postUserTagRepository = postUserTagRepository;
    }

    public UUID createPostUserTag(PostUserTag postUserTag) {
        return postUserTagRepository.save(postUserTag).getId();
    }
    public List<PostUserTag> getByPostId(UUID postId) {
        return postUserTagRepository.getByPostId(postId);
    }
}
