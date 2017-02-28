package contentclassification.model;

import contentclassification.domain.Languages;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 2/27/17.
 */
@Document(collection = "PartsOfSpeech")
public class PartsOfSpeech{
    @Id
    private String id;

    private Languages languages;
    private String pos;

    @Indexed
    private String initial;

    @Indexed
    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Languages getLanguages() {
        return languages;
    }

    public void setLanguages(Languages languages) {
        this.languages = languages;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PartsOfSpeech)) return false;

        PartsOfSpeech that = (PartsOfSpeech) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(languages, that.languages)
                .append(pos, that.pos)
                .append(initial, that.initial)
                .append(token, that.token)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(languages)
                .append(pos)
                .append(initial)
                .append(token)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PartsOfSpeech{" +
                "id='" + id + '\'' +
                ", languages=" + languages +
                ", pos='" + pos + '\'' +
                ", initial='" + initial + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("languages", (this.languages != null) ? this.languages.toString() : null);
        map.put("pos", this.pos);
        map.put("initial", this.initial);
        map.put("token", this.token);
        return map;
    }
}
