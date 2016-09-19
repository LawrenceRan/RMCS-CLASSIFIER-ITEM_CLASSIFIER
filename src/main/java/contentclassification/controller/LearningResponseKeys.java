package contentclassification.controller;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 9/7/16.
 */
public enum LearningResponseKeys {
    ENTITIES("entities"), LABELS("labels"), DESCRIPTIONS("descriptions"), SITELINKS("sitelinks"), VALUE("value");

    private final String key;

    LearningResponseKeys(String key){
        this.key = key;
    }

    @Override
    public String toString(){
        return this.key;
    }


    public static LearningResponseKeys fromString(String key){
        LearningResponseKeys learningResponseKeys = null;
        if(StringUtils.isNotBlank(key)){
            List<LearningResponseKeys> learningResponseKeysList = Arrays.asList(LearningResponseKeys.values());
            if(!learningResponseKeysList.isEmpty()){
                for(LearningResponseKeys l : learningResponseKeysList){
                    if(key.equalsIgnoreCase(l.toString())){
                        learningResponseKeys = l;
                    }
                }
            }
        }
        return learningResponseKeys;
    }
}
