package contentclassification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 7/13/16.
 */
public class RegExManager {
    private static final Logger logger = LoggerFactory.getLogger(RegExManager.class);

    public static String[] loadBrandTextRegEx(){
        String[] regExs = null;
        ClassLoader classLoader = RegExManager.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("regex-brands");
        if(inputStream != null){
            try{
                Yaml yaml = new Yaml();
                Map regEx = (Map) yaml.load(inputStream);
                if(regEx != null && !regEx.isEmpty()){
                    if(regEx.containsKey("text")) {
                        if(regEx.containsKey("text")) {
                            Object l = regEx.get("text");
                            if (l instanceof List) {
                                List<String> ls = (List<String>) l;
                                regExs = ls.toArray(new String[regEx.size()]);
                            }
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting regex brands. Message: "+ e.getMessage());
            }
        }
        return regExs;
    }

    public static String[] loadBrandHtmlRegEx(){
        String[] regExs = null;
        ClassLoader classLoader = RegExManager.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("regex-brands");
        if(inputStream != null){
            try{
                Yaml yaml = new Yaml();
                Map regEx = (Map) yaml.load(inputStream);
                if(regEx != null && !regEx.isEmpty()){
                    if(regEx.containsKey("html")) {
                        if(regEx.containsKey("html")) {
                            Object l = regEx.get("html");
                            if (l instanceof List) {
                                List<String> ls = (List<String>) l;
                                regExs = ls.toArray(new String[regEx.size()]);
                            }
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting regex brands. Message: "+ e.getMessage());
            }
        }
        return regExs;
    }

    public static String[] loadBrandHtmlAttrRegEx(){
        String[] regExs = null;
        ClassLoader classLoader = RegExManager.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("regex-brands");
        if(inputStream != null){
            try{
                Yaml yaml = new Yaml();
                Map regEx = (Map) yaml.load(inputStream);
                if(regEx != null && !regEx.isEmpty()){
                    if(regEx.containsKey("htmlAttributes")) {
                        if(regEx.containsKey("htmlAttributes")) {
                            Object l = regEx.get("htmlAttributes");
                            if (l instanceof List) {
                                List<String> ls = (List<String>) l;
                                regExs = ls.toArray(new String[regEx.size()]);
                            }
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting regex brands. Message: "+ e.getMessage());
            }
        }
        return regExs;
    }

    public static String[] loadRegEx(String resourceFileName, String key){
        String[] regExs = null;
        ClassLoader classLoader = RegExManager.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceFileName);
        if(inputStream != null){
            try{
                Yaml yaml = new Yaml();
                Map regEx = (Map) yaml.load(inputStream);
                if(regEx != null && !regEx.isEmpty()){
                    if(regEx.containsKey(key)) {
                        if(regEx.containsKey(key)) {
                            Object l = regEx.get(key);
                            if (l instanceof List) {
                                List<String> ls = (List<String>) l;
                                regExs = ls.toArray(new String[regEx.size()]);
                            }
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting regex brands. Message: "+ e.getMessage());
            }
        }
        return regExs;
    }
}
