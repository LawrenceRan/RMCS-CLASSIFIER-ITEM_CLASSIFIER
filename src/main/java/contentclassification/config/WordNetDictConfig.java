package contentclassification.config;

import org.apache.xpath.operations.Bool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by rsl_prod_005 on 5/11/16.
 */
@Configuration
@ConfigurationProperties(locations = "classpath:wordnet.properties", prefix = "wordnet")
@PropertySource(value = {"classpath:wordnet.properties"})
public class WordNetDictConfig {
    @Value("${wordnet.database.dir}")
    private String dict;
    private String value;
    @Value("${wordnet.display.results}")
    private String displayResults;

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public String getValue() {
        return "wordnet.database.dir";
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplayResults() {
        return displayResults;
    }

    public void setDisplayResults(String displayResults) {
        this.displayResults = displayResults;
    }

    public boolean getDisplayResultsBool(){
        return Boolean.parseBoolean(this.displayResults);
    }
}
