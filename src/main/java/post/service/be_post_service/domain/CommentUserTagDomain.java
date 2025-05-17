package post.service.be_post_service.domain;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.CommentHastag;
import post.service.be_post_service.entity.CommentUserTag;
import post.service.be_post_service.repositories.CommentUserTagRepository;

@Domain
public class CommentUserTagDomain extends BaseDomain<CommentUserTag, UUID> {
    private final CommentUserTagRepository commentUserTagRepository;

    @Autowired
    public CommentUserTagDomain(CommentUserTagRepository commentUserTagRepository) {
        super(commentUserTagRepository);
        this.commentUserTagRepository = commentUserTagRepository;
    }

    public UUID createCommentUserTag(CommentUserTag commentUserTag) {
        return commentUserTagRepository.save(commentUserTag).getId();
    }
    public List<CommentUserTag> getByCommentId(UUID commentId) {
        return commentUserTagRepository.getByCommentId(commentId);
    }
}