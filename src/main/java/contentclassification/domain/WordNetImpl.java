package contentclassification.domain;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/11/16.
 */
public class WordNetImpl {

    private String query;

    public WordNetImpl(String query){
        this.query = query.toLowerCase().trim();
    }

    private WordNetDatabase database(){
        return WordNetDatabase.getFileInstance();
    }

    private Synset[] getSynsets(){
        Synset[] synsets  = null;
        WordNetDatabase databaseObj = database();
        if(databaseObj != null){
            synsets = databaseObj.getSynsets(this.query);
        }
        return synsets;
    }

    public List<Map> getResults(){
        List<Map> output = new ArrayList<>();
        Synset[] synsets = getSynsets();
        if(synsets != null && synsets.length > 0){
            for(int x = 0; x < synsets.length; x++){
                Map<String, Object> map = new HashMap<>();
                map.put("definition", synsets[x].getDefinition());
                map.put("usageExamples", synsets[x].getUsageExamples());
                SynsetType synsetType = synsets[x].getType();

                map.put("type", synsetType.toString());

                String[] wordForms = synsets[x].getWordForms();
                map.put("wordForms", wordForms);
                output.add(map);
            }
        }
        return output;
    }
}
