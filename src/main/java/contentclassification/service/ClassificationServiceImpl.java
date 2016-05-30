package contentclassification.service;

import contentclassification.config.TermsScoringConfig;
import contentclassification.domain.*;
import contentclassification.utilities.BM25;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rsl_prod_005 on 5/24/16.
 */
@Service
public class ClassificationServiceImpl implements ClassificationService{

    @Autowired
    private TermsScoringConfig termsScoringConfig;

    Classification classification = null;

    @Override
    public List<String> uniqueCollection(String text) {
        classification = new Classification(text);
        return classification.uniqueCollection();
    }

    @Override
    public String[] tokenize(String text) {
        classification = new Classification(text);
        return classification.getTokens();
    }

    @Override
    public String getStem(String word) {
        JWIImpl jwi = new JWIImpl(word);
        return jwi.getStem();
    }

    @Override
    public List<String> getStems(String[] tokens) {
        List<String> stems = new ArrayList<>();
        if(tokens != null && tokens.length > 0){
            for(String s : tokens){
                JWIImpl jwi = new JWIImpl(s);
                String stem = jwi.getStem();
                if(StringUtils.isNotBlank(stem)){
                    stems.add(stem);
                }
            }
        }
        return stems;
    }

    @Override
    public List<Map> getPos(String[] tokens) {
        return classification.getPos(tokens);
    }

    @Override
    public List<String> getIntersection(List<String> a, List<String> b) {
        return classification.intersection(a,b);
    }

    @Override
    public List<Categories> getCategories(){
        Categories categories = new Categories();
        return categories.loadCategoriesFromYml();
    }

    @Override
    public double getTFScore(String[] document, String term){
        BM25 bm25 = new BM25(document, term);
        return bm25.tf();
    }

    @Override
    public double getIdfScore(String[] document, String term){
        BM25 bm25 = new BM25(document, term);
        return bm25.idf();
    }

    @Override
    public double getTfIdfWeightScore(String[] document, String term){
        BM25 bm25 = new BM25(document, term);
        return bm25.tfIdfWeightScore();
    }

    @Override
    public List<String> prepareTokens(List<String> tokens){
        List<String> output = new ArrayList<>();
        if(tokens != null && !tokens.isEmpty()){
            for(String t : tokens){
                if(StringUtils.isNotBlank(t)) {
                    output.add(t.toLowerCase().trim());
                }
            }
        }
        return output;
    }

    @Override
    public <T> List<Map> generateKeyValuePairs(List<T> objects){
        String regex = "\\w+\\=\\\"[a-zA-Z0-9]+\\\"\\s\\b\\w+\\=\\\".+\\\"";
        String keyValueRegEx = "\\w+\\=";

        List<Map> map = new ArrayList<>();
        if(objects != null && !objects.isEmpty()){
            List<String> f = new ArrayList<>();
            for(T o : objects){
                if(o instanceof String){
                    String s = (String) o;
                    if(StringUtils.isNotBlank(s)){
                        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(s);

                        int c = 0;
                        while(matcher.find()){
                            String grp = matcher.group(c);
                            if(StringUtils.isNotBlank(grp)){
                                f.add(grp.toLowerCase().trim());
                            }
                            c++;
                        }
                    }
                }
            }

            if(!f.isEmpty()){
                for(String f1 : f){
                    Map<Object, Object> m = new HashMap<>();
                    String[] a = f1.split("\\\"\\s\\b");
                    if(a.length > 0){
                        for(String a1 : a){
                            String a2 = a1.replaceAll("\"", "");
                            String[] a3 = a2.split("=");
                            if(a3.length > 0){
                                m.put(a3[0], a3[1]);
                            }
                        }
                    }
                    map.add(m);
                }
            }
        }
        return map;
    }

    @Override
    public String getContentMetaDataValue(NameAndContentMetaData n, List<Map> mList, WebMetaName webMetaName){
        String c = null;
        if(mList != null && !mList.isEmpty()){
            for(Map m : mList){
                if(m.containsKey(n.toString())){
                    Object v = m.get(n.toString());
                    if(v instanceof String){
                        if(((String) v).equalsIgnoreCase(webMetaName.toString())){
                            c = m.get(NameAndContentMetaData.CONTENT.toString()).toString();
                        }
                    }
                }
            }
        }
        return c;
    }

    @Override
    public List<String> getMultiWordedAttributes(Categories categories){
        List<String> attributes = new ArrayList<>();
        if(categories != null){
            List<String> cAttr = categories.getAttributes();
            if(cAttr != null && !cAttr.isEmpty()){
                for(String s : cAttr){
                    String[] a = s.split("\\s");
                    if(a.length > 1){
                        attributes.add(s);
                    }
                }
            }
        }
        return attributes;
    }

    @Override
    public String[] sentenceDetection(String text){
        String[] sentences = new String[]{};
        if(StringUtils.isNotBlank(text)){
            classification = new Classification(text);
             sentences = classification.getSentences();
        }
        return sentences;
    }

    @Override
    public boolean termFoundInSentences(String[] sentences, String term){
        boolean answer = false;
        if(sentences != null && sentences.length > 0 && StringUtils.isNotBlank(term)){
            for(String s : sentences){
                Pattern pattern = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(s);
                while(matcher.find()){
                    answer = true;
                }
            }
        }
        return answer;
    }

    @Override
    public Integer getTermToGroupScore(String term, String group){
        Integer score = 0;
        if(StringUtils.isNotBlank(term)){
            Pattern pattern = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(group);
            while(matcher.find()){
                score++;
            }
        }
        return score;
    }

    @Override
    public List<TermToGroupScore> getTermToGroupByContentAreaGroupings(List<TermToGroupScore> g,
                                                                       ContentAreaGroupings contentAreaGroupings){
        List<TermToGroupScore> t = new ArrayList<>();
        if (g != null && !g.isEmpty()) {
            for(TermToGroupScore t1 : g){
                if(t1.getGroup().equals(contentAreaGroupings)){
                    t.add(t1);
                }
            }
        }
        return t;
    }

    @Override
    public Double getTermScoringThreshold(){
        Double threshold = 0D;
        threshold = Double.parseDouble(termsScoringConfig.getThreshold());
        return threshold;
    }
}
