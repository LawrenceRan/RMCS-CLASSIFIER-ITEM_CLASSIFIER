package contentclassification.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsl_prod_005 on 4/3/17.
 */
public class TermsPositionByPosResponse {
    private List<String> suggestions = new ArrayList<>();

    public void addSuggestions(String suggestions){
        this.suggestions.add(suggestions);
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
