package contentclassification.service;

import contentclassification.domain.JWIImpl;
import contentclassification.domain.WordAndDefinition;
import contentclassification.domain.WordNetImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/11/16.
 */
@Service
public class WordNetService {
    private static Logger logger = LoggerFactory.getLogger(WordNetService.class);

    @Autowired
    private DictionaryIndexerService dictionaryIndexerService;

    public List<Map> getResponse(String query){
        WordNetImpl wordNet = new WordNetImpl(query);
        return wordNet.getResults();
    }

    public List<Map> findStemmers(String query){
        JWIImpl jwi = new JWIImpl(query);
        return jwi.findStems();
    }

    public List<Map> glosses(String query){
        JWIImpl jwi = new JWIImpl(query);
        return jwi.glosses();
    }

    public List<Map> search(String query){
        logger.info("About to process search using index dictionary. Query : "
                + (StringUtils.isNotBlank(query) ? query : "None"));
        List<Map> responseMap = null;
        List<WordAndDefinition> wordAndDefinitions = dictionaryIndexerService.searchDictionaryIndex(query + "*");
        if(wordAndDefinitions != null && !wordAndDefinitions.isEmpty()){
            responseMap = new ArrayList<>();
            for(WordAndDefinition wordAndDefinition : wordAndDefinitions){
                Map<String, String> wordMap = new HashMap<>();
                wordMap.put("word", wordAndDefinition.getWord());
                wordMap.put("definition", wordAndDefinition.getDefinition());
                responseMap.add(wordMap);
            }
        }
        logger.info("Done processing search in dictionary. Query : "
                + (StringUtils.isNotBlank(query) ? query : "None") + " Results : "
                + ((responseMap != null && !responseMap.isEmpty()) ? responseMap.size() : 0));
        return responseMap;
    }
}
