package contentclassification.model;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@Repository
public interface KnowledgeSetRepository extends MongoRepository<KnowledgeSet, String> {

}
