package contentclassification.utilities;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 7/22/16.
 */
public class HelperUtility {
    private static Logger logger = LoggerFactory.getLogger(HelperUtility.class);
    public static <T> List<T> iterableToList(Iterable<T> iterable){
        List<T> list = new ArrayList<>();
        if(iterable.iterator().hasNext()) {
            for (T it : iterable) {
                list.add(it);
            }
        }
        return list;
    }

    public static String[] listToArray(List<String> list){
        String[] output = null;
        if(list != null && !list.isEmpty()){
            output = new String[list.size()];
            int x = 0;
            for(String s : list){
                output[x] = s;
                x++;
            }
        }
        return output;
    }

    public static String getTopLevelGender(String term){
        String gender = null;
        if(StringUtils.isNotBlank(term)){
            ClassLoader classLoader = HelperUtility.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("en-gender-groupings");
            if(inputStream != null){
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) yaml.load(inputStream);
                if(map != null && !map.isEmpty()){
                    for(Map.Entry<String, Object> entry : map.entrySet()){
                        String key = entry.getKey();
                        Object valueObj = entry.getValue();
                        if(valueObj instanceof List){
                            @SuppressWarnings("unchecked")
                            List<String> values = (List<String>) valueObj;
                            if(!values.isEmpty()){
                                if(values.contains(term.toLowerCase().trim())){
                                    gender = key;
                                }
                            }
                        }
                    }
                }

                try{
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
        return gender;
    }

    public static String textToHtml(String text){
        String html = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>");
        if(StringUtils.isNotBlank(text)){
            int len = text.length();
            for(int x = 0; x < len; x++){
                char c = text.charAt(x);
                switch (c){
                    case '\r':
                        stringBuilder.append("</br>");
                        break;
                    case '\n':
                        stringBuilder.append("</br>");
                        break;
                    default:
                        stringBuilder.append(c);
                        break;
                }
            }
        }
        stringBuilder.append("</html>");
        html = stringBuilder.toString();
        return html;
    }

    public static <T> Map<T, T> uncheckedMapHandler(Map<T, T> map){
        @SuppressWarnings("unchecked")
        Map<T, T> uncheckedMap = map;
        return uncheckedMap;
    }

    public static <T> List<T> getValueOfMapAsList(Object object, String valueKey){
        List<T> values = null;
        if(object != null) {
            if(object instanceof Map) {
                Map map = (Map) object;
                if (!map.isEmpty()) {
                    values = new ArrayList<>();
                    @SuppressWarnings("unchecked")
                    Set<Map.Entry> entrySet = map.entrySet();

                    for(Map.Entry entry : entrySet){
                        values.add((T) entry.getValue());
                    }
                }
            }

            if(object instanceof List){
                List list = (List) object;
                if(!list.isEmpty()){
                    values = new ArrayList<>();
                    for(Object listObj : list){
                        if(listObj instanceof Map){
                            Map map = (Map) listObj;
                            if (!map.isEmpty()) {
                                @SuppressWarnings("unchecked")
                                Set<Map.Entry> entrySet = map.entrySet();

                                Object value = null;
                                for(Map.Entry entry : entrySet){
                                    if(entry.getKey().toString().equalsIgnoreCase(valueKey)) {
                                        value = entry.getValue();
                                    }
                                }

                                if(value != null){
                                    values.add((T) value);
                                }
                            }
                        }
                    }
                }
            }
        }
        return values;
    }
}
