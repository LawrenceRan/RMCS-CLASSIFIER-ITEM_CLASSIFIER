package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/24/16.
 */
public enum RuleEngineDataSetEnum {
    TITLE("title");

    private final String data;

    RuleEngineDataSetEnum(String data){
        this.data = data;
    }

    @Override
    public String toString(){
        return this.data;
    }

    public static RuleEngineDataSetEnum fromString(String dataset){
        RuleEngineDataSetEnum ruleEngineDataSetEnum = null;
        if(StringUtils.isNotBlank(dataset)){
            List<RuleEngineDataSetEnum> rulesEngineDataSetList = Arrays.asList(RuleEngineDataSetEnum.values());
            if(!rulesEngineDataSetList.isEmpty()){
                for(RuleEngineDataSetEnum r : rulesEngineDataSetList) {
                    if (dataset.equalsIgnoreCase(r.toString())) {
                        ruleEngineDataSetEnum = r;
                    }
                }
            }
        }
        return ruleEngineDataSetEnum;
    }
}
