package contentclassification.utilities;

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
}
