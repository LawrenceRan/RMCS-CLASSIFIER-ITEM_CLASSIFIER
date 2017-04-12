package contentclassification.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by rsl_prod_005 on 4/12/17.
 */
@Component
public class PreLoadTrainingDataFiles {
    private static Logger logger = LoggerFactory.getLogger(PreLoadTrainingDataFiles.class);

    private static String POS_FILE_PATH = "enPosMaxentPath";

    @Autowired
    private Environment environment;

    public void createTemp(){
        logger.info("About to check if en-pos-maxent file path.");
        String path = environment.getProperty(POS_FILE_PATH);
        logger.info("Done checking if en-pos-maxent file path. Path : "
                + (StringUtils.isNotBlank(path) ? path : "None"));
    }

    @PostConstruct
    public void initialize(){
        logger.info("About to write en-pos-maxent into temp directory.");
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("en-pos-maxent.bin");
        if(inputStream != null ) {
            try {
                Path temp = Files.createTempFile("en-pos-maxent", ".bin");
                Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
                File file = temp.toFile();
                if(file.exists() && file.canRead()) {
                    String path = file.getAbsolutePath();
                    if (StringUtils.isNotBlank(path)) {
                        System.setProperty(POS_FILE_PATH, path);
                    }
                    logger.info("File path : " + (StringUtils.isNotBlank(path) ? path : "None"));
                }
            } catch (Exception e){
                logger.warn("Error in preloading en-pos-maxent.bin file. Message : "+ e.getMessage());
            }
        }
        logger.info("Done writing en-pos-maxent into temp directory.");
    }

    @PreDestroy
    public void cleanup(){
        logger.info("About to cleaning up temporary file.");
        String path = environment.getProperty(POS_FILE_PATH);
        if(StringUtils.isNotBlank(path)){
            try {
                File file = new File(path);
                Boolean isDeleted = file.delete();
                logger.info("Temporary file : "+ path + " deletd : "+ isDeleted);
            } catch (Exception e){
                logger.warn("Error in deleting temporary file : "+ path + " Message : "+ e.getMessage());
            }
        }
        logger.info("Done cleaning up temporary file.");
    }
}
