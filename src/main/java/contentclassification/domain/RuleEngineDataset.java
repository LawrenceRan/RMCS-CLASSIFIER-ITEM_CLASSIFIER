package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/24/16.
 */
public enum RuleEngineDataSet {
    TITLE("title");

    private final String data;

    RuleEngineDataSet(String data){
        this.data = data;
    }

    @Override
    public String toString(){
        return this.data;
    }

    public static RuleEngineDataSet fromString(String dataset){
        RuleEngineDataSet ruleEngineDataSet = null;
        if(StringUtils.isNotBlank(dataset)){
            List<RuleEngineDataSet> rulesEngineDataSetList = Arrays.asList(RuleEngineDataSet.values());
            if(!rulesEngineDataSetList.isEmpty()){
                for(RuleEngineDataSet r : rulesEngineDataSetList) {
                    if (dataset.equalsIgnoreCase(r.toString())) {
                        ruleEngineDataSet = r;
                    }
                }
            }
        }
        return ruleEngineDataSet;
    }
}
