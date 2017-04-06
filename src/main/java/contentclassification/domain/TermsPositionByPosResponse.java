package contentclassification.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 4/3/17.
 */
public class TermsPositionByPosResponse {
    private List<String> suggestions = new ArrayList<>();

    public void addSuggestions(String suggestions){
        this.suggestions.add(suggestions);
    }

    public List<String> getSuggestions() {
        List<String> stringList = this.suggestions;
        Set<String> cleaner = new HashSet<>();
        cleaner.addAll(stringList);
        stringList.clear();
        stringList.addAll(cleaner);
        return stringList;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
