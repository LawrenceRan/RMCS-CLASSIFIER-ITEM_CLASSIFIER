package contentclassification.domain;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.data.BinarySearchWordnetFile;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Created by rsl_prod_005 on 5/13/16.
 */
public class JWIImpl {
    private Logger logger = LoggerFactory.getLogger(JWIImpl.class);

    private String query;

    public JWIImpl(String query){
        this.query = query;
    }

    private URL getDatabaseLocation(){
        URL url = null;
        try{
            String location = System.getProperty("wordnet.database.dir");
            url = new URL("file://"+location);
        } catch (Exception e){
            logger.debug("Error in getting location url. Message: "+ e.getMessage());
        }
        return url;
    }

    private IDictionary database(){
        IDictionary database = null;
        try{
            URL url = getDatabaseLocation();
            database = new Dictionary(url);
            database.open();
        } catch (Exception e){
            logger.debug("Error in getting a database. Message: "+ e.getMessage());
        }
        return database;
    }

    private POS getPartsOfSpeech(){
        POS pos = null;
        try{
            String query = this.query;
        } catch (Exception e){
            logger.debug("Error in getting pos. Message: "+ e.getMessage());
        }
        return pos;
    }

    private WordnetStemmer wordnetStemmer(){
        WordnetStemmer wordnetStemmer = null;
        IDictionary dictionary = database();
        try{
            wordnetStemmer = new WordnetStemmer(dictionary);
        } catch (Exception e){
            logger.debug("Error in getting wordnetStemmer. Message: "+ e.getMessage());
        } finally {

        }
        return wordnetStemmer;
    }

    public List<Map> findStems(){
        List<Map> stems = new ArrayList<>();
        List<POS> posList = Arrays.asList(POS.values());
        if(!posList.isEmpty()){
            for(POS pos : posList){
                WordnetStemmer stemmer = wordnetStemmer();
                if(stemmer != null) {
                    List<String> stemmers = stemmer.findStems(this.query, pos);
                    if(stemmers != null && !stemmers.isEmpty()) {
                        Map<Integer, Object> map = new HashMap<>();
                        map.put(pos.getNumber(), stemmers.get(0));
                        if (!map.isEmpty()) {
                            stems.add(map);
                        }
                    }
                }
            }
        }
        return stems;
    }

    public String getStem(){
        String stem = null;
        List<POS> posList = Arrays.asList(POS.values());
        if(!posList.isEmpty()){
            for(POS pos : posList){
                WordnetStemmer stemmer = wordnetStemmer();
                if(stemmer != null) {
                    List<String> stemmers = stemmer.findStems(this.query, pos);
                    if(stemmers != null && !stemmers.isEmpty()) {
                        stem = stemmers.get(0);
                    }
                }
            }
        }
        return stem;
    }

    public List<Map> glosses(){
        List<Map> glosses = new ArrayList<>();
        List<Map> stemmers = findStems();
        if(!stemmers.isEmpty()){
            IDictionary dictionary = database();
            for(Map map : stemmers){
                Map<String, Object> resultsMap = new HashMap<>();

                for(Object keySet : map.keySet()){
                    Integer posInt = (Integer) keySet;
                    POS pos = POS.getPartOfSpeech(posInt);
                    String value = map.get(keySet).toString();
                    IIndexWord iIndexWord = dictionary.getIndexWord(value, pos);
                    if(iIndexWord != null) {
                        IWordID iWordID = iIndexWord.getWordIDs().get(0);
                        IWord iWord = dictionary.getWord(iWordID);

                        //resultsMap.put("wordId", iWordID);
                        resultsMap.put("lemma", iWord.getLemma());
                        resultsMap.put("gloss", iWord.getSynset().getGloss());
                        resultsMap.put("type", POS.getPartOfSpeech(iWord.getSynset().getType()).toString());
                        glosses.add(resultsMap);
                    }
                }
            }
        }
        return glosses;
    }

    public List<Map> search(){
        List<Map> resultsMap = null;
        IDictionary iDictionary = database();

        return resultsMap;
    }
}
