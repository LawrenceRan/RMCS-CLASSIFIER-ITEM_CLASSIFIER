package contentclassification.service;

import contentclassification.model.Color;
import org.springframework.stereotype.Service;

/**
 * Created by rsl_prod_005 on 5/23/16.
 */
@Service
public class ColorServiceImpl extends GraphDBGenericServiceImpl<Color> {

    @Override
    public Iterable<Color> findAll() {
        return super.findAll();
    }

    @Override
    public Color createOrUpdate(Color object) {
        return super.createOrUpdate(object);
    }

    @Override
    public Color find(Long id) {
        return super.find(id);
    }

    @Override
    public void delete(Long id) {
        super.delete(id);
    }

    @Override
    public Class<Color> getEntityType() {
        return Color.class;
    }
}
