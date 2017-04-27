package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by rsl_prod_005 on 4/27/17.
 */
public class LanguagePunctuations implements Serializable{
    private Long typeId;
    private List<PunctuationSign> signList;
    private String comment;
    private Languages language;

    public LanguagePunctuations(Long typeId, List<PunctuationSign> signList) {
        this.typeId = typeId;
        this.signList = signList;
    }

    public Long getTypeId() {
        return typeId;
    }

    public List<PunctuationSign> getSignList() {
        return signList;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Languages getLanguage() {
        return language;
    }

    public void setLanguage(Languages language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof LanguagePunctuations)) return false;

        LanguagePunctuations that = (LanguagePunctuations) o;

        return new EqualsBuilder()
                .append(typeId, that.typeId)
                .append(signList, that.signList)
                .append(comment, that.comment)
                .append(language, that.language)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(typeId)
                .append(signList)
                .append(comment)
                .append(language)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "LanguagePunctuations{" +
                "typeId=" + typeId +
                ", signList=" + signList +
                ", comment='" + comment + '\'' +
                ", language=" + language +
                '}';
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("typeId", this.getTypeId());
        map.put("signList", this.getSignList());
        map.put("comment", this.getComment());
        map.put("language", this.getLanguage());
        return map;
    }
}
