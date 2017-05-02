package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 4/28/17.
 */
public class TextModificationIndex {
    private String preValue;
    private String postValue;

    public TextModificationIndex(String preValue, String postValue) {
        this.preValue = preValue;
        this.postValue = postValue;
    }

    public String getPreValue() {
        return preValue;
    }

    public String getPostValue() {
        return postValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TextModificationIndex)) return false;

        TextModificationIndex that = (TextModificationIndex) o;

        return new EqualsBuilder()
                .append(preValue, that.preValue)
                .append(postValue, that.postValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(preValue)
                .append(postValue)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "TextModificationIndex{" +
                "preValue='" + preValue + '\'' +
                ", postValue='" + postValue + '\'' +
                '}';
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("preValue", this.getPreValue());
        map.put("postValue", this.getPostValue());
        return map;
    }

    public static Map<String, TextModificationIndex> toPostValueMap(List<TextModificationIndex> modificationIndices){
        Map<String, TextModificationIndex> postValueMap = new HashMap<>();
        if(modificationIndices != null && !modificationIndices.isEmpty()){
            for(TextModificationIndex textModificationIndex : modificationIndices){
                postValueMap.put(textModificationIndex.getPostValue(), textModificationIndex);
            }
        }
        return postValueMap;
    }

    public static List<String> restorePreValue(List<String> orderedTitles,
                                               List<TextModificationIndex> modificationIndices){
        List<String> restoredValues = new LinkedList<>();
        if(!orderedTitles.isEmpty()){
            Map<String, TextModificationIndex> toPostValueMap = toPostValueMap(modificationIndices);
            for(String title : orderedTitles){
                if(toPostValueMap.containsKey(title)){
                    TextModificationIndex textModificationIndex
                            = toPostValueMap.get(title);
                    if(textModificationIndex != null) {
                        restoredValues.add(textModificationIndex.getPreValue());
                    }
                }
            }
        }
        return restoredValues;
    }
}
