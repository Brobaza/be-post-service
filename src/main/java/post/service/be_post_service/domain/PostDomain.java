package post.service.be_post_service.domain;

import java.util.UUID;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import post.service.be_post_service.base.BaseDomain;
import post.service.be_post_service.custom.Domain;
import post.service.be_post_service.entity.Post;
import post.service.be_post_service.enums.PostType;
import post.service.be_post_service.repositories.PostRepository;

@Domain
public class PostDomain extends BaseDomain<Post, UUID> {
    private final PostRepository postRepository;

    @Autowired
    public PostDomain(PostRepository postRepository) {
        super(postRepository);
        this.postRepository = postRepository;
    }

    public UUID createPost(Post post) {
        return postRepository.save(post).getId();
    }
    public Post getParentPost(UUID id) {
        return postRepository.getParentPost(id);
    }
    public Post getPostById(UUID id){
        return postRepository.getPostById(id);
    }
    public List<Post> getPostsByUserId(UUID userId){
        return postRepository.getPostsByUserId(userId);
    }
    public List<Post> getByPostType(UUID userId, List<PostType> postType){
        return postRepository.getPostByPostType(userId, postType);
    }
}
