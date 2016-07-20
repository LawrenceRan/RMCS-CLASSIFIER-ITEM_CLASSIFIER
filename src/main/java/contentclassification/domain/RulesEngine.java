package contentclassification.domain;

import contentclassification.model.RulesEngineModel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
public class RulesEngine {
    private static final Logger logger = LoggerFactory.getLogger(RulesEngine.class);

    private Long id;
    private List<Map> rule;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Map> getRule() {
        return rule;
    }

    public void setRule(List<Map> rule) {
        this.rule = rule;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.getId());
        hashCodeBuilder.append(this.getRule());
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof RulesEngine){
            RulesEngine rulesEngine = (RulesEngine) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.getId(), rulesEngine.getId());
            equalsBuilder.append(this.getRule(), rulesEngine.getRule());
            return equalsBuilder.isEquals();
        }
        return false;
    }


    public static List<RulesEngineModel> loadRules(){
        List<RulesEngineModel> rulesEngineModels = new ArrayList<>();
        try {
            ClassLoader classLoader = RulesEngine.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("RulesEngine");
            if(inputStream != null){
                try{
                    Yaml yaml = new Yaml();
                    Map<Integer, Object> rules = (Map<Integer, Object>) yaml.load(inputStream);
                    if(!rules.isEmpty()){
                        for(Map.Entry<Integer, Object> entry : rules.entrySet()) {
                            RulesEngineModel rulesEngineModel = new RulesEngineModel();
                            String ruleId = entry.getKey().toString();
                            String prefix = RulesEngineModel.class.getSimpleName().toLowerCase();
                            String id = prefix+":"+ruleId;
                            rulesEngineModel.setId(id);
                            if(entry.getValue() instanceof List){
                                List<Map> rulesMap = (List<Map>) entry.getValue();
                                rulesEngineModel.setRules(rulesMap);
                                rulesEngineModels.add(rulesEngineModel);
                            }
                        }
                    }
                } catch (Exception e){
                    logger.debug("Error message: "+ e.getMessage());
                }
            }
        } catch (Exception e){
            logger.debug("Error in loading rules. Message: "+ e.getMessage());
        }
        return rulesEngineModels;
    }
}
