package post.service.be_post_service.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.CommentStory;
import post.service.be_post_service.repositories.CommentStoryRepository;

@Domain
public class CommentStoryDomain extends BaseDomain<CommentStory, UUID> {

    private final CommentStoryRepository commentStoryRepository;

    @Autowired
    public CommentStoryDomain(CommentStoryRepository commentStoryRepository) {
        super(commentStoryRepository);
        this.commentStoryRepository = commentStoryRepository;
    }

    public UUID createCommentStory(CommentStory commentStory) {
        return commentStoryRepository.save(commentStory).getId();
    }

    public List<CommentStory> getByStoryId(UUID storyId) {
        return commentStoryRepository.getByStoryId(storyId);
    }

    public List<CommentStory> getByUserId(UUID userId) {
        return commentStoryRepository.getByUserId(userId);
    }
}