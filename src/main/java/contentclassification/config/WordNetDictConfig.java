package contentclassification.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

    @Value("${lucene.indexer.dir}")
    private String luceneIndexerDir;

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

    public String getLuceneIndexerDir() {
        return luceneIndexerDir;
    }

    public void setLuceneIndexerDir(String luceneIndexerDir) {
        this.luceneIndexerDir = luceneIndexerDir;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        WordNetDictConfig rhs = (WordNetDictConfig) obj;
        return new EqualsBuilder()
                .append(this.dict, rhs.dict)
                .append(this.value, rhs.value)
                .append(this.displayResults, rhs.displayResults)
                .append(this.luceneIndexerDir, rhs.luceneIndexerDir)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(dict)
                .append(value)
                .append(displayResults)
                .append(luceneIndexerDir)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "WordNetDictConfig{" +
                "dict='" + dict + '\'' +
                ", value='" + value + '\'' +
                ", displayResults='" + displayResults + '\'' +
                ", luceneIndexerDir='" + luceneIndexerDir + '\'' +
                '}';
    }
}
