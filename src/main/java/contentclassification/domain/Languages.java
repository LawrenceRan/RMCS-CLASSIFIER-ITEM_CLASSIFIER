package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 10/12/16.
 */
public enum Languages {
    EN("English", "en"), ES("Spanish", "es"), FR("French", "fr");

    private final String language;
    private final String initial;

    Languages(String language, String initial)
    {
        this.language = language;
        this.initial = initial;
    }

    @Override
    public String toString(){
        return this.language;
    }

    public String toInitial(){
        return this.initial;
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
