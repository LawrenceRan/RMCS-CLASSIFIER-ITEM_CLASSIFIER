package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 6/3/16.
 */
public class ResponseCategoryToAttribute {
    private String category;
    private List<String> attributes;
    private List<String> colors;
    private String gender;

    public ResponseCategoryToAttribute() {
    }

    public ResponseCategoryToAttribute(String category,
                                       List<String> attributes,
                                       List<String> colors){
        this.category = category;
        this.attributes = attributes;
        this.colors = colors;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.category);
        hashCodeBuilder.append(this.attributes);
        hashCodeBuilder.append(this.colors);
        hashCodeBuilder.append(this.gender);
        return hashCodeBuilder.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof ResponseCategoryToAttribute) {
            ResponseCategoryToAttribute responseCategoryToAttribute = (ResponseCategoryToAttribute) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.category, responseCategoryToAttribute.getCategory());
            equalsBuilder.append(this.attributes, responseCategoryToAttribute.getAttributes());
            equalsBuilder.append(this.colors, responseCategoryToAttribute.getColors());
            equalsBuilder.append(this.gender, responseCategoryToAttribute.getGender());
            return equalsBuilder.isEquals();
        }
        return false;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("category", this.category);
        map.put("colors", this.colors);
        map.put("gender", this.gender);
        map.put("attributes", this.attributes);
        return map;
    }
}
