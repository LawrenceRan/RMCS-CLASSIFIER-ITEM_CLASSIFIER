package contentclassification.model;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by rsl_prod_005 on 1/23/17.
 */
@Repository
public interface StemmedWordsRepository extends MongoRepository<StemmedWords, String> {
    public StemmedWords findByTerm(String term);
}
