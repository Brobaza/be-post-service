package post.service.be_post_service.domain;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.CommentReaction;
import post.service.be_post_service.repositories.CommentReactionRepository;

@Domain
public class CommentReactionDomain extends BaseDomain<CommentReaction, UUID> {
    private final CommentReactionRepository commentReactionRepository;

    @Autowired
    public CommentReactionDomain(CommentReactionRepository commentReactionRepository) {
        super(commentReactionRepository);
        this.commentReactionRepository = commentReactionRepository;
    }

    public UUID createCommentReaction(CommentReaction commentReaction) {
        return commentReactionRepository.save(commentReaction).getId();
    }
}

