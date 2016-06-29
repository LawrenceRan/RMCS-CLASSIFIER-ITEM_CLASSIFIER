package contentclassification.service;

import com.google.common.collect.Sets;
import contentclassification.config.TermsScoringConfig;
import contentclassification.domain.*;
import contentclassification.model.RulesEngineModel;
import contentclassification.utilities.BM25;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.*;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.weka.WekaClusterer;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.clusterers.XMeans;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rsl_prod_005 on 5/24/16.
 */
@Service
public class ClassificationServiceImpl implements ClassificationService{

    private static final Logger logger = LoggerFactory.getLogger(ClassificationServiceImpl.class);
    @Autowired
    private TermsScoringConfig termsScoringConfig;

    @Autowired
    private JsoupService jsoupService;

    @Autowired
    private RulesEngineModelServiceImpl rulesEngineModelService;

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
        String regex = "\\s.*?\\=\\\"[a-zA-Z0-9]+\\\"\\s\\b\\w+\\=\\\".+\\\"";
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
                                if(a3.length == 2) {
                                    try {
                                        m.put(a3[0], a3[1]);
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
                                }
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
                            if(m.get(NameAndContentMetaData.CONTENT.toString()) != null) {
                                c = m.get(NameAndContentMetaData.CONTENT.toString()).toString();
                            }
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
                Pattern pattern = Pattern.compile("\\b"+term+"\\b", Pattern.CASE_INSENSITIVE);
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
            Pattern pattern = Pattern.compile("\\b"+term+"\\b", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
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

    @Override
    public String getSentencesAsString(String... sentences){
        String sentence = null;
        if(sentences != null && sentences.length > 0){
            StringBuilder stringBuilder = new StringBuilder();
            int x = 0;
            for(String s : sentences){
                if(x < (sentences.length -1)) {
                    stringBuilder.append(s+"\n");
                } else {
                    stringBuilder.append(s);
                }
                x++;
            }
            sentence = stringBuilder.toString();
        }
        return sentence;
    }

    @Override
    public String removePossibleUrlFromText(List<Map> links, String text){
        if(StringUtils.isNotBlank(text)){
            Pattern pattern = Pattern.compile("\\<a[^\\>]*\\>([^\\<]+)\\<\\/a\\>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            int grpCount = matcher.groupCount();
            List<String> textToBeRemoved = new ArrayList<>();
            while(matcher.find()){
                textToBeRemoved.add(matcher.group());
            }

            if(!textToBeRemoved.isEmpty()){
                for(String s : textToBeRemoved){
                    text = text.replace(s, "");
                }
            }
        }
        return text;
    }

    @Override
    public List<String> removeSentencesFromList(List<String> list){
        List<String> updated = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for(String s : list){
                String[] h = sentenceDetection(s);
                classification = new Classification(s);
                String[] sentences = classification.getSentences();
                System.out.println("Sentences: "+ sentences);
            }
        }
        return updated;
    }

    @Override
    public List<FabricName> getFabricNames(){
        return FabricName.getFabrics();
    }

    @Override
    public List<FabricName> getFabricsFromContent(String text){
        List<FabricName> fabricNames = new ArrayList<>();
        if(StringUtils.isNotBlank(text)){
            List<FabricName> isPresent = Classification.isFabricPresent(text);
            Set<FabricName> uniqueFabricNames = new HashSet<>();
            if(!isPresent.isEmpty()){
                uniqueFabricNames.addAll(isPresent);
            }
            fabricNames.addAll(uniqueFabricNames);
        }
        return fabricNames;
    }

    @Override
    public List<String> colorsFromSelectFields(String text) {
        List<String> colors = new LinkedList<>();
        if(StringUtils.isNotBlank(text)){
            String[] regExs = {"\\<\\bselect\\b.*\\=\\\".*?(color|colour).*(.*?)\\>",
                    "\\<\\binput\\b.*\\=\\\".*?(color|colour).*(.*?)\\/\\>"};

            List<String> inputFields = new ArrayList<>();

            if(regExs != null && regExs.length > 0) {
                for(String regEx : regExs) {
                    Pattern pattern = Pattern.compile(regEx,
                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(text);

                    while (matcher.find()) {
                        inputFields.add(matcher.group());
                    }
                }
            }

            //Parsing document as text for further dom queries or manipulations.
            Document document = null;
            try {
                document = JsoupImpl.parseHtml(text);
            } catch (Exception e){
                logger.debug("Error: "+ e.getMessage());
            }

            if(!inputFields.isEmpty()){
                List<Map> keyValuePair = generateKeyValuePairs(inputFields);

                //Get tag name of select element.
                String tagName = null;
                if(!keyValuePair.isEmpty()){
                    for(Map<String, String> m : keyValuePair) {
                        if (m.containsKey("name")) {
                            tagName = m.get("name").toString();
                        }
                    }
                }

                if(StringUtils.isNotBlank(tagName)){
                    try {
                        if(document != null){
                            Elements elements = document.getElementsByAttributeValue("name", tagName);
                            if(!elements.isEmpty()) {
                                Elements selectElements = elements.get(0).children();
                                Iterator<Element> elementIterator = selectElements.iterator();
                                while (elementIterator.hasNext()) {
                                    Element element = elementIterator.next();
                                    if(element.hasAttr("selected")) {
                                        colors.add(element.text());
                                    } else {
                                        //colors.add(element.text());
                                    }
                                }
                            }
                        }
                    } catch (Exception e){
                        logger.debug("Error: "+ e.getMessage());
                    }
                }
            }


            try {
                String colorTagAttributeName = null;
                if (document != null) {
                    List<String> colorIds = new ArrayList<>();
                    Elements elements = document.getElementsByTag("input");
                    if (!elements.isEmpty()) {
                        Iterator<Element> elementIterator = elements.iterator();
                        while (elementIterator.hasNext()) {
                            Element element = elementIterator.next();
                            String attributeName = element.attr("name");
                            if (AppUtils.regExContains("(color|colour)", attributeName)) {
                                colorTagAttributeName = element.attr("name");
                                String colorId = element.id();
                                String val = element.val();
                                colorIds.add(colorId);
                            }
                        }
                    }

                    if (!colorIds.isEmpty()) {
                        for (String c : colorIds) {
                            Elements colorElements = document.getElementsByAttributeValue("for", c);
                            if (!colorElements.isEmpty()) {
                                Iterator<Element> elementIterator = colorElements.iterator();
                                while (elementIterator.hasNext()) {
                                    Element element = elementIterator.next();
                                    String colorText = element.text();
                                    if (StringUtils.isBlank(colorText)) {
                                        String dataId = element.attr("data-id");
                                        if (StringUtils.isNotBlank(dataId)) {
                                            List<String> tokenAttributeName = new ArrayList<>();
                                            if (StringUtils.isNotBlank(colorTagAttributeName)) {
                                                tokenAttributeName
                                                        .addAll(Arrays.asList(colorTagAttributeName.split("-")));
                                            }
                                            List<String> tokenDataId = Arrays.asList(dataId.split("-"));
                                            List<String> intersection = getIntersection(tokenAttributeName, tokenDataId);
                                            if (!intersection.isEmpty()) {
                                                //remove intersected words from data id attribute's value.
                                                for (String i : intersection) {
                                                    dataId = dataId.replace(i, "");
                                                }
                                                colors.add(dataId.replace("-", ""));
                                            } else {
                                                colors.add(dataId);
                                            }
                                        }
                                    } else {
                                        colors.add(colorText);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Error: " + e.getMessage());
            }
        }
        return colors;
    }

    @Override
    public List<String> sizeFromSelectFields(String text) {
        List<String> sizes = new LinkedList<>();
        if(StringUtils.isNotBlank(text)){
            String regEx = "\\<\\bselect\\b.*\\=\\\".*?(size|dimension).*(.*?)\\>";
            Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(text);

            List<String> sizeSelect = new ArrayList<>();
            while (matcher.find()){
                sizeSelect.add(matcher.group());
            }

            Document document = null;
            try {
                document = JsoupImpl.parseHtml(text);
            } catch (Exception e){
                logger.debug("Error in getting dom document. Message: "+ e.getMessage());
            }

            //get size from text and compare it with

            if(!sizeSelect.isEmpty()){
                List<Map> keyAndValue = generateKeyValuePairs(sizeSelect);

                //Get tag name of select element.
                String tagName = null;
                if(!keyAndValue.isEmpty()){
                    for(Map<String, String> m : keyAndValue) {
                        if (m.containsKey("name")) {
                            tagName = m.get("name").toString();
                        }
                    }
                }

                if(StringUtils.isNotBlank(tagName)){
                    try {
                        if(document != null){
                            Elements elements = document.getElementsByAttributeValue("name", tagName);
                            if(!elements.isEmpty()) {
                                Elements selectElements = elements.get(0).children();
                                Iterator<Element> elementIterator = selectElements.iterator();
                                while (elementIterator.hasNext()) {
                                    Element element = elementIterator.next();
                                    if(element.hasAttr("selected")) {
                                        sizes.add(element.text());
                                    } else {
                                        sizes.add(element.text());
                                    }

                                    //If element has child elements.
                                    Elements children = element.children();
                                    if(!children.isEmpty()){
                                        Iterator<Element> childrenItr = children.iterator();
                                        while(childrenItr.hasNext()){
                                            Element child = childrenItr.next();
                                            sizes.add(child.text());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e){
                        logger.debug("Error: "+ e.getMessage());
                    }
                }
            }

            if(sizes.isEmpty()){
                if(document != null){
                    Elements elements = document.getElementsByTag("select");
                    if(!elements.isEmpty()){
                        Iterator<Element> elementIterator = elements.iterator();
                        while (elementIterator.hasNext()){
                            Element element = elementIterator.next();
                            String attributeName = element.attr("name");
                            if(AppUtils.regExContains("(size)", attributeName)) {
                                Elements children = element.children();
                                if(!children.isEmpty()) {
                                    Iterator<Element> childIterator = children.iterator();
                                    while(childIterator.hasNext()) {
                                        Element child = childIterator.next();
                                        String sizeText = child.text();
                                        if(StringUtils.isNotBlank(sizeText)) {
                                            sizes.add(sizeText);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if(sizes.isEmpty()){
                List<Size> sizeList = Size.loadSizeFromYaml();

                List<String> unverified = new ArrayList<>();

                String[] html5Data = {"[^data-]"};
                if(html5Data != null && html5Data.length > 0 && document != null){
                    for(String t : html5Data){
                        if(StringUtils.isNotBlank(t)) {
                            Elements elements = document.select(t);
                            if (!elements.isEmpty()) {
                                Iterator<Element> elementsIterator = elements.iterator();
                                while (elementsIterator.hasNext()) {
                                    Element element = elementsIterator.next();
                                    String outerHtml = element.outerHtml();

                                    boolean isPresent = AppUtils.regExContains("(size)", outerHtml);
                                    if (isPresent) {
                                        Elements children = element.children();
                                        if (!children.isEmpty()) {
                                            Iterator<Element> elementIterator = children.iterator();
                                            while (elementIterator.hasNext()) {
                                                Element child = elementIterator.next();
                                                if (StringUtils.isNotBlank(child.text())) {
                                                    unverified.add(child.text());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                String[] tags = {"div","span","ul","li"};
                if(tags != null && tags.length > 0 && document != null){
                    for(String t : tags){
                        if(StringUtils.isNotBlank(t)){
                            Elements elements = document.select(t);
                            if(!elements.isEmpty()) {
                                Iterator<Element> elementIterator = elements.iterator();
                                while (elementIterator.hasNext()) {
                                    Element element = elementIterator.next();
                                    boolean isPresent = AppUtils
                                            .regExContains("\\<"+ element.tagName() +"\\s\\b.*?(size)+.(\\>|\\/\\>)", element.outerHtml());
                                    if (isPresent) {
                                        Elements children = element.children();
                                        if(!children.isEmpty()) {
                                            Iterator<Element> childIterator = children.iterator();
                                            while (childIterator.hasNext()) {
                                                Element childElement = childIterator.next();
                                                logger.info("Elements: " + childElement.ownText());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //about to get curated data set on sizes of items
                List<String> verifiedSizes = new ArrayList<>();
                if(sizeList != null && !sizeList.isEmpty()){
                    for(Size size : sizeList){
                        verifiedSizes.addAll(size.getSizes());
                    }
                }

                //Verifying sizes against curated data set
                if(!unverified.isEmpty() && !verifiedSizes.isEmpty()){
                    List<String> intersection = getIntersection(unverified, verifiedSizes);

                    //Found an intersection between our knowledge and unverified data set.
                    if(!intersection.isEmpty()) {
                        sizes.addAll(intersection);
                    } else {
                        //Verify and include unknown knowledge about sizes to knowledge data set.
                        logger.info("Unknown sizes: "+ unverified);
                    }
                }
            }

            //Remove elements of size found that have the exclusion words in them.
            Map<String, List<String>> exclusionSizeMap = Size.loadSizeExclusionList();
            List<String> sizeExclusionList = null;
            if(exclusionSizeMap != null && !exclusionSizeMap.isEmpty()){
                for(Map.Entry<String, List<String>> m : exclusionSizeMap.entrySet()){
                    if(m.getKey().equals(SizeProperties.EXCLUSION_LIST.toString())){
                        sizeExclusionList = m.getValue();
                    }
                }
            }

            if(sizeExclusionList != null && !sizes.isEmpty()){
                List<String> updatedSizes = new ArrayList<>();
                for(String s : sizes){
                    int x = 0;
                    for(String s1: sizeExclusionList){
                        boolean isPresent = AppUtils.regExContains(s1, s);
                        if(!isPresent){ x++; }
                    }
                    if(x == sizeExclusionList.size()){ updatedSizes.add(s); }
                }

                if(!updatedSizes.isEmpty()){
                    sizes.clear();
                    sizes.addAll(updatedSizes);
                }
            }
        }
        return sizes;
    }

    @Override
    public String getCategoryByTerm(String term) {
        String category = null;
        if(StringUtils.isNotBlank(term)){
            List<Categories> categoriesList = getCategories();
            if(!categoriesList.isEmpty()){
                for(Categories c : categoriesList){
                    List<String> attributes = c.getAttributes();
                    if(!attributes.isEmpty()) {
                        for(String s : attributes) {
                            if (term.equalsIgnoreCase(s)) {
                                category = c.getCategory();
                            }
                        }
                    }
                }
            }
        }
        return category;
    }

    @Override
    public String removePossibleInputFieldFromText(String text){
        if(StringUtils.isNotBlank(text)){
            List<String> textToBeRemoved = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\<\\binput\\b\\s+.*\\/\\>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()){
                textToBeRemoved.add(matcher.group());
            }

            if(!textToBeRemoved.isEmpty()){
                for(String s : textToBeRemoved){
                    text = text.replace(s,"");
                }
            }
        }
        return text;
    }

    @Override
    public String removePossibleImagesFromText(String text){
        if(StringUtils.isNotBlank(text)){
            List<String> textImagesToBeRemoved = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\<\\bimg\\b\\s+.*\\/\\>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()){
                textImagesToBeRemoved.add(matcher.group());
            }

            if(!textImagesToBeRemoved.isEmpty()){
                for(String s : textImagesToBeRemoved){
                    text = text.replace(s, "");
                }
            }
        }
        return text;
    }

    @Override
    public String removeNavigationAndMenuBars(String text) {
        if(StringUtils.isNotBlank(text)){
            List<String> textToBeRemoved = new ArrayList<>();
            String regEx = "\\<(\\bdiv\\b|\\bul\\b)\\s(\\bclass\\b|\\bid\\b)\\=\\\".*?(nav|menu|dropdown).*[\\S\\s]*?(\\<\\/\\bdiv\\b\\>|\\<\\/\\bul\\b\\>)";
            //String regEx = "\\<(\\bdiv\\b|\\bul\\b)\\s\\bclass\\b\\=\\\"(nav|menu).*[\\S\\s]*?(\\<\\/\\bdiv\\b\\>|\\<\\/\\bul\\b\\>)";
            Pattern pattern = Pattern
                    .compile(regEx,
                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()){
                textToBeRemoved.add(matcher.group());
            }

            if(!textToBeRemoved.isEmpty()){
                for(String s : textToBeRemoved){
                    text = text.replace(s, "");
                }
            }
        }
        return text;
    }

    @Override
    public String[] getSentencesWithTerm(String[] sentences, String term){
        String[] foundSentences = null;
        if(sentences != null && sentences.length > 0 && StringUtils.isNotBlank(term)){
            int x = 0;
            List<String> f = new ArrayList<>();
            for(String sentence : sentences) {
                Pattern pattern = Pattern.compile("\\b"+ term +"\\b", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(sentence);
                while(matcher.find()) {
                    f.add(sentence);
                    logger.info("Sentence: " + x + " : " + sentence);
                    x++;
                }
            }

            if(!f.isEmpty()) {
                Set<String> cleanUp = new HashSet<>();
                cleanUp.addAll(f);
                f.clear();
                f.addAll(cleanUp);
                foundSentences = f.toArray(new String[f.size()]);
            }
        }
        return foundSentences;
    }

    @Override
    public List<ResponseCategoryToAttribute> getCombinedMatrix(
            List<ResponseCategoryToAttribute> responseCategoryToAttributes){
        List<ResponseCategoryToAttribute> updated = new ArrayList<>();
        List<CombinationMatrix> combinationMatrixList = CombinationMatrix.getCombinationMatrix();
        if(responseCategoryToAttributes != null && !responseCategoryToAttributes.isEmpty()){
            List<String> attributes = new ArrayList<>();
            Set<String> colors = new HashSet<>();
            Map<String, Object> genderMap = new HashMap<>();
            Map<String, List<String>> materialsMap = new HashMap<>();
            Map<String, List<String>> sizesMap = new HashMap<>();
            Map<String, Object> pricingMap = new HashMap<>();

            Map<String, List<String>> categoryToAttributes = new HashMap<>();

            for(ResponseCategoryToAttribute r : responseCategoryToAttributes){
                attributes.add(r.getCategory());

                if(!categoryToAttributes.containsKey(r.getCategory())){
                    categoryToAttributes.put(r.getCategory(), r.getAttributes());
                } else {
                    List<String> attrs = categoryToAttributes.get(r.getCategory());
                    attrs.addAll(r.getAttributes());

                    //Clean up of duplicated attributes.
                    Set<String> cleanUp = new HashSet<>();
                    cleanUp.addAll(attrs);
                    attrs.clear();
                    attrs.addAll(cleanUp);

                    categoryToAttributes.put(r.getCategory(), attrs);
                }

                List<String> exitingColors = r.getColors();
                if(!exitingColors.isEmpty()){
                    colors.addAll(r.getColors());
                }

                String gender = r.getGender();
                if(StringUtils.isNotBlank(gender)){
                    genderMap.put(r.getCategory(), gender);
                }

                List<String> materials = r.getMaterials();
                if(materials != null && !materials.isEmpty()){
                    materialsMap.put(r.getCategory(), materials);
                }

                List<String> sizes = r.getSizes();
                if(sizes != null && !sizes.isEmpty()){
                    sizesMap.put(r.getCategory(), sizes);
                }

                Map<String, Object> pricing = r.getPricing();
                if (pricing != null && !pricing.isEmpty()){
                    pricingMap.put(r.getCategory(), pricing);
                }
            }

            boolean isRuled = false;
            String proposeCategory = null;
            List<String> intersection = null;
            String includedCategory = null;

            if(!combinationMatrixList.isEmpty()){
                for(CombinationMatrix c : combinationMatrixList) {
                    List<String> matrixList = c.getCombinedCategories();

                    if(!matrixList.isEmpty()) {
                        includedCategory = matrixList.get(0);
                    }

                    intersection = getIntersection(attributes, matrixList);
                    if(!intersection.isEmpty() && intersection.size() == matrixList.size()){
                        isRuled = true;
                        proposeCategory = c.getCategories();

                    }
                }
            }

            if(isRuled && StringUtils.isNotBlank(proposeCategory)){
                ResponseCategoryToAttribute responseCategoryToAttribute = new ResponseCategoryToAttribute();
                if(!categoryToAttributes.isEmpty()) {
                    if(intersection != null) {
                        List<String> combinedAttributes = new ArrayList<>();
                        for(String i : intersection) {
                            if (categoryToAttributes.containsKey(i)) {
                                combinedAttributes.addAll(categoryToAttributes.get(i));
                            }
                        }

                        //Clean up attributes to remove attribute duplicates
                        Set<String> cleanUp = new HashSet<>();
                        cleanUp.addAll(combinedAttributes);
                        combinedAttributes.clear();
                        combinedAttributes.addAll(cleanUp);

                        responseCategoryToAttribute.setAttributes(combinedAttributes);
                    }
                }
                responseCategoryToAttribute.setCategory(proposeCategory);

                List<String> updatedColors = new ArrayList<>();
                updatedColors.addAll(colors);
                responseCategoryToAttribute.setColors(updatedColors);

                if(!genderMap.isEmpty()){
                    responseCategoryToAttribute.setGender(genderMap.get(includedCategory).toString());
                }

                //Add materials found to combined response.
                if(!materialsMap.isEmpty()){
                    responseCategoryToAttribute.setMaterials(materialsMap.get(includedCategory));
                }

                //add sizes found found for combined response.
                if(!sizesMap.isEmpty()){
                   responseCategoryToAttribute.setSizes(sizesMap.get(includedCategory));
                }

                //Get category if proposed category is also found in incoming ResponseCategory
                if(attributes.contains(proposeCategory)){
                    for(ResponseCategoryToAttribute r : responseCategoryToAttributes) {
                        if(r.getCategory().equalsIgnoreCase(proposeCategory)){
                            List<String> existingAttributes = responseCategoryToAttribute.getAttributes();
                            existingAttributes.addAll(r.getAttributes());

                            //Clean up of duplicated attributes
                            Set<String> cleanUp = new HashSet<>();
                            cleanUp.addAll(existingAttributes);
                            existingAttributes.clear();
                            existingAttributes.addAll(cleanUp);

                            responseCategoryToAttribute.setAttributes(existingAttributes);
                        }
                    }
                }

                updated.add(responseCategoryToAttribute);
            }
        }
        return updated;
    }

    //Used to group a collection of responses attributes by categories.
    public List<ResponseCategoryToAttribute> groupResponseByCategory(
            List<ResponseCategoryToAttribute> responseCategoryToAttributes){
        List<ResponseCategoryToAttribute> updated = new ArrayList<>();
        if(responseCategoryToAttributes != null && !responseCategoryToAttributes.isEmpty()){
            Map<String, List<String>> categoryToAttributes = new HashMap<>();
            Map<String, List<String>> colorsMap = new HashMap<>();
            Map<String, String> genderMap = new HashMap<>();
            Map<String, List<String>> materialsMap = new HashMap<>();
            Map<String, List<String>> sizesMap = new HashMap<>();
            Map<String, Map<String, Object>> pricingMap = new HashMap<>();

            for(ResponseCategoryToAttribute r : responseCategoryToAttributes){

                if(!categoryToAttributes.containsKey(r.getCategory())){
                    categoryToAttributes.put(r.getCategory(), r.getAttributes());
                } else {
                    List<String> attrs = categoryToAttributes.get(r.getCategory());
                    attrs.addAll(r.getAttributes());

                    Set<String> set = new HashSet<>();
                    set.addAll(attrs);
                    attrs.clear();
                    attrs.addAll(set);

                    categoryToAttributes.put(r.getCategory(), attrs);
                }

                colorsMap.put(r.getCategory(), r.getColors());
                genderMap.put(r.getCategory(), r.getGender());
                materialsMap.put(r.getCategory(), r.getMaterials());
                sizesMap.put(r.getCategory(), r.getSizes());
                pricingMap.put(r.getCategory(), r.getPricing());
            }

            if(!categoryToAttributes.isEmpty()){
                for(String ca : categoryToAttributes.keySet()){
                    ResponseCategoryToAttribute r = new ResponseCategoryToAttribute();
                    r.setCategory(ca);
                    r.setAttributes(categoryToAttributes.get(ca));
                    r.setColors(colorsMap.get(ca));
                    r.setGender(genderMap.get(ca));
                    r.setMaterials(materialsMap.get(ca));
                    r.setSizes(sizesMap.get(ca));
                    r.setPricing(pricingMap.get(ca));
                    updated.add(r);
                }
            }
        }
        return updated;
    }

    /**
     * This method in an implementation to retrieve price for a given item or content.
     */
    @Override
    public Map<String, Object> getPrice(String text){
        Map<String, Object> results = new HashMap<>();
        if(StringUtils.isNotBlank(text)){
            Document document = null;
            try{
                document = JsoupImpl.parseHtml(text);
            } catch (Exception e){
                logger.debug("Error in parsing document. Message: "+ e.getMessage());
            }

            String[] html5Data = {"[^data-]"};
            if(html5Data != null && html5Data.length > 0){
                if(document != null) {
                    for (String s : html5Data) {
                        if (StringUtils.isNotBlank(s)) {
                            Elements elements = document.select(s);
                            if (!elements.isEmpty()) {
                                Iterator<Element> elementsIterator = elements.iterator();
                                while (elementsIterator.hasNext()) {
                                    Element element = elementsIterator.next();
                                    String outerHtml = element.outerHtml();

                                    boolean isPresent = AppUtils.regExContains("(price)", outerHtml);
                                    if (isPresent) {
                                        Elements children = element.children();
                                        if (!children.isEmpty()) {
                                            Iterator<Element> elementIterator = children.iterator();
                                            while (elementIterator.hasNext()) {
                                                Element child = elementIterator.next();
                                                if (StringUtils.isNotBlank(child.text())) {
//                                                    unverified.add(child.text());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     *
     */
    @Override
    public Map<String, Object> getGender(String[] sentences, String keywords, String description){
        Map<String, Object> results = new HashMap<>();
        if(sentences != null && sentences.length > 0){
            StringBuilder sentenceAsString = new StringBuilder();
            int x = 0;
            for(String sentence : sentences){
                if(x < (sentences.length - 1)) {
                    sentenceAsString.append(sentence.trim().toLowerCase() + "\n");
                } else {
                    sentenceAsString.append(sentence.trim().toLowerCase());
                }
                x++;
            }

            String possibleTitle = getPossibleTitle(sentences);

            if(StringUtils.isNotBlank(sentenceAsString)) {
                String[] tokens = tokenize(sentenceAsString.toString().toLowerCase());
                List<String> listTokens = Arrays.asList(tokens);

                List<Gender> genderMetrics = Gender.loadGenderMatrix();
                List<String> strGenderMetrics = new ArrayList<>();
                if(!genderMetrics.isEmpty()){
                    for(Gender g : genderMetrics){
                        strGenderMetrics.add(g.getAttribute());
                    }
                }

                List<String> intersection = null;
                if(!strGenderMetrics.isEmpty() && !listTokens.isEmpty()){
                    intersection = getIntersection(strGenderMetrics, listTokens);
                }

                List<Map> scoredTermsFromContent = new ArrayList<>();

                if(intersection != null){
                    List<TFIDFWeightedScore> tfIdfWeightedScores = new ArrayList<>();
                    for(String s : intersection){
                        TFIDFWeightedScore tfidfWeightedScore = getTfIdfWeightedScore(tokens, s);
                        tfIdfWeightedScores.add(tfidfWeightedScore);
                    }
                    Collections.sort(tfIdfWeightedScores, TFIDFWeightedScore.tfidfWeightedScoreComparator);

                    List<String> scoredTerms = new LinkedList<>();

                    if (!tfIdfWeightedScores.isEmpty()){
                        for(TFIDFWeightedScore tfidfWeightedScore : tfIdfWeightedScores) {
                            scoredTerms.add(tfidfWeightedScore.getTerm());
                            scoredTermsFromContent.add(tfidfWeightedScore.toMap());
                        }
                    }

                    List<TotalTermToGroup> totalTermToGroups = new ArrayList<>();

                    if(!scoredTerms.isEmpty()){
                        List<TermToGroupScore> groupScoreList = new ArrayList<>();
                        List<ContentAreaGroupings> cList = ContentAreaGroupings.contentAreaGroupingsList();

                        for (ContentAreaGroupings c : cList) {
                            for (String s : scoredTerms) {
                                TermToGroupScore termToGroupScore =
                                        getTermToGroupScore(c,s, possibleTitle, keywords, description);
                                groupScoreList.add(termToGroupScore);
                            }
                        }

                        //Present total scoring on all terms found.
                        if (!groupScoreList.isEmpty()) {
                            Map<String, List<TermToGroupScore>> m = new HashMap<>();
                            for (ContentAreaGroupings c : cList) {
                                List<TermToGroupScore> t = getTermToGroupByContentAreaGroupings(groupScoreList, c);
                                m.put(c.toString(), t);
                            }

                            Map<String, Integer> totalTermToGroup = new HashMap<>();
                            for (ContentAreaGroupings c : cList) {
                                if (m.containsKey(c.toString())) {
                                    List<TermToGroupScore> tag = m.get(c.toString());
                                    if (tag != null && !tag.isEmpty()) {
                                        for (TermToGroupScore t : tag) {
                                            if (totalTermToGroup.containsKey(t.getTerm())) {
                                                Integer i = totalTermToGroup.get(t.getTerm());
                                                Integer x1 = i + t.getScore();
                                                totalTermToGroup.put(t.getTerm(), x1);
                                            } else {
                                                totalTermToGroup.put(t.getTerm(), t.getScore());
                                            }
                                        }
                                    }
                                }
                            }

                            if (!scoredTermsFromContent.isEmpty()) {
                                for (Map m1 : scoredTermsFromContent) {
                                    TotalTermToGroup totalTermToGroup1 = new TotalTermToGroup();
                                    String term = null;
                                    Double o = null;
                                    Integer p = null;

                                    if (m1.containsKey("term")) {
                                        term = m1.get("term").toString();
                                        totalTermToGroup1.setTerm(term);
                                    }

                                    //This gets and sets term frequency count from scoredTermsFromContent
                                    if (m1.containsKey("score")) {
                                        Object s = m1.get("score");
                                        if (s instanceof Double) {
                                            o = (Double) s;
                                            totalTermToGroup1.setTermFrequencyScore(o);
                                        }
                                    }

                                    //This gets and sets term count from grouped areas
                                    if (StringUtils.isNotBlank(term)) {
                                        if (totalTermToGroup.containsKey(term)) {
                                            p = totalTermToGroup.get(term);
                                            totalTermToGroup1.setTermToGroupScore(p);
                                        }
                                    }

                                    if (o != null && p != null) {
                                        Double t = TotalTermToGroup.calculateWeightedScore(p, o);
                                        totalTermToGroup1.setWeightTotalScore(t);
                                    }
                                    totalTermToGroups.add(totalTermToGroup1);
                                }
                            }

                            //Present total scoring on all terms found.
                            if (!totalTermToGroups.isEmpty()) {
                                Collections.sort(totalTermToGroups, TotalTermToGroup.totalTermToGroupComparator);
                            }

                            if(!totalTermToGroups.isEmpty()) {
                                TotalTermToGroup toGroup = totalTermToGroups.get(0);
                                results.put("gender", toGroup.getTerm());
                            }
                        }
                    }
                }

                //get all possessive nouns
//                List<Map> posMap = getPos(tokenize(possibleTitle));
//                if(!posMap.isEmpty()){
//                    for(Map m : posMap){
//                        if(m.containsKey("pos")){
//                            String pos = m.get("pos").toString();
//                            //logger.info("POS: "+ pos);
//                        }
//                    }
//                }

            }
        }
        return results;
    }


    /**
     * Get possible title from content.
     * @param sentences
     * @return
     */
    @Override
    public String getPossibleTitle(String[] sentences){
        //Get possible title or item description from first list
        String possibleTitle = null;
        if (sentences != null && sentences.length > 0) {
            if (StringUtils.isNotBlank(sentences[0])) {
                String t = sentences[0];
                if (t.contains("\n")) {
                    possibleTitle = t.substring(0, t.indexOf("\n"));
                }
            }
        }
        //End of getting a possible title.
        return possibleTitle;
    }


    /**
     * Get TFIDFWeightedScore score for a term and it's tokens
     * @param tokens
     * @param term
     * @return
     */
    @Override
    public TFIDFWeightedScore getTfIdfWeightedScore(String[] tokens, String term){
        if(tokens != null && tokens.length > 0 && StringUtils.isNotBlank(term)){
            double tfScore = getTFScore(tokens, term);
            double idfScore = getIdfScore(tokens, term);
            double tfIdfWeightScore = getTfIdfWeightScore(tokens, term);

            TFIDFWeightedScore tfidfWeightedScore = new TFIDFWeightedScore();
            tfidfWeightedScore.setTerm(term);

            if(!Double.isNaN(tfIdfWeightScore) && !Double.isInfinite(tfIdfWeightScore)) {
                tfidfWeightedScore.setScore(tfIdfWeightScore);
            } else {
                tfidfWeightedScore.setScore(0d);
            }

            if(!Double.isNaN(idfScore) && !Double.isInfinite(idfScore)) {
                tfidfWeightedScore.setIdfScore(idfScore);
            } else {
                tfidfWeightedScore.setIdfScore(0d);
            }

            if(!Double.isNaN(tfScore) && !Double.isInfinite(tfScore)) {
                tfidfWeightedScore.setTfScore(tfScore);
            } else {
                tfidfWeightedScore.setTfScore(0d);
            }
            return tfidfWeightedScore;
        }
        return null;
    }

    @Override
    public TermToGroupScore getTermToGroupScore(ContentAreaGroupings contentAreaGroupings,
                                                String term, String description, String title, String keywords){
        if(contentAreaGroupings != null) {
            TermToGroupScore termToGroupScore = new TermToGroupScore();
            termToGroupScore.setGroup(contentAreaGroupings);
            termToGroupScore.setTerm(term);

            switch (contentAreaGroupings) {
                case BODY:
                    termToGroupScore.setScore(1);
                    break;
                case TITLE:
                    if (StringUtils.isNotBlank(title)) {
                        Integer tScore = getTermToGroupScore(term, title);
                        termToGroupScore.setScore(tScore);
                    } else {
                        termToGroupScore.setScore(0);
                    }
                    break;
                case DESCRIPTION:
                    if (StringUtils.isNotBlank(description)) {
                        Integer tDesc = getTermToGroupScore(term, description);
                        termToGroupScore.setScore(tDesc);
                    } else {
                        termToGroupScore.setScore(0);
                    }
                    break;
                case KEYWORDS:
                    if (StringUtils.isNotBlank(keywords)) {
                        Integer tKeywords = getTermToGroupScore(term, keywords);
                        termToGroupScore.setScore(tKeywords);
                    } else {
                        termToGroupScore.setScore(0);
                    }
                    break;
                default:
                    termToGroupScore.setScore(0);
                    break;
            }
            return termToGroupScore;
        }

        return null;
    }

    @Override
    public ResponseCategoryToAttribute refineResultSet(List<ResponseCategoryToAttribute> responseCategoryToAttributeList,
                                                       RulesEngineDataSet rulesEngineDataSet){
        ResponseCategoryToAttribute responseCategoryToAttribute = null;
        if(!responseCategoryToAttributeList.isEmpty()){
            Iterable<RulesEngineModel> rulesEngineModels = rulesEngineModelService.findAll();
            Iterator<RulesEngineModel> rulesEngineModelIterator = rulesEngineModels.iterator();

            List<ResponseCategoryToAttribute> occurrence = new ArrayList<>();

            if(rulesEngineModelIterator != null){
                while(rulesEngineModelIterator.hasNext()){
                    RulesEngineModel rulesEngineModel = rulesEngineModelIterator.next();
                    List<Map> rules = rulesEngineModel.getRules();
                    if(rules != null && !rules.isEmpty()){
                        for(Map<String, String> m : rules){
                            RulesEngineTask rulesEngineTask = null;
                            RuleEngineInput ruleEngineInput = null;
                            RuleEngineDataSet ruleEngineDataSet = null;

                            for(Map.Entry<String, String> mRules : m.entrySet()){
                                if(mRules.getKey().equalsIgnoreCase("task")){
                                    rulesEngineTask = RulesEngineTask.fromString(mRules.getValue());
                                }

                                if(mRules.getKey().equalsIgnoreCase("input")){
                                    ruleEngineInput = RuleEngineInput.fromString(mRules.getValue());
                                }

                                if(mRules.getKey().equalsIgnoreCase("dataset")){
                                    ruleEngineDataSet = RuleEngineDataSet.fromString(mRules.getValue());
                                }
                            }


                            if(rulesEngineTask != null){
                                switch (rulesEngineTask){
                                    case OCCURRENCE:
                                        Map<ResponseCategoryToAttribute, Double> rScore = new HashMap<>();

                                        List<ResponseToAttributeClusterScore> responseToAttributeClusterScoreList = new ArrayList<>();

                                        if(!responseCategoryToAttributeList.isEmpty()) {
                                            for(ResponseCategoryToAttribute r : responseCategoryToAttributeList) {
                                                double categoryToAttribute =
                                                        applyRulesEngineOccurrence(r,
                                                                rulesEngineDataSet,
                                                                ruleEngineInput,
                                                                ruleEngineDataSet);
                                                ResponseToAttributeClusterScore responseToAttributeClusterScore = new ResponseToAttributeClusterScore();
                                                responseToAttributeClusterScore.setResponseCategoryToAttribute(r);
                                                responseToAttributeClusterScore.setScore(categoryToAttribute);
                                                responseToAttributeClusterScoreList.add(responseToAttributeClusterScore);
                                            }
                                        }
                                        Collections.sort(responseToAttributeClusterScoreList, ResponseToAttributeClusterScore.responseToAttributeClusterScoreComparator);
                                        Collections.reverse(responseToAttributeClusterScoreList);

                                        if(!responseCategoryToAttributeList.isEmpty()) {
                                            responseCategoryToAttribute = responseToAttributeClusterScoreList.get(0)
                                                    .getResponseCategoryToAttribute();
                                        }
                                        break;
                                    default:

                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return responseCategoryToAttribute;
    }

    @Override
    public double applyRulesEngineOccurrence(ResponseCategoryToAttribute responseCategoryToAttribute,
                                                                  RulesEngineDataSet rulesEngineDataSet,
                                                                  RuleEngineInput ruleEngineInput,
                                                                  RuleEngineDataSet ruleEngineDataset){
        double r = 0d;
        if (responseCategoryToAttribute != null){
            Map<String, Object> rMap = responseCategoryToAttribute.toMap();
            Map<String, Object> dMap = rulesEngineDataSet.toMap();

            String inputKey = ruleEngineInput.toString();
            Object inputValue = null;

            if(rMap.containsKey(inputKey)){
                inputValue = rMap.get(inputKey);
            }

            String dataSetKey = ruleEngineDataset.toString();
            Object dataSetValue = null;

            if(dMap.containsKey(dataSetKey)){
                dataSetValue = dMap.get(dataSetKey);
            }

            List<String> dataSetList = new ArrayList<>();
            if (dataSetValue instanceof String){
                String a = dataSetValue.toString();
                dataSetList.addAll(Arrays.asList(tokenize(a)));
            }

            List<String> inputDataSet = new ArrayList<>();
            if (inputValue instanceof List) {
                inputDataSet.addAll((List) inputValue);
            }

            if(!dataSetList.isEmpty() && !inputDataSet.isEmpty()){
                List<String> unionDataSet = classification.union(dataSetList, inputDataSet);
                double[] iD = new double[unionDataSet.size()];
                double[] dD = new double[unionDataSet.size()];

                if(!unionDataSet.isEmpty()){
                    int x = 0;
                    for(String s : unionDataSet){
                        if(dataSetKey.contains(s)){ iD[x] = 1d; } else { iD[x] = 0d; }
                        if (inputDataSet.contains(s)){ dD[x] = 1d; } else { dD[x] = 0d; }
                        x++;
                    }
                }

                Instance a = new DenseInstance(iD);
                Instance b = new DenseInstance(dD);

                Dataset dataset = new DefaultDataset();
                dataset.add(a);
                dataset.add(b);

                XMeans xm = new XMeans();
                Clusterer kMeans = new WekaClusterer(xm);
                Dataset[] cluster = kMeans.cluster(dataset);

                ClusterEvaluation clusterEvaluation = new SumOfAveragePairwiseSimilarities();
                ClusterEvaluation evaluation1 = new SumOfCentroidSimilarities();
                ClusterEvaluation evaluation2 = new AICScore();
                ClusterEvaluation evaluation3 = new BICScore();

                double sumOfAveragePairwiseSimilarities = clusterEvaluation.score(cluster);
                double sumOfCentroidSimilarities = evaluation1.score(cluster);
                double aicScore = evaluation2.score(cluster);
                double bicScore = evaluation3.score(cluster);

                r = bicScore;
                logger.info("BIC score for: "+ responseCategoryToAttribute.toString() + " is: "+ r);
            }
        }
        return r;
    }

    @Override
    public List<String> colorsVerification(List<Map> colorsValidated){
        List<String> colors = new ArrayList<>();
        if(colorsValidated != null && !colorsValidated.isEmpty()){
            List<String> nonValidated = new ArrayList<>();
            for(Map<String, Object> m : colorsValidated){
                String color = null;
                if(m.containsKey("name")){
                    color = m.get("name").toString();
                }

                boolean isValidated = false;
                if(m.containsKey("isValidated")){
                    isValidated = Boolean.parseBoolean(m.get("isValidated").toString());
                }

                if(!isValidated){
                    nonValidated.add(color);
                }
            }

            if(!nonValidated.isEmpty()){
                List<Color> colorList = Color.loadColors();
                Map<String, Double> colorSimilarityScores = new HashMap<>();
                ValueComparator valueComparator = new ValueComparator(colorSimilarityScores);
                TreeMap<String, Double> treeMap = new TreeMap<>(valueComparator);

                for(String s : nonValidated){
                    //Distance computation between an unknown color and a curated one.
                    Color color = new Color();
                    color.setName(s.trim().toLowerCase());

                    double similarityRate = Color.similarityAgainstCuratedColors(colorList, color);
                    colorSimilarityScores.put(color.toString(), similarityRate);
                }

                if(!colorSimilarityScores.isEmpty()){
                    treeMap.putAll(colorSimilarityScores);
                    if(!treeMap.isEmpty()){
                        for(Map.Entry<String, Double> m : treeMap.entrySet()){
                            if(m.getValue() > 0D){
                                colors.add(m.getKey());
                            }
                        }
                    }
                }

//                if(!colors.isEmpty()){
//                    List<String> colorDescriptors = ColorsDescription.colorDescriptorsWords();
//                    List<String> unwantedColorDescriptors = new ArrayList<>();
//                    if(!colorDescriptors.isEmpty()){
//                        for(String c : colors){
//                            List<String> tokenize = Arrays.asList(tokenize(c));
//                            if(!tokenize.isEmpty()){
//                                List<String> intersection = getIntersection(tokenize, colorDescriptors);
//                                if (!intersection.isEmpty()) {
//
//                                } else {
//                                    unwantedColorDescriptors.add(c);
//                                }
//                            }
//                        }
//                    }
//
////                    if(!unwantedColorDescriptors.isEmpty()){
////                        for(String uw : unwantedColorDescriptors){
////                            colors.remove(uw);
////                        }
////                    }
//                }
            }
        }
        return colors;
    }

    @Override
    public List<String> colorsVerified(List<Map> colorsValidated){
        List<String> validated = new ArrayList<>();
        if(colorsValidated != null && !colorsValidated.isEmpty()) {
            for (Map<String, Object> m : colorsValidated) {
                String color = null;
                if (m.containsKey("name")) {
                    color = m.get("name").toString();
                }

                boolean isValidated = false;
                if (m.containsKey("isValidated")) {
                    isValidated = Boolean.parseBoolean(m.get("isValidated").toString());
                }

                if (isValidated) {
                    validated.add(color);
                }
            }
        }
        return validated;
    }
}
