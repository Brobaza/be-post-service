package post.service.be_post_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "post.service.be_post_service")
@EntityScan("post.service.be_post_service.entity")
@EnableJpaRepositories("post.service.be_post_service.repositories")
public class BePostServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(BePostServiceApplication.class, args);
	}
}
