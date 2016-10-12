package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by rsl_prod_005 on 10/12/16.
 */
public class WordAndDefinition {
    private String word;
    private String definition;

    public WordAndDefinition(String word) {
        this.word = word;
    }

    public WordAndDefinition(String word, String definition) {
        this.word = word;
        this.definition = definition;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }


    @Override
    public String toString() {
        return "WordAndDefinition{" +
                "word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                '}';
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
        WordAndDefinition rhs = (WordAndDefinition) obj;
        return new EqualsBuilder()
                .append(this.word, rhs.word)
                .append(this.definition, rhs.definition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(word)
                .append(definition)
                .toHashCode();
    }
}
