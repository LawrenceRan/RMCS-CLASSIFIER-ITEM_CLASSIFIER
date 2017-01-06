package contentclassification.domain;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 5/13/16.
 */
public class JWIImpl {
    private Logger logger = LoggerFactory.getLogger(JWIImpl.class);

    private String query;
    private POS pos;

    public JWIImpl(String query){
        this.query = query;
    }

    public POS getPos() {
        return pos;
    }

    public void setPos(POS pos) {
        this.pos = pos;
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
            logger.debug("Error in getting WordNet Stemmer. Message: "+ e.getMessage());
        } finally {
            dictionary.close();
        }
        return wordnetStemmer;
    }

    public List<Map> findStems(){
        List<Map> stems = new ArrayList<>();
        IDictionary dictionary = database();
        try {
            List<POS> posList = Arrays.asList(POS.values());
            if(!posList.isEmpty()){
                for(POS pos : posList){
                        WordnetStemmer stemmer = new WordnetStemmer(dictionary);
                        List<String> stemmers = stemmer.findStems(this.query, pos);
                        if (stemmers != null && !stemmers.isEmpty()) {
                            Map<Integer, Object> map = new HashMap<>();
                            map.put(pos.getNumber(), stemmers.get(0));
                            if (!map.isEmpty()) {
                                stems.add(map);
                            }
                        }
                }
            }
        } catch (Exception e){
            logger.debug("Error in finding stems. Message : "+ e.getMessage());
        } finally {
            dictionary.close();
        }
        return stems;
    }

    public String getStem(){
        String stem = null;
        IDictionary dictionary = database();
        try {
            List<POS> posList = Arrays.asList(POS.values());
            if (!posList.isEmpty()) {
                List<String> stems = new ArrayList<>();
                for (POS pos : posList) {
                    WordnetStemmer stemmer = new WordnetStemmer(dictionary);
                    List<String> stemmers = stemmer.findStems(this.query, pos);
                    if (stemmers != null && !stemmers.isEmpty()) {
                        stems.addAll(stemmers);
                    }
                }

                if(!stems.isEmpty()) {
                    stem = stems.get(0);
                }
            }
        } catch (Exception e){
            logger.debug("Error in finding stems. Message : "+ e.getMessage());
        } finally {
            dictionary.close();
        }
        return stem;
    }

    public List<Map> glosses(){
        List<Map> glosses = new ArrayList<>();
        List<Map> stemmers = findStems();
        if(!stemmers.isEmpty()){
            IDictionary dictionary = database();
            try {
                for (Map map : stemmers) {
                    Map<String, Object> resultsMap = new HashMap<>();

                    for (Object keySet : map.keySet()) {
                        Integer posInt = (Integer) keySet;
                        POS pos = POS.getPartOfSpeech(posInt);
                        String value = map.get(keySet).toString();
                        IIndexWord iIndexWord = dictionary.getIndexWord(value, pos);
                        if (iIndexWord != null) {
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
            } catch (Exception e){
                logger.debug("Error in getting glosses. Message : "+ e.getMessage());
            } finally {
                dictionary.close();
            }
        }
        return glosses;
    }

    public List<Map> search(){
        List<Map> resultsMap = null;
        IDictionary iDictionary = database();
        try{

        } catch (Exception e){
            logger.debug("Error in searching dictionary. Message : "+ e.getMessage());
        } finally {
            iDictionary.close();
        }
        return resultsMap;
    }

    public List<String> synonyms(){
        List<String> synonyms = null;
        IDictionary dictionary = database();
        try{
            POS pos = (this.pos != null) ? this.pos : POS.NOUN;

            IIndexWord iIndexWord = dictionary.getIndexWord(query, pos);
            if(iIndexWord != null){
                synonyms = new ArrayList<>();
                List<IWordID> wordIDs = iIndexWord.getWordIDs();
                if(wordIDs != null && !wordIDs.isEmpty()){
                    for(IWordID wordID : wordIDs){
                        IWord iWord = dictionary.getWord(wordID);
                        String word = iWord.getLemma();
                        List<ISynsetID> relatedSynsets = iWord.getSynset().getRelatedSynsets();
                        if(relatedSynsets != null && !relatedSynsets.isEmpty()){
                            for(ISynsetID synsetID : relatedSynsets){
                                List<IWord> iWords = dictionary.getSynset(synsetID).getWords();
                                if(iWords != null && !iWords.isEmpty()) {
                                    for(IWord iWord1 : iWords) {
                                        String synonym = iWord1.getLemma();
                                        synonym = (synonym.contains("_")) ? synonym.replace("_", " ") : synonym;
                                        synonyms.add(synonym);
                                    }
                                }
                            }
                        }
                    }

                    if(!synonyms.isEmpty()){
                        Set<String> cleanUp = new HashSet<>();
                        cleanUp.addAll(synonyms);

                        synonyms.clear();
                        synonyms.addAll(cleanUp);

                        Collections.sort(synonyms);
                    }
                }
            } else {
                logger.info("Word : "+ query + " not found in dictionary as using pos : "+ pos);
            }
        } catch (Exception e){
            logger.warn("Error in getting synonyms for term: "+ query + ". Message : "+ e.getMessage());
        }
        return synonyms;
    }
}
