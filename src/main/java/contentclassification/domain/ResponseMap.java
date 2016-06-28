package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/27/16.
 */
public enum ResponseMap {
    CLASSIFICATION("classification"), GENDER("gender"), GENRE("genre"), SUB_GENRE("subGenres"), COLORS("colors"),
    MATERIALS("materials"), SIZES("sizes"), PRICING("pricing"), BRAND("brand"), IS_LUXURY("isLuxury");

    private final String response;


    ResponseMap(String response){
        this.response = response;
    }

    @Override
    public String toString(){
        return this.response;
    }

    public ResponseMap fromString(String response){
        ResponseMap responseMap = null;
        if(StringUtils.isNotBlank(response)){
            List<ResponseMap> responseMapList = Arrays.asList(ResponseMap.values());
            if(!responseMapList.isEmpty()){
                for(ResponseMap r : responseMapList){
                    if(response.equalsIgnoreCase(r.toString())){
                        responseMap = r;
                    }
                }
            }
        }
        return responseMap;
    }
}
