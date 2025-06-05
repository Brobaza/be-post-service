package post.service.be_post_service.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.StoryReaction;
import post.service.be_post_service.repositories.StoryReactionRepository;

@Domain
public class StoryReactionDomain extends BaseDomain<StoryReaction, UUID> {
    private final StoryReactionRepository storyReactionRepository;

    @Autowired
    public StoryReactionDomain(StoryReactionRepository storyReactionRepository) {
        super(storyReactionRepository);
        this.storyReactionRepository = storyReactionRepository;
    }

    public UUID createStoryReaction(StoryReaction storyReaction) {
        return storyReactionRepository.save(storyReaction).getId();

    }
    public List<StoryReaction> getStoryReactionById(UUID storyReactionId) {
        return storyReactionRepository.findById(storyReactionId).map(List::of).orElse(Collections.emptyList());
    }
    public List<StoryReaction> getStoryReactionByStoryId(UUID storyId) {
        return storyReactionRepository.findByStoryId(storyId);
    }
}