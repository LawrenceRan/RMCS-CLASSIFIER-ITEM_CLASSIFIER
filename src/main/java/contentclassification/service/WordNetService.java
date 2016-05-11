package contentclassification.service;

import contentclassification.domain.WordNetImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/11/16.
 */
@Service
public class WordNetService {
    public List<Map> getResponse(String query){
        WordNetImpl wordNet = new WordNetImpl(query);
        return wordNet.getResults();
    }
}
