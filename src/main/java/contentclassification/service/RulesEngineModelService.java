package contentclassification.service;

import contentclassification.domain.RulesEngine;
import contentclassification.model.RulesEngineModel;
import contentclassification.model.RulesEngineModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
public abstract class RulesEngineModelService<T> implements RulesEngineModelRepository<T> {
    private static final Logger logger = LoggerFactory.getLogger(RulesEngineModelService.class);

    @Override
    public List<T> findAll() {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public T find(String id) {
        return null;
    }

    @Override
    public T createOrUpdate(T object) {
        if(object instanceof RulesEngineModel){
            RulesEngineModel rulesEngineModel = (RulesEngineModel) object;
            String key = rulesEngineModel.getId();
            String value = rulesEngineModel.toJson();
            RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
            redisTemplate.opsForValue().set(key, value);

            return object;
        }
        return null;
    }

    public abstract Class<T> getEntityType();
}
