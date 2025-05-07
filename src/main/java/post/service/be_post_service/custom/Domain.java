package post.service.be_post_service.custom;

import org.springframework.stereotype.Service;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface Domain {
}
