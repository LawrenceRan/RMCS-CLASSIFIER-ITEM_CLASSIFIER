package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/24/16.
 */
public enum RuleEngineInput {
    ATTRIBUTES("attributes");

    private final String input;

    RuleEngineInput(String input){
        this.input = input;
    }

    @Override
    public String toString(){
        return this.input;
    }

    public static RuleEngineInput fromString(String input){
        RuleEngineInput ruleEngineInput = null;
        if(StringUtils.isNotBlank(input)){
            List<RuleEngineInput> ruleEngineInputList = Arrays.asList(RuleEngineInput.values());
            if(!ruleEngineInputList.isEmpty()){
                for(RuleEngineInput r : ruleEngineInputList){
                    if(input.equalsIgnoreCase(r.toString())){
                        ruleEngineInput = r;
                    }
                }
            }
        }
        return ruleEngineInput;
    }
}
