package contentclassification.service;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 8/3/16.
 */
public enum ThirdPartyProviderResponseKeys {
    CLOTH_SIZE("ClothingSize"), BRAND("Brand"), DEPARTMENT("Department"), SIZE("Size"),
    ITEM_ATTRIBUTES("ItemAttributes"), PRODUCT_TYPE_NAME("ProductTypeName"), TITLE("Title"), COLOR("Color");

    private final String key;

    ThirdPartyProviderResponseKeys(String key){
        this.key = key;
    }

    @Override
    public String toString(){
        return this.key;
    }

    public static ThirdPartyProviderResponseKeys fromString(String key){
        ThirdPartyProviderResponseKeys thirdPartyProviderResponseKeys = null;
        if(StringUtils.isNotBlank(key)){
            List<ThirdPartyProviderResponseKeys> thirdPartyProviderResponseKeysList =
                    Arrays.asList(ThirdPartyProviderResponseKeys.values());
            if(thirdPartyProviderResponseKeysList.isEmpty()){
                for(ThirdPartyProviderResponseKeys t : thirdPartyProviderResponseKeysList){
                    if(key.equalsIgnoreCase(t.toString())){
                        thirdPartyProviderResponseKeys = t;
                    }
                }
            }
        }
        return thirdPartyProviderResponseKeys;
    }
}
