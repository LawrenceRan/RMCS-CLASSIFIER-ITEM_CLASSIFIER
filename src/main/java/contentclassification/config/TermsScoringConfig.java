package contentclassification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsl_prod_005 on 5/30/16.
 */
@Configuration
public class TermsScoringConfig {
    @Value("${terms.scoring.threshold}")
    private String threshold;

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }
}
