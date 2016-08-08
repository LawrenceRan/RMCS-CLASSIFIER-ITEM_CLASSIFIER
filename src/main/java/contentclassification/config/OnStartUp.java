package contentclassification.config;

import contentclassification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by rsl_prod_005 on 5/11/16.
 */
@Component
public class OnStartUp implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OnStartUp.class);

    @Autowired
    WordNetDictConfig wordNetDictConfig;

    @Autowired
    ClassificationConfig classificationConfig;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        System.setProperty(wordNetDictConfig.getValue(), wordNetDictConfig.getDict());
        System.setProperty("categories.add.top", classificationConfig.getAddTopLevel());
        System.setProperty("clothing.fabric.names.uri", classificationConfig.getFabricNameResource());
        System.setProperty("enable.javascript", classificationConfig.getEnableJavascript());
        System.setProperty("enable.css", classificationConfig.getEnableCss());
        try {
            notificationService.sendNotification();
        } catch (InterruptedException e) {
            logger.debug("Error in sending start up message. Message: ", e.getMessage());
        }
    }
}
