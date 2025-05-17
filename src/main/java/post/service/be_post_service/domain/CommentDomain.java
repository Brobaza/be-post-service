package post.service.be_post_service.domain;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.Comment;
import post.service.be_post_service.repositories.CommentRepository;
import java.util.List;
@Domain
public class CommentDomain extends BaseDomain<Comment, UUID> {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentDomain(CommentRepository commentRepository) {
        super(commentRepository);
        this.commentRepository = commentRepository;
    }

    public UUID createComment(Comment comment) {
        return commentRepository.save(comment).getId();
    }
    public Comment getParentComment(UUID id) {
        return commentRepository.getParentComment(id);
    }
    public List<Comment> getByPostId(UUID postId){
        return commentRepository.getByPostId(postId);
    }
}

