package contentclassification.service;

import contentclassification.domain.Languages;
import contentclassification.model.PartsOfSpeech;
import contentclassification.model.PartsOfSpeechRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by rsl_prod_005 on 2/28/17.
 */
@Service
public class PartsOfSpeechService {
    private static Logger logger = LoggerFactory.getLogger(PartsOfSpeechService.class);

    @Autowired
    private PartsOfSpeechRepository partsOfSpeechRepository;

    public PartsOfSpeech findByTokenAndLanguages(String token, Languages languages){
        PartsOfSpeech partsOfSpeech = null;
        if(StringUtils.isNotBlank(token) && languages != null){
            partsOfSpeech = partsOfSpeechRepository.findByTokenAndLanguages(token, languages);
        }
        return partsOfSpeech;
    }

    public List<PartsOfSpeech> findBySentenceAndLanguage(String sentence, Languages languages){
        List<PartsOfSpeech> partsOfSpeech = null;
        if(StringUtils.isNotBlank(sentence) && languages != null){
            partsOfSpeech = partsOfSpeechRepository.findBySentenceAndLanguages(sentence, languages);
        }
        return partsOfSpeech;
    }

    public PartsOfSpeech add(Languages languages, String token, String pos, String initial){
        PartsOfSpeech partsOfSpeech = null;
        if(languages != null && StringUtils.isNotBlank(token)
                && StringUtils.isNotBlank(pos) && StringUtils.isNotBlank(initial)){
            partsOfSpeech = new PartsOfSpeech();
            partsOfSpeech.setLanguages(languages);
            partsOfSpeech.setPos(pos);
            partsOfSpeech.setToken(token);
            partsOfSpeech.setInitial(initial);
        }
        return partsOfSpeech;
    }

    @Async
    public void processPartsOfSpeech(List<PartsOfSpeech> partsOfSpeechList){
        logger.info("About to process parts of speech caching. List : "
                + ((partsOfSpeechList != null && !partsOfSpeechList.isEmpty()) ? partsOfSpeechList.toString() : "None"));
        if(partsOfSpeechList != null && !partsOfSpeechList.isEmpty()){
            for(PartsOfSpeech partsOfSpeech : partsOfSpeechList){
                partsOfSpeechRepository.save(partsOfSpeech);
            }
        }
        logger.info("Done processing parts of speech caching for list. List : "
                + ((partsOfSpeechList != null && !partsOfSpeechList.isEmpty()) ? partsOfSpeechList.toString() : "None"));
    }
}
