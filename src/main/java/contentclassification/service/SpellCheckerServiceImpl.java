package contentclassification.service;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.DefaultWordFinder;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import contentclassification.domain.Classification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rsl_prod_005 on 10/7/16.
 */
@Service
public class SpellCheckerServiceImpl implements SpellCheckerService {
    private static final Logger logger = LoggerFactory.getLogger(SpellCheckerServiceImpl.class);
    private static final String DICTIONARY = "words.utf-8.txt";
    private SpellChecker spellChecker;
    private List<String> misspelledWords;
    private static SpellDictionaryHashMap spellDictionaryHashMap;

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(DICTIONARY);
        if(inputStream != null) {
            try {
                Reader reader = new InputStreamReader(inputStream);
                spellDictionaryHashMap = new SpellDictionaryHashMap(reader);
            } catch (Exception e){
                logger.debug("Error in getting dictionary file. Message : "+ e.getMessage());
            } finally {
                try{
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
    }

    SpellCheckerServiceImpl(){
        this.misspelledWords = new ArrayList<>();
        initialize();
    }

    public void initialize(){
        spellChecker = new SpellChecker(spellDictionaryHashMap);
        spellChecker.addSpellCheckListener(this);
    }

    @Override
    public void spellingError(SpellCheckEvent spellCheckEvent) {
        spellCheckEvent.ignoreWord(true);
        misspelledWords.add(spellCheckEvent.getInvalidWord());
    }

    @Override
    public List<String> getSuggestions(String misspelledWord){
        List<String> suggestions = null;
        if(StringUtils.isNotBlank(misspelledWord)){
            @SuppressWarnings("unchecked")
            List<Word> dictSuggestions = spellChecker.getSuggestions(misspelledWord, 0);
            if(dictSuggestions != null && !dictSuggestions.isEmpty()){
                suggestions = new ArrayList<>();
                for(Word word : dictSuggestions){
                    suggestions.add(word.getWord());
                }
            }
        }
        return suggestions;
    }

    @Override
    public String getCorrectedLine(String line) {
        StringBuilder stringBuilder = new StringBuilder();
        if(StringUtils.isNotBlank(line)){
            String[] tempWords = line.split(" ");
            for(String word : tempWords){
                if(!spellChecker.isCorrect(word)){
                    List<String> suggestions = getSuggestions(word);
                    if(suggestions != null && !suggestions.isEmpty()) {
                        String correctedWord = suggestions.get(0);
                        stringBuilder.append(correctedWord);
                    }
                } else {
                    stringBuilder.append(word);
                }
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString().trim();
    }

    @Override
    public List<String> getMisspelledWords(String line) {
        if(StringUtils.isNotBlank(line)){
            StringWordTokenizer stringWordTokenizer = new StringWordTokenizer(line, new DefaultWordFinder());
            spellChecker.checkSpelling(stringWordTokenizer);
            return misspelledWords;
        }
        return null;
    }

    @Override
    public boolean isCorrect(String query) {
        if(StringUtils.isNotBlank(query)){
            return spellChecker.isCorrect(query);
        }
        return true;
    }

    @Override
    public double getSimilarityScore(String queryText, String suggestedText) {
        return Classification.similarityMeasurement(queryText, suggestedText);
    }

    @Override
    public List<String> applyRegExToSuggestions(String query, List<String> suggestions) {
        List<String> updateDatedSuggestions = null;
        if (suggestions != null && !suggestions.isEmpty()) {
            updateDatedSuggestions = new ArrayList<>();
            for(String suggestion : suggestions){
                Pattern pattern = Pattern.compile("\\b"+ query, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(suggestion);
                while(matcher.find()){
                    updateDatedSuggestions.add(suggestion);
                }
            }
        }
        return updateDatedSuggestions;
    }

    @Override
    public List<String> updateSuggestions(String query, List<String> suggestions){
        List<String> updatedSuggestions = new LinkedList<>();
        List<Map> sortSuggestions = new ArrayList<>();
        if(suggestions != null && !suggestions.isEmpty()) {
            for (String suggestion : suggestions) {
                double score = getSimilarityScore(query.toLowerCase().trim(),
                        suggestion.toLowerCase().trim());

                if (score > 0) {
                    Map<String, Object> doubleMap = new HashMap<>();
                    doubleMap.put("score", score);
                    doubleMap.put("suggestion", suggestion);

                    sortSuggestions.add(doubleMap);
                }
            }

            Collections.sort(sortSuggestions, new Comparator<Map>() {
                @Override
                public int compare(Map o1, Map o2) {
                    Double score1 = (Double) o1.get("score");
                    Double score2 = (Double) o2.get("score");
                    int value = 0;

                    if (score1 > score2) {
                        value = -1;
                    }

                    if (score1 < score2) {
                        value = 1;
                    }


                    return value;
                }
            });

            for (Map map : sortSuggestions) {
                updatedSuggestions.add(map.get("suggestion").toString());
            }

            if (!updatedSuggestions.isEmpty()) {
                List<String> updatedByRegEx = applyRegExToSuggestions(query, updatedSuggestions);
                if (updatedByRegEx != null && !updatedByRegEx.isEmpty()) {
                    updatedSuggestions.clear();
                    updatedSuggestions.addAll(updatedByRegEx);
                }
            }
        }

        return updatedSuggestions;
    }
}
