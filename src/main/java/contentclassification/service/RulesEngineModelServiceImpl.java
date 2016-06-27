package contentclassification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import contentclassification.model.RulesEngineModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by rsl_prod_005 on 6/23/16.
 */
@Service
public class RulesEngineModelServiceImpl extends RulesEngineModelService<RulesEngineModel> {
    private static final Logger logger = LoggerFactory.getLogger(RulesEngineModel.class);
    private static Integer COUNT = 100;
    private static final String PREFIX = RulesEngineModel.class.getSimpleName().toLowerCase()+"*";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private RedisConnection redisConnection;

    public RulesEngineModel find(String id){
        RulesEngineModel rulesEngineModel = null;
        if(StringUtils.isNotBlank(id)){

        }
        return rulesEngineModel;
    }

    public List<RulesEngineModel> findAll(){
        List<RulesEngineModel> rulesEngineModelList = new ArrayList<>();
        redisConnection = redisTemplate.getConnectionFactory().getConnection();
        if (redisConnection != null && !redisConnection.isClosed()) {
            List<String> rulesStr =  new ArrayList<>();
            List<String> ruleKeys = new ArrayList<>();
            try {
                ScanOptions scanOptions = ScanOptions.scanOptions()
                        .match(PREFIX)
                        .count(100)
                        .build();

                boolean isDone = false;

                Cursor<byte[]> cursor = redisConnection.scan(scanOptions);
                try {
                    while (cursor.hasNext()) {
                        byte[] bytes = cursor.next();
                        String key = new String(bytes);
                        ruleKeys.add(key);
                    }
                } catch (NoSuchElementException n) {
                    logger.debug("No such element exception. Message: " + n.getMessage());
                }

                if(!ruleKeys.isEmpty()){
                    for(String key : ruleKeys){
                        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
                        String value = valueOperations.get(key);
                        if(StringUtils.isNotBlank(value)){
                            rulesStr.add(value);
                        }
                    }
                }

                if(!rulesStr.isEmpty()){
                    for(String r : rulesStr){
                        RulesEngineModel rulesEngineModel = new RulesEngineModel();
                        ObjectMapper objectMapper = new ObjectMapper();
                        if(StringUtils.isNotBlank(r)) {
                            Map<String, Object> map = objectMapper.readValue(r, HashMap.class);
                            if (!map.isEmpty()) {
                                if (map.containsKey("id")) {
                                    rulesEngineModel.setId(map.get("id").toString());
                                }

                                if (map.containsKey("rules")) {
                                    Object rulesObject = map.get("rules");
                                    if (rulesObject instanceof List) {
                                        List<Map> rules = (ArrayList<Map>) rulesObject;
                                        if (!rules.isEmpty()) {
                                            rulesEngineModel.setRules(rules);
                                        }
                                    }
                                }
                            }
                        }
                        rulesEngineModelList.add(rulesEngineModel);
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting rules from redis. Message: "+ e.getMessage());
            }
        }
        return rulesEngineModelList;
    }

    public void delete(String id){
        super.delete(id);
    }

    public RulesEngineModel createOrUpdate(RulesEngineModel rulesEngineModel){
        return super.createOrUpdate(rulesEngineModel);
    }

    public void insertListOfRule(List<RulesEngineModel> rulesEngineModelList){
        if(!rulesEngineModelList.isEmpty()){
            for(RulesEngineModel rulesEngineModel : rulesEngineModelList){
                ValueOperations<String, String> valueOperations  = redisTemplate.opsForValue();
                valueOperations.set(rulesEngineModel.getId(), rulesEngineModel.toJson());
            }
        }
    }

    @Override
    public Class<RulesEngineModel> getEntityType() {
        return RulesEngineModel.class;
    }
}