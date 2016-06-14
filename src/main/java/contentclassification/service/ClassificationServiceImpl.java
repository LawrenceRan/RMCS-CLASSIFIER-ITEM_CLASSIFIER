package contentclassification.service;

import contentclassification.config.TermsScoringConfig;
import contentclassification.domain.*;
import contentclassification.utilities.BM25;
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
                                try {
                                    m.put(a3[0], a3[1]);
                                } catch(ArrayIndexOutOfBoundsException e){
                                    e.printStackTrace();
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

            if(!inputFields.isEmpty()){
                try{
                    String colorTagAttributeName = null;
                    if(document != null){
                        List<String> colorIds = new ArrayList<>();
                        Elements elements = document.getElementsByTag("input");
                        if(!elements.isEmpty()){
                            Iterator<Element> elementIterator = elements.iterator();
                            while(elementIterator.hasNext()){
                                Element element = elementIterator.next();
                                String attributeName = element.attr("name");
                                if(AppUtils.regExContains("(color|colour)", attributeName)){
                                    colorTagAttributeName = element.attr("name");
                                    String colorId = element.id();
                                    String val = element.val();
                                    colorIds.add(colorId);
                                }
                            }
                        }

                        if(!colorIds.isEmpty()) {
                            for(String c : colorIds) {
                                Elements colorElements = document.getElementsByAttributeValue("for", c);
                                if(!colorElements.isEmpty()) {
                                    Iterator<Element> elementIterator = colorElements.iterator();
                                    while(elementIterator.hasNext()) {
                                        Element element = elementIterator.next();
                                        String colorText = element.text();
                                        if(StringUtils.isBlank(colorText)) {
                                            String dataId = element.attr("data-id");
                                            if(StringUtils.isNotBlank(dataId)){
                                                List<String> tokenAttributeName = new ArrayList<>();
                                                if(StringUtils.isNotBlank(colorTagAttributeName)) {
                                                    tokenAttributeName
                                                            .addAll(Arrays.asList(colorTagAttributeName.split("-")));
                                                }
                                                List<String> tokenDataId = Arrays.asList(dataId.split("-"));
                                                List<String> intersection = getIntersection(tokenAttributeName, tokenDataId);
                                                if(!intersection.isEmpty()){
                                                    //remove intersected words from data id attribute's value.
                                                    for(String i : intersection){
                                                        dataId = dataId.replace(i, "");
                                                    }
                                                    colors.add(dataId.replace("-",""));
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
                } catch(Exception e){
                    logger.debug("Error: "+ e.getMessage());
                }
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

            Map<String, List<String>> categoryToAttributes = new HashMap<>();

            for(ResponseCategoryToAttribute r : responseCategoryToAttributes){
                attributes.add(r.getCategory());

                if(!categoryToAttributes.containsKey(r.getCategory())){
                    categoryToAttributes.put(r.getCategory(), r.getAttributes());
                } else {
                    List<String> attrs = categoryToAttributes.get(r.getCategory());
                    attrs.addAll(r.getAttributes());
                    categoryToAttributes.put(r.getCategory(), attrs);
                }

                List<String> exitingColors = r.getColors();
                if(!exitingColors.isEmpty()){
                    colors.addAll(r.getColors());
                }
            }

            boolean isRuled = false;
            String proposeCategory = null;
            List<String> intersection = null;

            if(!combinationMatrixList.isEmpty()){
                for(CombinationMatrix c : combinationMatrixList) {
                    List<String> matrixList = c.getCombinedCategories();
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
                        responseCategoryToAttribute.setAttributes(combinedAttributes);
                    }
                }
                responseCategoryToAttribute.setCategory(proposeCategory);

                List<String> updatedColors = new ArrayList<>();
                updatedColors.addAll(colors);
                responseCategoryToAttribute.setColors(updatedColors);

                //Get category if proposed category is also found in incoming ResponseCategory
                if(attributes.contains(proposeCategory)){
                    for(ResponseCategoryToAttribute r : responseCategoryToAttributes) {
                        if(r.getCategory().equalsIgnoreCase(proposeCategory)){
                            List<String> existingAttributes = responseCategoryToAttribute.getAttributes();
                            existingAttributes.addAll(r.getAttributes());
                            responseCategoryToAttribute.setAttributes(existingAttributes);
                        }
                    }
                }

                updated.add(responseCategoryToAttribute);
            }
        }
        return updated;
    }

    public List<ResponseCategoryToAttribute> groupResponseByCategory(
            List<ResponseCategoryToAttribute> responseCategoryToAttributes){
        List<ResponseCategoryToAttribute> updated = new ArrayList<>();
        if(responseCategoryToAttributes != null && !responseCategoryToAttributes.isEmpty()){
            Map<String, List<String>> categoryToAttributes = new HashMap<>();
            Map<String, List<String>> colorsMap = new HashMap<>();
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

            }

            if(!categoryToAttributes.isEmpty()){
                for(String ca : categoryToAttributes.keySet()){
                    ResponseCategoryToAttribute r = new ResponseCategoryToAttribute();
                    r.setCategory(ca);
                    r.setAttributes(categoryToAttributes.get(ca));
                    r.setColors(colorsMap.get(ca));
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
}
