package contentclassification.config;

import contentclassification.service.DictionaryIndexerService;
import contentclassification.service.LanguagePunctuationService;
import contentclassification.service.NotificationService;
import contentclassification.service.PreLoadTrainingDataFiles;
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

    @Autowired
    private DictionaryIndexerService dictionaryIndexerService;

    @Autowired
    private PreLoadTrainingDataFiles preLoadTrainingDataFiles;

    @Autowired
    private LanguagePunctuationService punctuationService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        System.setProperty(wordNetDictConfig.getValue(), wordNetDictConfig.getDict());
        System.setProperty("categories.add.top", classificationConfig.getAddTopLevel());
        System.setProperty("clothing.fabric.names.uri", classificationConfig.getFabricNameResource());
        System.setProperty("enable.javascript", classificationConfig.getEnableJavascript());
        System.setProperty("enable.css", classificationConfig.getEnableCss());
        System.setProperty("lucene.indexer.dir", wordNetDictConfig.getLuceneIndexerDir());

        //Load dictionary in to lucene indexer.
        logger.info("About to start indexing language dictionary into indexer.");
        dictionaryIndexerService.loadDefaultDictionaryIntoIndexer();
        logger.info("Done loading default language dictionary into indexer.");

        logger.info("About to preload training data into temporary directory");
        preLoadTrainingDataFiles.createTemp();
        logger.info("Done setting training data into temporary directory");

        logger.info("About to preload language punctuations into cache.");
        punctuationService.loadLanguagePunctuationsIntoCache();
        logger.info("Done loading language punctuations into cache.");
    }
}
