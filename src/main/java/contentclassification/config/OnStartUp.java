package contentclassification.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by rsl_prod_005 on 5/11/16.
 */
@Component
public class OnStartUp implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    WordNetDictConfig wordNetDictConfig;

    @Autowired
    ClassificationConfig classificationConfig;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        System.setProperty(wordNetDictConfig.getValue(), wordNetDictConfig.getDict());
        System.setProperty("categories.add.top", classificationConfig.getAddTopLevel());
        System.setProperty("clothing.fabric.names.uri", classificationConfig.getFabricNameResource());
    }
}
