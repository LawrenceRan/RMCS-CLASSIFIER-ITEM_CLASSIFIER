package contentclassification.service;

import contentclassification.domain.Languages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 10/12/16.
 */
@Service
public class DictionaryIndexerService {
    private static Logger logger = LoggerFactory.getLogger(DictionaryIndexerService.class);
    private String LANGUAGE_CONFIG = "language-dictionaries-config.yml";

    public List<Languages> getSupportedLanguages(){
        return Arrays.asList(Languages.values());
    }

    /**
     * Get language dictionary file name and path.
     * @param languages
     * @return
     */
    public String getLanguageDictionaryFilePath(Languages languages){
        String fileName = null;
        if(languages != null){
            String language = languages.toString();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(LANGUAGE_CONFIG);
            try {
                if (inputStream != null) {
                    Yaml yaml = new Yaml();
                    @SuppressWarnings("unchecked")
                    List<Map> configMaps = (List<Map>) yaml.load(inputStream);
                    if (configMaps != null && !configMaps.isEmpty()) {
                        for (Map configMap : configMaps) {
                            if (configMap.containsKey("language")) {
                                String configLanguage = configMap.get("language").toString();
                                if (language.equalsIgnoreCase(configLanguage)) {
                                    fileName = configMap.get("fileName").toString();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting language dictionary file path. Message : "+ e.getMessage());
            } finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (Exception e){
                        logger.warn("Exception in closing input stream. Message : "+ e.getMessage());
                    }
                }
            }
        }
        return fileName;
    }

    public File getDictionaryFile(String fileName){
        File file = null;
        if(StringUtils.isNotBlank(fileName)){
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(fileName);
            if(url != null){
                file = new File(url.getFile());
            }
        }
        return file;
    }

}
