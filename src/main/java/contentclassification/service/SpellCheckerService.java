package contentclassification.service;

import com.swabunga.spell.event.SpellCheckListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by rsl_prod_005 on 10/7/16.
 */
public interface SpellCheckerService extends SpellCheckListener {
    public List<String> getSuggestions(String misspelledWord);
    public String getCorrectedLine(String query);
    public List<String> getMisspelledWords(String query);
    public boolean isCorrect(String query);
    public double getSimilarityScore(String queryText, String suggestedText);
    public List<String> applyRegExToSuggestions(String query, List<String> suggestions);
    public List<String> updateSuggestions(String query, List<String> suggestions);
}
