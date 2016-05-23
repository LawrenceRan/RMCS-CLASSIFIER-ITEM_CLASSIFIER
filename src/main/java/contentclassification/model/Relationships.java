package contentclassification.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
public enum Relationships {
    IS_A("IS_A");

    private final String relates;

    Relationships(String relates){ this.relates = relates; }

    @Override
    public String toString(){ return this.relates; }

    public static Relationships fromString(String relates){
        Relationships relationships = null;
        if(StringUtils.isNotBlank(relates)){
            List<Relationships> relationshipsList = Arrays.asList(Relationships.values());
            if(!relationshipsList.isEmpty()){
                for(Relationships r : relationshipsList){
                    if(relates.equalsIgnoreCase(r.toString())){
                        relationships = r;
                    }
                }
            }
        }
        return relationships;
    }
}
