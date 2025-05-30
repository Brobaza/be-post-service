package post.service.be_post_service.base;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class BaseDto<ID> {

    private ID id;

    private String statusCode;

    private boolean isDeleted;

}