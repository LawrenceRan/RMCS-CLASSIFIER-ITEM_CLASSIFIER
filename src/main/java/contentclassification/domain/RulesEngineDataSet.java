package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/23/16.
 */
public class RulesEngineDataSet {
    private String title;
    private String body;
    private List<Map> metas;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Map> getMetas() {
        return metas;
    }

    public void setMetas(List<Map> metas) {
        this.metas = metas;
    }

    @Override
    public String toString(){
        return "[ title : "+ this.getTitle() + "]";
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.title);
        hashCodeBuilder.append(this.body);
        hashCodeBuilder.append(this.metas);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof RulesEngineDataSet) {
            RulesEngineDataSet rulesEngineDataSet = (RulesEngineDataSet) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.title, rulesEngineDataSet.getTitle());
            equalsBuilder.append(this.body, rulesEngineDataSet.getBody());
            equalsBuilder.append(this.metas, rulesEngineDataSet.getMetas());
            return equalsBuilder.isEquals();
        }
        return false;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("title", this.title);
        map.put("body", this.body);
        map.put("metas", this.metas);
        return map;
    }
}
