package contentclassification.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
public interface RulesEngineModelRepository<T> {
    List<T> findAll();
    T find(String id);
    void delete(String id);
    T createOrUpdate(T object);
}
