package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by rsl_prod_005 on 10/12/16.
 */
public class LanguageDictionary {
    private Languages language;
    private List<WordAndDefinition> wordAndDefinitionList;

    public Languages getLanguage() {
        return language;
    }

    public void setLanguage(Languages language) {
        this.language = language;
    }

    public List<WordAndDefinition> getWordAndDefinitionList() {
        return wordAndDefinitionList;
    }

    public void setWordAndDefinitionList(List<WordAndDefinition> wordAndDefinitionList) {
        this.wordAndDefinitionList = wordAndDefinitionList;
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
        LanguageDictionary rhs = (LanguageDictionary) obj;
        return new EqualsBuilder()
                .append(this.language, rhs.language)
                .append(this.wordAndDefinitionList, rhs.wordAndDefinitionList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(language)
                .append(wordAndDefinitionList)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "LanguageDictionary{" +
                "language=" + language +
                ", wordAndDefinitionList=" + wordAndDefinitionList +
                '}';
    }
}
