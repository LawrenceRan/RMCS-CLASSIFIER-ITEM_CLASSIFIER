package contentclassification.service;

import contentclassification.domain.LearningImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by rsl_prod_005 on 10/12/16.
 */
@Service
public class LearningService implements ILearning{

    @Override
    public Map<String, Object> find(String query){
        Map<String, Object> results = null;
        LearningImpl learning = LearningImpl.setQuery(query);
        String answer = learning.find();
        return results;
    }
}
