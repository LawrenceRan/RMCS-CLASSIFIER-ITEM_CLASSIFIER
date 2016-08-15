package contentclassification.utilities;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 7/22/16.
 */
public class HelperUtility {
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
                Map<String, Object> map = (Map<String, Object>) yaml.load(inputStream);
                if(map != null && !map.isEmpty()){
                    for(Map.Entry<String, Object> entry : map.entrySet()){
                        String key = entry.getKey();
                        Object valueObj = entry.getValue();
                        if(valueObj instanceof List){
                            List<String> values = (List<String>) valueObj;
                            if(!values.isEmpty()){
                                if(values.contains(term.toLowerCase().trim())){
                                    gender = key;
                                }
                            }
                        }
                    }
                }
            }
        }
        return gender;
    }
}
