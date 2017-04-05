package contentclassification.config;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rsl_prod_005 on 3/31/17.
 */
@Configuration
public class DroolsConfiguration {
    private static Logger logger = LoggerFactory.getLogger(DroolsConfiguration.class);

    private static String DRL_DIR = "drl";

    @Bean
    public StatelessKieSession kieSession(){
        StatelessKieSession kieSession = null;
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            String path = DRL_DIR + "/classification_rules.drl";

            InputStream inputStream = classLoader.getResourceAsStream(path);
            if(inputStream != null) {
                org.kie.api.io.Resource resource = ResourceFactory.newInputStreamResource(inputStream, "UTF-8");
                resource.setSourcePath(path);
                resource.setTargetPath(path);
                kieFileSystem.write(resource);

                KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

                try {
                    kieBuilder.buildAll();
                } catch (Exception e) {
                    logger.warn("Error in building drool rules. Message : " + e.getMessage());
                }

                if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                    throw new RuntimeException("Build Error: " + kieBuilder.getResults().toString());
                }

                KieRepository kieRepository = kieServices.getRepository();
                KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
                kieSession = kieContainer.newStatelessKieSession();
                logger.info("New stateless drool session bean created"
                        + ((kieSession != null) ? kieSession.toString() : "None"));
            } else {
                logger.info("Unable to open stream for DRL files. Path : "
                        + ((StringUtils.isNotBlank(path)) ? path : "None"));
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e){
            logger.warn("Error in getting kie session for rules engine. Message : "+ e.getMessage());
        }

        return kieSession;
    }
}
