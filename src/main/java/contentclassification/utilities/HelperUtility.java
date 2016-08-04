package contentclassification.utilities;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
}
