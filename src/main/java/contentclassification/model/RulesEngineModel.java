package contentclassification.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
@RedisHash("ruleEngineModel")
public class RulesEngineModel {
    private static final Logger logger = LoggerFactory.getLogger(RulesEngineModel.class);

    @Id
    private String id;
    private List<Map> rules;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Map> getRules() {
        return rules;
    }

    public void setRules(List<Map> rules) {
        this.rules = rules;
    }

    public String toJson(){
        String jsonStr = null;
        Map<String, Object> rulesMap = new HashMap<>();
        rulesMap.put("id", this.getId());
        rulesMap.put("rules", this.getRules());

        if(!rulesMap.isEmpty()){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                jsonStr = objectMapper.writeValueAsString(rulesMap);
            } catch (JsonProcessingException e){
                logger.debug("Error in processing json.  Message: "+ e.getMessage());
            }
        }
        return jsonStr;
    }

    public static RulesEngineModel fromJson(String jsonStr){
        RulesEngineModel rulesEngineModel = null;

        return rulesEngineModel;
    }
}
