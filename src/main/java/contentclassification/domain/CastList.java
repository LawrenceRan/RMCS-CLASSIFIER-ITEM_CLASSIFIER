package contentclassification.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/30/16.
 */
public class CastList {
    public static <T> List<T> castList(Class<? extends T> object, Collection<T> collection){
        List<T> r = new ArrayList<T>(collection.size());
        if(!collection.isEmpty()) {
            for (Object obj : collection) {
                r.add(object.cast(obj));
            }
        }
        return r;
    }
}
