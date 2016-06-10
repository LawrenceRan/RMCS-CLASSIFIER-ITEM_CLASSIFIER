package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/10/16.
 */
public enum SizeProperties {
    CLOTHES("clothes"), EXCLUSION_LIST("exclusionList");

    private final String property;

    SizeProperties(String property){
        this.property = property;
    }

    @Override
    public String toString(){
        return this.property;
    }

    public static SizeProperties fromString(String property){
        SizeProperties sizeProperties = null;
        if(StringUtils.isNotBlank(property)){
            List<SizeProperties> sizePropertiesList = Arrays.asList(SizeProperties.values());
            if(!sizePropertiesList.isEmpty()){
                for(SizeProperties s : sizePropertiesList){
                    if(property.equalsIgnoreCase(s.toString())){
                        sizeProperties = s;
                    }
                }
            }
        }
        return sizeProperties;
    }
}
