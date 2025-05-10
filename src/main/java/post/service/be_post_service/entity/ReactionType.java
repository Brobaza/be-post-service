package post.service.be_post_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import post.service.be_post_service.base.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "reaction_type", indexes = {
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionType extends BaseEntity<UUID> {
    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;
    @Column(name = "icon", columnDefinition = "TEXT")
    private String icon;
    @Column(name = "display_order")
    private Integer displayOrder;
}
