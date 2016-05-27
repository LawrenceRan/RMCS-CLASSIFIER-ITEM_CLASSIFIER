package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This class holds serves as the data structure for identified areas of a content document
 *
 * These are as follows:
 * 1. Main body of content
 * 2. Keywords from meta data
 * 3. Possible title of the said content
 * 4. Description taken from meta data of the content item.
 *
 * Created by rsl_prod_005 on 5/27/16.
 */
public enum ContentAreaGroupings {
    BODY("body"), TITLE("title"), KEYWORDS("keywords"), DESCRIPTION("description");

    private final String group;

    ContentAreaGroupings(String group){
        this.group = group;
    }

    @Override
    public String toString(){
        return this.group;
    }

    public static ContentAreaGroupings fromString(String group){
        ContentAreaGroupings contentAreaGroupings = null;
        if(StringUtils.isNotBlank(group)){
            List<ContentAreaGroupings> contentAreaGroupingsList = Arrays.asList(ContentAreaGroupings.values());
            if(!contentAreaGroupingsList.isEmpty()){
                for(ContentAreaGroupings c : contentAreaGroupingsList){
                    if(group.equalsIgnoreCase(c.toString())){
                        contentAreaGroupings = c;
                    }
                }
            }
        }
        return contentAreaGroupings;
    }

    public static List<ContentAreaGroupings> contentAreaGroupingsList(){
        List<ContentAreaGroupings> c = null;
        c = Arrays.asList(ContentAreaGroupings.values());
        return c;
    }
}
