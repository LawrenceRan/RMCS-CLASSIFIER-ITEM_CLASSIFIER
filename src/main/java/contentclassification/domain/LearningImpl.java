package contentclassification.domain;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class LearningImpl {
    private static final Logger logger = LoggerFactory.getLogger(LearningImpl.class);
    private String query;

    public static LearningImpl setQuery(String query){
        return new LearningImpl(query);
    }

    private LearningImpl(String query){
        this.query = query;
    }

    private List<String> words(){
        List<String> words = new ArrayList<>();
        Set<String> cleanUpContainer = new HashSet<>();
        List<String> incomingWords = Arrays.asList(this.query.trim().toLowerCase()
                .replace(" - ", " ")
                .replace(" | ", " ")
                .split(" "));
        cleanUpContainer.addAll(incomingWords);
        words.addAll(cleanUpContainer);
        return words;
    }

    public Map<String, Object> updateKnowledgeBase(){
        Map<String, Object> update = new HashMap<>();
        List<String> words = words();
        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream("en-pos-maxent.bin");
            //inputStream = new FileInputStream("en-token.bin");
            POSModel posModel = new POSModel(inputStream);
            POSTaggerME taggerME = new POSTaggerME(posModel);

            //String[] query = this.query.split(" - ");
            String[] query = null;

            if(words != null && !words.isEmpty()){
                query = new String[words.size()];
                int x = 0;
                for(String s : words){
                    query[x] = s;
                    x++;
                }
            }


            //Using tokenizers instead
            //TokenizerModel tokenizerModel = new TokenizerModel(inputStream);
            //Tokenizer tokenizer = new TokenizerME(tokenizerModel);

            ///String[] tagsTokenized = tokenizer.tokenize(this.query);
            String[] tags = taggerME.tag(query);

            Map<String, Object> tagResponses = new HashMap<>();
            if(tags != null && tags.length > 0){
                int x = 0;
                for(String tag : tags){
                    if(StringUtils.isNotBlank(tag)) {
                        if(!tag.equalsIgnoreCase(":") && !tag.equalsIgnoreCase(".")) {
                            POSRESPONSES p = POSRESPONSES.valueOf(tag);
                            logger.info("Query: "+  query[x] +" Tag: " + tag + " f: "+ p);
                            tagResponses.put(query[x], p.toString());
                        }
                    }
                    x++;
                }
            }
            update.put("Parts-Of-Speech", tagResponses);

            //String externalResource = find("r");
        } catch (IOException io){
            logger.debug("Error: "+ io.getMessage());
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e){
                    logger.debug("Error: "+ e.getMessage());
                }
            }
        }
        return update;
    }

    public String find(){
        String finding = null;
        if(StringUtils.isNotBlank(query)){
            Model model = getModel();
        }
        return finding;
    }

    /**
     * This method would be used to generate sentences out of a given tags.
     * @param tags
     * @return
     */
    public static String generateSentence(String[] tags){
        String sentence = null;
        if(tags != null && tags.length > 0){
            InputStream inputStream = null;
            try{
                StringBuilder stringBuilder = new StringBuilder();
                int x = 0;
                for(String t : tags){
                    if(x < (tags.length -1)) {
                        stringBuilder.append(t + " ");
                    } else {
                        stringBuilder.append(t);
                    }
                    x++;
                }

                String s = stringBuilder.toString();
            } catch(Exception e){
                logger.debug("Error: "+ e.getMessage());
            } finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception ex) {
                    logger.warn("Error in closing file. Message : "+ ex.getMessage());
                }
            }
        }
        return sentence;
    }

    /**
     * This method is used to get parts-of-speech of a given list of strings.
     * @param collection
     * @return
     */
    public static Map<String, Object> getPartsOfSpeech(List<String> collection){
        Map<String, Object> response = new HashMap<>();
        if(collection != null && !collection.isEmpty()){
            InputStream inputStream = null;
            try {
                ClassLoader classLoader = LearningImpl.class.getClassLoader();
                inputStream = classLoader.getResourceAsStream("en-pos-maxent.bin");
                POSModel posModel = new POSModel(inputStream);
                POSTaggerME taggerME = new POSTaggerME(posModel);

                String[] query = null;

                if(collection != null && !collection.isEmpty()){
                    query = new String[collection.size()];
                    int x = 0;
                    for(String s : collection){
                        query[x] = s;
                        x++;
                    }
                }

                String[] tags = taggerME.tag(query);

                Map<String, Object> tagResponses = new HashMap<>();
                if(tags != null && tags.length > 0){
                    int x = 0;
                    for(String tag : tags){
                        if(StringUtils.isNotBlank(tag)) {
                            if(!tag.equalsIgnoreCase(":") && !tag.equalsIgnoreCase(".")) {
                                POSRESPONSES p = POSRESPONSES.valueOf(tag);
                                tagResponses.put(query[x], p.toString());
                            }
                        }
                        x++;
                    }
                }
                response.put("partsOfSpeech", tagResponses);


            } catch (Exception e){
                logger.debug("Error: Message: "+ e.getMessage());
            } finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e){
                        logger.debug("Error: "+ e.getMessage());
                    }
                }
            }
        }
        return response;
    }

    private Model getModel(){
        Model model = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("labels_en.ttl");
        if(inputStream != null) {
            try {
                model = ModelFactory.createDefaultModel();
                model.read(inputStream, null, "TTL");
            } catch (Exception e){
                logger.warn("Error in getting model for ttl file. Message : "+ e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.warn("Error in closing input stream. Message : " + e.getMessage());
                }
            }
        }

        return model;
    }
}
