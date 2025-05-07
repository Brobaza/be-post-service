package post.service.be_post_service.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import post.service.be_post_service.domain.PostDomain;

@Service
public class PostService {

    private final PostDomain postDomain;

    @Autowired
    public PostService(PostDomain postDomain) {
        this.postDomain = postDomain;
    }

    public void createPost(String title, String content, List<String> images) {

    }
}
