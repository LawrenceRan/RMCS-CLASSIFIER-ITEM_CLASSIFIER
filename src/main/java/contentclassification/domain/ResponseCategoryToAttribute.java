package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 6/3/16.
 */
public class ResponseCategoryToAttribute {
    private String category;
    private List<String> attributes;

    public ResponseCategoryToAttribute() {
    }

    public ResponseCategoryToAttribute(String category, List<String> attributes){
        this.category = category;
        this.attributes = attributes;
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

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.category);
        hashCodeBuilder.append(this.attributes);
        return hashCodeBuilder.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof ResponseCategoryToAttribute) {
            ResponseCategoryToAttribute responseCategoryToAttribute = (ResponseCategoryToAttribute) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.category, responseCategoryToAttribute.getCategory());
            equalsBuilder.append(this.attributes, responseCategoryToAttribute.getAttributes());
            return equalsBuilder.isEquals();
        }
        return false;
    }
}
