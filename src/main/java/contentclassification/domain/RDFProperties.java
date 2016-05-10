package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public enum RDFProperties {
    TYPE("type"), COMMENT("comment"), SUBJECT("subject"), LABEL("label"), ABSTRACT("abstract");
    private String property;

    RDFProperties(String property){
        this.property = property;
    }

    public RDFProperties fromString(String property){
        RDFProperties rdfProperty = null;
        List<RDFProperties> rdfProperties = Arrays.asList(RDFProperties.values());
        if(StringUtils.isNotEmpty(property)){
            if(rdfProperties != null && !rdfProperties.isEmpty()){
                for(RDFProperties r : rdfProperties){
                    if(property.equalsIgnoreCase(r.toString())){
                        rdfProperty = r;
                    }
                }
            }
        }
        return rdfProperty;
    }

    @Override
    public String toString(){
        return this.property;
    }
}
