package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 4/27/17.
 */
public class PunctuationSign {
    private String name;
    private String value;

    public PunctuationSign(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PunctuationSign)) return false;

        PunctuationSign that = (PunctuationSign) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PunctuationSign{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", this.getName());
        map.put("value", this.getValue());
        return map;
    }
}
