package contentclassification.domain;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/28/16.
 */
public class ValueComparator implements Comparator<String> {
    private Map<String, Double> map;

    public ValueComparator(Map<String, Double> map){
        this.map = map;
    }

    @Override
    public int compare(String o1, String o2) {
        if(map.get(o1) >= map.get(o2)){
            return -1;
        } else {
            return 1;
        }
    }
}
