package post.service.be_post_service.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID> extends JpaRepository<T, ID> {

    // @Modifying
    // @Transactional
    // @Query("UPDATE #{#entityName} t SET t.statusCode = :statusCode WHERE t.id = :id")
    // void updateEntity(@Param("id") String id, @Param("statusCode") String statusCode);
}
