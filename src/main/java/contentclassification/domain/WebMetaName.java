package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/26/16.
 */
public enum WebMetaName {
    KEYWORDS("keywords"),DESCRIPTION("description"),SUBJECT("subject"),COPYRIGHT("copyright"),
    LANGUAGE("language"),ROBOTS("robots"), REVISED("revised"),ABSTRACT("abstract"),TOPIC("topic"),
    SUMMARY("summary"),CLASSIFICATION("classification"),AUTHOR("author"),
    DESIGNER("designer"),REPLY_TO("reply-to"),OWNER("owner"),URL("url"),IDENTIFIER_URL("identifier-url"),
    DIRECTORY("directory"),PAGENAME("pagename"),CATEGORY("category"),COVERAGE("coverage"),DISTRIBUTION("distribution"),
    RATING("rating"),REVISIT("revisit"),AFTER("after"),
    SUBTITLE("subtitle"),TARGET("target"),HANDHELDFRIENDLY("handheldfriendly"),
    MOBILEOPTIMIZED("mobileoptimized"),DATE("date"),SEARCH_DATE("search_date"),DC_TITLE("dc.title"),
    RESOURCELOADERDYNAMICSTYLES("resourceloaderdynamicstyles"),MEDIUM("medium"),
    SYNDICATION_SOURCE("syndication-source"),ORIGINAL_SOURCE("original-source"),VERIFY_V1("verify-v1"),Y_KEY("y_key"),PAGEKEY("pagekey");

    private final String name;

    WebMetaName(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public static WebMetaName fromString(String name){
        WebMetaName w = null;
        if(StringUtils.isNotBlank(name)){
            List<WebMetaName> webMetaNameList = Arrays.asList(WebMetaName.values());
            if(!webMetaNameList.isEmpty()){
                for(WebMetaName n : webMetaNameList){
                    if(name.equalsIgnoreCase(n.toString())){
                        w = n;
                    }
                }
            }
        }
        return w;
    }
}
