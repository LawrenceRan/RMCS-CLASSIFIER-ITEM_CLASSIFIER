package contentclassification.service;

import contentclassification.domain.LanguagePunctuations;
import contentclassification.domain.Languages;
import contentclassification.domain.PunctuationSign;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 4/27/17.
 * A service class for performing CRUD function on Language punctuations.
 */
@Service
public class LanguagePunctuationService {
    private static Logger logger = LoggerFactory.getLogger(LanguagePunctuationService.class);
    private static String EN_PUNCTUATION = "ymls/en-punctuations.yml";

    /**
     * get all languages punctuations.
     * @return
     */
    public List<LanguagePunctuations> getAllLanguagePunctuations(){
        List<LanguagePunctuations> languagePunctuationsList = null;

        return languagePunctuationsList;
    }


    /**
     * Get list of punctuations sign by passing language.
     * @param languages
     * @return
     */
    public LanguagePunctuations getPunctuationSignByLanguage(Languages languages){
        LanguagePunctuations selectedLanguagePunctuations = null;
        if(languages != null){
            List<LanguagePunctuations> languagePunctuationsList = loadPunctuationSignFromResourceFile();
            if(languagePunctuationsList != null && !languagePunctuationsList.isEmpty()){
                for(LanguagePunctuations languagePunctuations : languagePunctuationsList){
                    if(languagePunctuations.getLanguage().equals(Languages.EN)){
                        selectedLanguagePunctuations = languagePunctuations;
                        break;
                    }
                }
            }
        }
        return selectedLanguagePunctuations;
    }

    public List<PunctuationSign> getPunctuationSignsByLanguageAndType(Languages languages, Long typeId){
        List<PunctuationSign> signs = null;
        if(languages != null && typeId != null){
            LanguagePunctuations languagePunctuations = getPunctuationSignByLanguage(languages);
            if(languagePunctuations != null){
                if(languagePunctuations.getTypeId().equals(typeId)){
                    signs = languagePunctuations.getSignList();
                }
            }
        }
        return signs;
    }

    /**
     * This method is used to load language punctuations from resource file.
     * @return List<PunctuationSign>
     */
    @Cacheable
    public List<LanguagePunctuations> loadPunctuationSignFromResourceFile(){
        List<LanguagePunctuations> languagePunctuationsList = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(EN_PUNCTUATION);
        if(inputStream != null){
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                List<Map> mapList = (List<Map>) yaml.load(inputStream);
                if(!mapList.isEmpty()){
                    languagePunctuationsList = new ArrayList<>();
                    for(Map map : mapList){
                        Long typeId = (map.containsKey("typeId") && map.get("typeId") != null) ? Long.parseLong(map.get("typeId").toString()) : null;
                        String comment = (map.containsKey("comment")) ? map.get("comment").toString() : null;

                        List<PunctuationSign> signs = new ArrayList<>();
                        if(map.containsKey("signs")){
                            Object signsObj = map.get("signs");
                            if(signsObj != null && (signsObj instanceof List)){
                                @SuppressWarnings("unchecked")
                                List<Map> signsMap = (List<Map>) signsObj;
                                if(!signsMap.isEmpty()){
                                    for(Map signMap : signsMap){
                                        String name = null;
                                        String value = null;

                                        if(signMap.containsKey("name")){
                                            name = signMap.get("name").toString();
                                        }

                                        if(signMap.containsKey("value")){
                                            value = signMap.get("value").toString();
                                        }

                                        if(StringUtils.isNotBlank(name) &&  StringUtils.isNotBlank(value)){
                                            PunctuationSign punctuationSign = new PunctuationSign(name, value);
                                            signs.add(punctuationSign);
                                        }
                                    }
                                }
                            }
                        }

                        if(typeId != null) {
                            LanguagePunctuations languagePunctuations = new LanguagePunctuations(typeId, signs);
                            languagePunctuations.setComment(comment);
                            languagePunctuations.setLanguage(Languages.EN);
                            languagePunctuationsList.add(languagePunctuations);
                        }

                    }
                }
            } catch (Exception e){
                logger.warn("Error in occurred while process language punctuation. Message : "+ e.getMessage());
            }
        }
        return languagePunctuationsList;
    }


    /**
     * Remove all punctuations found in a given text of type string by providing a list of punctuation marks.
     * @param text
     * @param punctuationSigns
     * @return
     */
    public String removePunctuations(String text, List<PunctuationSign> punctuationSigns){
        if(punctuationSigns != null && !punctuationSigns.isEmpty()){
            for(PunctuationSign punctuationSign : punctuationSigns){
                String value = punctuationSign.getValue();
                if(StringUtils.isNotBlank(value)){
                    text = text.replace(value, "");
                }
            }
        }
        return text;
    }

    /**
     * Remove all punctuations found in a given list of text of type string by providing a list of punctuation marks.
     * @param texts
     * @param punctuationSigns
     * @return
     */
    public List<String> removePunctuationsFromList(List<String> texts, List<PunctuationSign> punctuationSigns){
        List<String> updatedText = new ArrayList<>();
        if(!texts.isEmpty() && !punctuationSigns.isEmpty()){
            for(String text : texts){
                text = removePunctuations(text, punctuationSigns);
                updatedText.add(text);
            }
        }
        return updatedText;
    }

    public void loadLanguagePunctuationsIntoCache(){
        loadPunctuationSignFromResourceFile();
    }
}
