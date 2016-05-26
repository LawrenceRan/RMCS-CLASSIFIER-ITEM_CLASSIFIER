package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/26/16.
 */
public enum NameAndContentMetaData{
    NAME("name"), CONTENT("content");

    private final String s;

    NameAndContentMetaData(String s){
        this.s = s;
    }

    @Override
    public String toString(){
        return this.s;
    }

    public static NameAndContentMetaData fromString(String s){
        NameAndContentMetaData n = null;
        if(StringUtils.isNotBlank(s)){
            List<NameAndContentMetaData> nList = Arrays.asList(NameAndContentMetaData.values());
            if(!nList.isEmpty()){
                for(NameAndContentMetaData na : nList){
                    if(s.equalsIgnoreCase(na.toString())){
                        n = na;
                    }
                }
            }
        }
        return n;
    }
}
