package contentclassification.jobs;

import contentclassification.domain.FabricName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/1/16.
 */
@Component
public class FabricNames {
    private static final Logger logger = LoggerFactory.getLogger(FabricNames.class);

    @Scheduled(fixedRate = 14400000)
    public void getFabricNames(){
        List<FabricName> fabricNameList = FabricName.getFabricNamesFromExternalUri();
        if(!fabricNameList.isEmpty()) {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("fabric-names.yml");
            if(inputStream != null) {
                FabricName.writeFabricNames(fabricNameList, inputStream);
                logger.info("Fabrics: " + fabricNameList.size());

                try {
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
    }
}
