package post.service.be_post_service.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public abstract class BaseService<T extends BaseEntity<ID>, ID> {

    @Autowired
    protected BaseRepository<T, ID> repository;

    protected void throwNotFound(String message) {
        throw new RuntimeException(message != null ? message : "Entity not found");
    }

    public T create(T entity) {
        if (entity.getId() != null) {
            throw new RuntimeException("Entity already has ID, cannot create.");
        }
        return repository.save(entity);
    }

    public List<T> createMany(List<T> entities) {
        return repository.saveAll(entities);
    }

    public T saveOrUpdate(T entity) {
        return repository.save(entity);
    }

    public T findOne(ID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElse(null);
    }

    public T findOneOrFail(ID id) {
        return repository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
    }

    public boolean existsById(ID id) {
        Optional<T> opt = repository.findById(id);
        return opt.isPresent() && !opt.get().isDeleted();
    }

    public List<T> findAll() {
        return repository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .toList();
    }

    public void updateById(ID id, T data) {
        T existing = findOneOrFail(id);
        data.setId(id);
        data.setCreatedBy(existing.getCreatedBy());
        data.setCreatedDate(existing.getCreatedDate());
        repository.save(data);
    }

    public void softDeleteById(ID id) {
        T entity = findOneOrFail(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    public void restoreById(ID id) {
        T entity = findOneOrFail(id);
        entity.setDeleted(false);
        repository.save(entity);
    }

    public void destroyById(ID id) {
        if (!repository.existsById(id)) {
            throwNotFound("Cannot hard-delete non-existing entity");
        }
        repository.deleteById(id);
    }

    public void destroyAll(List<ID> ids) {
        List<T> entities = repository.findAllById(ids);
        repository.deleteAll(entities);
    }
}
