package contentclassification.jobs;

import contentclassification.domain.RulesEngine;
import contentclassification.model.RulesEngineModel;
import contentclassification.service.RulesEngineModelServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by rsl_prod_005 on 6/22/16.
 */
@Component
public class RulesEngineManager {
    private static final Logger logger = LoggerFactory.getLogger(RulesEngineManager.class);

    @Autowired
    private RulesEngineModelServiceImpl engineModelService;

    @Scheduled(fixedRate = 14400000)
    public void loadRulesFromFile(){
        List<RulesEngineModel> rulesEngineModelList = RulesEngine.loadRules();
        if(!rulesEngineModelList.isEmpty()){
            engineModelService.insertListOfRule(rulesEngineModelList);
        }
        logger.info("Rules engine updated into Redis data store.");
    }
}
