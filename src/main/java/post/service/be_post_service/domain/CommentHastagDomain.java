package post.service.be_post_service.domain;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.CommentHastag;
import post.service.be_post_service.repositories.CommentHastagRepository;

@Domain
public class CommentHastagDomain extends BaseDomain<CommentHastag, UUID> {
    private final CommentHastagRepository commentHastagRepository;

    @Autowired
    public CommentHastagDomain(CommentHastagRepository commentHastagRepository) {
        super(commentHastagRepository);
        this.commentHastagRepository = commentHastagRepository;
    }

    public UUID createCommentHashtag(CommentHastag commentHashtag) {
        return commentHastagRepository.save(commentHashtag).getId();
    }
}
