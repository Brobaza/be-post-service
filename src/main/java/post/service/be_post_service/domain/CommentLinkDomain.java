package post.service.be_post_service.domain;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.CommentHastag;
import post.service.be_post_service.entity.CommentLink;
import post.service.be_post_service.repositories.CommentLinkRepository;

@Domain
public class CommentLinkDomain extends BaseDomain<CommentLink, UUID> {
    private final CommentLinkRepository commentLinkRepository;

    @Autowired
    public CommentLinkDomain(CommentLinkRepository commentLinkRepository) {
        super(commentLinkRepository);
        this.commentLinkRepository = commentLinkRepository;
    }

    public UUID createCommentLink(CommentLink commentLink) {
        return commentLinkRepository.save(commentLink).getId();
    }
    public CommentLink getByCommentId(UUID commentId) {
        return commentLinkRepository.getByCommentId(commentId);
    }
}
