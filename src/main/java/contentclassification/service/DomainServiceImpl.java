package contentclassification.service;

import contentclassification.model.Domain;
import org.springframework.stereotype.Service;

/**
 * Created by rsl_prod_005 on 5/23/16.
 */
@Service
public class DomainServiceImpl extends GraphDBGenericServiceImpl<Domain> {

    @Override
    public Iterable<Domain> findAll() {
        return super.findAll();
    }

    @Override
    public Domain find(Long id) {
        return super.find(id);
    }

    @Override
    public Domain createOrUpdate(Domain object) {
        return super.createOrUpdate(object);
    }

    @Override
    public void delete(Long id) {
        super.delete(id);
    }

    @Override
    public Class<Domain> getEntityType() {
        return Domain.class;
    }
}
