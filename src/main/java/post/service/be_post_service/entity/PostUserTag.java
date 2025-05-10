
package post.service.be_post_service.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import post.service.be_post_service.base.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "post_user_tag", indexes = {
        @Index(name = "index_post_tag_user_id", columnList = "user_id"),
        @Index(name = "idx_post_tag_id", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUserTag extends BaseEntity<UUID> {
    @Column(name = "user_id", nullable = false)
    private UUID user_id;
    @Column(name = "post_id", nullable = false)
    private UUID post_id;
    @Column(name = "start_index", nullable = false)
    private int start_index;
    @Column(name = "end_index", nullable = false)
    private int end_index;
}
