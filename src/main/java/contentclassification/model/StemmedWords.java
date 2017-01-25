package contentclassification.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by rsl_prod_005 on 1/23/17.
 */
@Document(collection = "StemmedWords")
public class StemmedWords {
    private String id;
    @Indexed
    private String term;
    @Indexed
    private String stem;
    private Date createdOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "StemmedWords{" +
                "id='" + id + '\'' +
                ", term='" + term + '\'' +
                ", stem='" + stem + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof StemmedWords)) return false;

        StemmedWords that = (StemmedWords) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(term, that.term)
                .append(stem, that.stem)
                .append(createdOn, that.createdOn)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(term)
                .append(stem)
                .append(createdOn)
                .toHashCode();
    }
}
