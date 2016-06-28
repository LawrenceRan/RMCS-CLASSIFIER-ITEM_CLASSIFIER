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
    private List<String> materials;
    private List<String> sizes;
    private Map<String, Object> pricing;
    private String brand;
    private String isLuxury;

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

    public List<String> getMaterials() {
        return materials;
    }

    public void setMaterials(List<String> materials) {
        this.materials = materials;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public Map<String, Object> getPricing() {
        return pricing;
    }

    public void setPricing(Map<String, Object> pricing) {
        this.pricing = pricing;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getIsLuxury() {
        return isLuxury;
    }

    public void setIsLuxury(String isLuxury) {
        this.isLuxury = isLuxury;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.category);
        hashCodeBuilder.append(this.attributes);
        hashCodeBuilder.append(this.colors);
        hashCodeBuilder.append(this.gender);
        hashCodeBuilder.append(this.materials);
        hashCodeBuilder.append(this.sizes);
        hashCodeBuilder.append(this.pricing);
        hashCodeBuilder.append(this.brand);
        hashCodeBuilder.append(this.isLuxury);
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
            equalsBuilder.append(this.materials, responseCategoryToAttribute.getMaterials());
            equalsBuilder.append(this.sizes, responseCategoryToAttribute.getSizes());
            equalsBuilder.append(this.pricing, responseCategoryToAttribute.getPricing());
            equalsBuilder.append(this.brand, responseCategoryToAttribute.getBrand());
            equalsBuilder.append(this.isLuxury, responseCategoryToAttribute.getIsLuxury());
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
        map.put("materials", this.materials);
        map.put("sizes", this.sizes);
        map.put("pricing", this.pricing);
        map.put("brand", this.brand);
        map.put("isLuxury", this.isLuxury);
        return map;
    }

    public Map<String, Object> toResponseMap(){
        Map<String, Object> map = new HashMap<>();
        map.put(ResponseMap.GENRE.toString(), this.category);
        map.put(ResponseMap.COLORS.toString(), this.colors);
        map.put(ResponseMap.GENDER.toString(), this.gender);
        map.put(ResponseMap.SUB_GENRE.toString(), this.attributes);
        map.put(ResponseMap.MATERIALS.toString(), this.materials);
        map.put(ResponseMap.SIZES.toString(), this.sizes);
        map.put(ResponseMap.PRICING.toString(),this.pricing);
        map.put(ResponseMap.BRAND.toString(),this.brand);
        map.put(ResponseMap.IS_LUXURY.toString(),this.isLuxury);
        return map;
    }

    @Override
    public String toString() {
        StringBuilder colorsStr = new StringBuilder();
        List<String> colors = this.colors;
        if(colors != null && !colors.isEmpty()){
            int x = 0;
            for(String c : colors){
                if(x < (colors.size() - 1)) {
                    colorsStr.append(c +",");
                } else {
                    colorsStr.append(c);
                }
                x++;
            }
        }

        StringBuilder attrStr = new StringBuilder();
        List<String> attributes = this.attributes;

        if(attributes != null && !attributes.isEmpty()){
            int x = 0;
            for(String attr : attributes){
                if(x < (attributes.size() - 1)){
                    attrStr.append(attr + ",");
                } else {
                    attrStr.append(attr);
                }
                x++;
            }
        }

        return "[ category : " + this.category + ", gender : "+ this.gender  +", colors : "+ colorsStr.toString() +"" +
                ", attributes : "+ attrStr.toString() +", brand : "+ this.brand +", isLuxury : "+ this.isLuxury +"]";
    }
}
