package contentclassification.model;

import contentclassification.domain.Languages;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by rsl_prod_005 on 2/27/17.
 */
@Repository
public interface PartsOfSpeechRepository extends MongoRepository<PartsOfSpeech, String> {
    public PartsOfSpeech findByTokenAndLanguages(String token, Languages languages);
}
