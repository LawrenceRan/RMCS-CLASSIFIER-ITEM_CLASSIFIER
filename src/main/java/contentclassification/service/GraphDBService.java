package contentclassification.service;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
public interface GraphDBService<T> {
    Iterable<T> findAll();
    T find(Long id);
    void delete(Long id);
    T createOrUpdate(T object);
}
