package post.service.be_post_service.domain;

import java.util.UUID;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.Story;
import post.service.be_post_service.repositories.StoryRepository;

@Domain
public class StoryDomain extends BaseDomain<Story, UUID> {
    private final StoryRepository storyRepository;

    @Autowired
    public StoryDomain(StoryRepository storyRepository) {
        super(storyRepository);
        this.storyRepository = storyRepository;
    }

    public UUID createStory(Story story) {
        return storyRepository.save(story).getId();
    }

    public Story getStoryById(UUID storyId) {
        return storyRepository.getStoryById(storyId);
    }

    public List<Story> getStoriesByUserId(UUID userId) {
        return storyRepository.getStoriesByUserId(userId);
    }

    public List<Story> getActiveStoriesByUserIds(List<UUID> userIds, Date now) {
        return storyRepository.getActiveStoriesByUserIds(userIds, now);
    }

    public List<Story> getAllActiveStories(Date now) {
        return storyRepository.getAllActiveStories(now);
    }
}
