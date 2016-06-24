package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/23/16.
 */
public enum RulesEngineTask {
    OCCURRENCE("occurrence");

    private final String task;

    RulesEngineTask(String task){
        this.task = task;
    }

    @Override
    public String toString(){
        return this.task;
    }

    public static RulesEngineTask fromString(String task){
        RulesEngineTask rulesEngineTask = null;
        if(StringUtils.isNotBlank(task)){
            List<RulesEngineTask> rulesEngineTaskList = Arrays.asList(RulesEngineTask.values());
            if(!rulesEngineTaskList.isEmpty()){
                for(RulesEngineTask r : rulesEngineTaskList){
                    if(task.equalsIgnoreCase(r.toString())){
                        rulesEngineTask = r;
                    }
                }
            }
        }
        return rulesEngineTask;
    }
}
