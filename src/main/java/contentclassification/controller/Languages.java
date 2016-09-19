package contentclassification.controller;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 9/7/16.
 */
public enum Languages {
    ENGLISH("en");

    private final String language;

    Languages(String language){
        this.language = language;
    }

    @Override
    public String toString(){
        return this.language;
    }

    public static Languages fromString(String language){
        Languages languages = null;
        if(StringUtils.isNotBlank(language)){
            List<Languages> languagesList = Arrays.asList(Languages.values());
            if(!languagesList.isEmpty()){
                for(Languages l : languagesList){
                    if(language.equalsIgnoreCase(l.toString())){
                        languages = l;
                    }
                }
            }
        }
        return languages;
    }
}
