package contentclassification.service;

import contentclassification.model.StemmedWords;
import contentclassification.model.StemmedWordsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by rsl_prod_005 on 1/23/17.
 */
@Service
public class StemmedWordsService {
    @Autowired
    private StemmedWordsRepository stemmedWordsRepository;

    public StemmedWords findByTerm(String term){
        return stemmedWordsRepository.findByTerm(term);
    }

    public StemmedWords add(StemmedWords stemmedWords){
        return stemmedWordsRepository.save(stemmedWords);
    }
}
