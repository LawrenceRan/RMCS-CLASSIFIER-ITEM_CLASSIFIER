package contentclassification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import contentclassification.config.ClassificationConfig;
import contentclassification.config.RequestProxy;
import contentclassification.config.WordNetDictConfig;
import contentclassification.domain.AppUtils;
import contentclassification.domain.Categories;
import contentclassification.domain.Color;
import contentclassification.domain.ContentAreaGroupings;
import contentclassification.domain.FabricName;
import contentclassification.domain.LanguageSymbols;
import contentclassification.domain.Languages;
import contentclassification.domain.LearningImpl;
import contentclassification.domain.NameAndContentMetaData;
import contentclassification.domain.POSRESPONSES;
import contentclassification.domain.ResponseCategoryToAttribute;
import contentclassification.domain.ResponseMap;
import contentclassification.domain.RestResponseKeys;
import contentclassification.domain.RulesEngineDataSet;
import contentclassification.domain.TFIDFWeightedScore;
import contentclassification.domain.TermSimilarityToList;
import contentclassification.domain.TermToGroupScore;
import contentclassification.domain.TotalTermToGroup;
import contentclassification.domain.WebMetaName;
import contentclassification.service.ClassificationServiceImpl;
import contentclassification.service.JsoupService;
import contentclassification.service.LearningService;
import contentclassification.service.SpellCheckerServiceImpl;
import contentclassification.service.ThirdPartyProviderService;
import contentclassification.service.WordNetService;
import contentclassification.utilities.HelperUtility;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.evaluation.NominalPrediction;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@Controller
public class Index {
    private static final Logger logger = LoggerFactory.getLogger(Index.class);

    @Autowired
    private JsoupService jsoupService;

    @Autowired
    private WordNetService wordNetService;

    @Autowired
    private WordNetDictConfig wordNetDictConfig;

    @Autowired
    private ClassificationServiceImpl classificationService;

    @Autowired
    private ClassificationConfig classificationConfig;

    @Autowired
    private RequestProxy requestProxy;

    @Autowired
    private ThirdPartyProviderService thirdPartyProviderService;

    @Autowired
    private SpellCheckerServiceImpl spellCheckerService;

    @Autowired
    private LearningService learningService;

//    @Autowired
//    private DomainGraphDBImpl domainGraphDB;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> data = new HashMap<>();
        data.put("message", "welcome to item classification test application.");
        modelAndView.addObject("data", data);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/feed", method = RequestMethod.GET)
    public ModelAndView analyzeFeed(@RequestParam(required = true) String url) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }

    @RequestMapping(value = "/v1/text", method = RequestMethod.GET)
    public ModelAndView analyzeText(@RequestParam(required = true) String text) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String sessionId = attr.getSessionId();

        if(StringUtils.isNotBlank(text)){
            logger.info("[ "+ sessionId +" ] About to get tokens of text.");
            String[] tokens = classificationService.tokenize(text);
            logger.info("[ "+ sessionId +" ] Done generating tokens text.");

            logger.info("About to detect sentences.");
            String[] sentences = classificationService.sentenceDetection(text.trim());
            logger.info("Done detecting sentences. Length: "+ sentences.length + " ID: "+ sessionId);

            List<String> tokensAsList = null;

            List<String> validColor = new ArrayList<>();
            if(tokens != null && tokens.length > 0){
                tokensAsList = Arrays.asList(tokens);
                for(String token : tokensAsList){
                    boolean isValid = Color.isExisting(token);
                    if(isValid){ validColor.add(token); }
                }
            }

            //About to get fabric names
            List<String> materials = new ArrayList<>();
            List<FabricName> fabricNames = classificationService.getFabricsFromContent(text);
            if(fabricNames != null && !fabricNames.isEmpty()){
                for(FabricName fabricName : fabricNames){
                    materials.add(fabricName.getName());
                }
            }
            //end of getting fabric by name

            List<Categories> categoriesList = classificationService.getCategories();

            List<String> categories = null;
            List<String> attributes = null;

            if(categoriesList != null && !categoriesList.isEmpty()){
                categories = new ArrayList<>();
                attributes = new ArrayList<>();
                for(Categories category : categoriesList){
                    categories.add(category.getCategory());
                    attributes.addAll(category.getAttributes());
                }
            }

            List<Map> scoredTermsFromContent = new ArrayList<>();
            if(categories != null && !attributes.isEmpty()){
                List<String> intersect = classificationService.getIntersection(tokensAsList, attributes);
                if(intersect.isEmpty()) {
                    intersect = classificationService.getIntersection(tokensAsList, categories);
                }

                //About to compute for similarity between the given terms and existing attributes.
                if(intersect.isEmpty()){
                    if(tokensAsList != null && !tokensAsList.isEmpty()) {
                        List<Map> scoreSimilarity = new ArrayList<>();
                        for (String token : tokensAsList) {
                            TermSimilarityToList termSimilarityToList = new TermSimilarityToList(token, attributes);
                            Map<String, Object> highestScore = termSimilarityToList.highestScore();
                            scoreSimilarity.add(highestScore);
                        }

                        logger.info("error...");
                    }
                }



                if (!intersect.isEmpty()) {
                    List<TFIDFWeightedScore> tfIdfWeightedScores = new ArrayList<>();
                    for (String i : intersect) {
                        TFIDFWeightedScore tfidfWeightedScore =
                                classificationService.getTfIdfWeightedScore(tokens, i);
                        tfIdfWeightedScores.add(tfidfWeightedScore);
                    }

                    Collections.sort(tfIdfWeightedScores, TFIDFWeightedScore.tfidfWeightedScoreComparator);

                    if (!tfIdfWeightedScores.isEmpty()) {
                        List<Map> tfIdfWeightedScoresMap = new ArrayList<>();
                        for (TFIDFWeightedScore tfidfWeightedScore : tfIdfWeightedScores) {
                            tfIdfWeightedScoresMap.add(tfidfWeightedScore.toMap());
                        }
                        scoredTermsFromContent.addAll(tfIdfWeightedScoresMap);
                    }
                }
            }

            /**
             * The method is to help determine whether a given content is gender specific or neutral. If former is
             * is found it should be surfaced and otherwise that should be surfaced as well.
             */
            logger.info("About to get gender for a given URL. ID: "+ sessionId);
            Map<String, Object> gender = classificationService.getGender(sentences, null, null);
            logger.info("Done getting gender. Gender: "+ gender.toString() + "ID: "+ sessionId);
            //end of get gender.

            Set<String> terms = new HashSet<>();
            if(!scoredTermsFromContent.isEmpty()){
                for(Map scoredMap : scoredTermsFromContent){
                    terms.add(scoredMap.get("term").toString());
                }
            }

            List<ResponseCategoryToAttribute> responseCategoryToAttributes = new ArrayList<>();
            if(!terms.isEmpty()){
                for(String term : terms){
                    String stemmedTerm = classificationService.getStem(term);
                    term = StringUtils.isNotBlank(stemmedTerm) ? stemmedTerm : term;
                    ResponseCategoryToAttribute responseCategoryToAttribute =
                            new ResponseCategoryToAttribute();
                    responseCategoryToAttribute.setCategory(classificationService
                            .getCategoryByTerm(English.plural(term, 1)));
                    responseCategoryToAttribute.setPricing(null);
                    responseCategoryToAttribute.setSizes(null);
                    responseCategoryToAttribute.setMaterials(materials);
                    responseCategoryToAttribute.setBrand(null);
                    responseCategoryToAttribute.setColors(validColor);
                    responseCategoryToAttribute.setIsLuxury(null);

                    List<String> attributesList = new ArrayList<>();
                    attributesList.add(English.plural(term, 1));

                    responseCategoryToAttribute.setAttributes(attributesList);

                    if(!gender.isEmpty()) {
                        if(gender.containsKey("gender")) {
                            responseCategoryToAttribute.setGender((Map) gender.get("gender"));
                        }
                    }

                    responseCategoryToAttributes.add(responseCategoryToAttribute);
                }
            }

            if(terms.isEmpty()){
                ResponseCategoryToAttribute responseCategoryToAttribute =
                        new ResponseCategoryToAttribute();

                List<String> attributesList = new ArrayList<>();
                responseCategoryToAttribute.setAttributes(attributesList);

                responseCategoryToAttribute.setPricing(null);
                responseCategoryToAttribute.setSizes(null);
                responseCategoryToAttribute.setMaterials(materials);
                responseCategoryToAttribute.setBrand(null);
                responseCategoryToAttribute.setColors(validColor);
                responseCategoryToAttribute.setIsLuxury(null);

                if(!gender.isEmpty()) {
                    if(gender.containsKey("gender")) {
                        responseCategoryToAttribute.setGender((Map) gender.get("gender"));
                    }
                }

                responseCategoryToAttributes.add(responseCategoryToAttribute);
            }

            response.put(ResponseMap.CLASSIFICATION.toString(), responseCategoryToAttributes);
        }

        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/tags", method = RequestMethod.GET)
    public ModelAndView generateTags(@RequestParam(required = true) String text) {
        logger.info("Request for custom tags using parameter: " + text);
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }

    @RequestMapping(value = "/tags", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getTags(){
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String sessionId = attr.getSessionId();
        Map<String, Object> response = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String outputString = null;
        try {
            outputString = objectMapper.writeValueAsString(response);
        } catch (Exception e){
            logger.debug("Error in parsing response as string. Message: "+ e.getMessage() +" ID: "+ sessionId);
        }
        return outputString;
    }

    @RequestMapping(value = "/v1/learning", method = RequestMethod.GET, produces = "application/json")
    public ModelAndView getExternalData(@RequestParam(required = true) String query) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            LearningImpl learning = LearningImpl.setQuery(query);
            String answer = learning.find();
            if(StringUtils.isNotBlank(answer)) {
                response.put(LearningResponseKeys.DESCRIPTIONS.toString(), answer);
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/url", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String generateTagsByUrl(@RequestParam(required = true, name = "url") String url,
                                    @RequestParam(required = false, name = "showScore", defaultValue = "false")
                                            boolean showScore)  {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String sessionId = attr.getSessionId();

        logger.info("About to decode URL parameter. URL "+ url);
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (Exception e){
            logger.debug("Error in decoding URL parameter. URL: "+ url);
        }
        logger.info("Done decoding URL parameter. Updated URL: "+ url);

        logger.info("About to check whether to use proxy request. ID: "+ sessionId);
        boolean useProxy = requestProxy.isEnable();
        logger.info("Done use proxy :"+ useProxy + " ID:"+ sessionId);

        logger.info("About to get proxy url if use isEnabled. ID: "+ sessionId);
        String proxyUrl = null;
        if(useProxy){
            proxyUrl = requestProxy.getProxyUrl();
        }
        logger.info("Done: Proxy URL: "+ proxyUrl + " ID: "+ sessionId);

        logger.info("About to process request for URL: " + url + " ID: " + sessionId);
        Map<String, Object> response = new HashMap<>();
        if (StringUtils.isNotBlank(url)) {

            //Get domain name from url
            logger.info("About to perform domain name retrieval. URL: "+ url + " ID: "+ sessionId);
            String domain = classificationService.getDomainName(url);
            logger.info("Done. Domain name: "+ domain);
            //end of getting domain from url.

            logger.info("About to retrieve content string from third party provider.");
            boolean isDomainAProvider = false;
            if(StringUtils.isNotBlank(domain)){
                isDomainAProvider = thirdPartyProviderService.isDomainAProvider(domain);
            }
            logger.info("Done getting content string from third party provider. Results: "+ isDomainAProvider);

            logger.info("About to make request to third party provider.");
            String thirdPartyProviderDescription = null;
            if(isDomainAProvider){
                thirdPartyProviderDescription = thirdPartyProviderService.getItemDescription(domain, url);
            }
            if(StringUtils.isNotBlank(thirdPartyProviderDescription)) {
                logger.info("Done getting third party provider. Item details: " + thirdPartyProviderDescription);
            } else {
                logger.info("Done getting third party provider. Item details: None.");
            }

            logger.info("About to get content string for URL. ID: "+ sessionId);
            String contentString = jsoupService.getContentAsString(url);
            if(StringUtils.isNotBlank(contentString)) {
                logger.info("Content string  retrieved : Length : " + contentString.length());
            } else {
                logger.info("No content string  retrieved : Length : 0");
            }

            logger.info("About to generate document object out of content string. ID: "+ sessionId);
            Document document = null;
            if(StringUtils.isNotBlank(contentString)) {
                document = jsoupService.getDocumentByParser(contentString);
            }
            logger.info("Done generating document out of content string. ID: "+ sessionId);

            logger.info("About to get title from document. ID: "+ sessionId);
            String title = null;
            if(document != null) {
                title = jsoupService.getTitleByDocument(document);
            }
            logger.info("Done getting title from document. ID: "+ sessionId);

            logger.info("About to get metas from document. URL: "+ url + " ID:"+ sessionId);
            List<String> metaList = null;
            if(document != null) {
                metaList = jsoupService.metasByDocument(document);
            }
            logger.info("Done getting metas from document. URL: "+ url + " ID:" + sessionId);

            List<String> titleTokens = new ArrayList<>();
            logger.info("About to get tokens of title from document.");
            if (StringUtils.isNotBlank(title)) {
                titleTokens.addAll(classificationService
                        .prepareTokens(Arrays.asList(classificationService.tokenize(title))));
            } else { logger.info("Done empty title from document from url. ID: "+ sessionId); }

            logger.info("About to get all links or URLs found in document. ID: "+ sessionId);
            List<Map> linksMap = null;
            if(document != null) {
                linksMap = jsoupService.getLinksUrlAndValueByDocument(document);
            }
            if(linksMap != null) {
                logger.info("Done. found " + linksMap.size() + " urls in document. ID: " + sessionId);
            } else {
                logger.info("Done. none found 0 urls in document. ID: " + sessionId);
            }

            logger.info("About to extract text from document. URL :"+ url + "ID: "+ sessionId);
            String text = null;
            if(StringUtils.isNotBlank(contentString) && !isDomainAProvider) {
                text = jsoupService
                        .parseHtmlText(classificationService.removeNavigationAndMenuBars(
                                classificationService.removePossibleImagesFromText(
                                        classificationService.removePossibleInputFieldFromText(
                                                classificationService.removePossibleUrlFromText(linksMap, contentString)))), url);
            }

            if(StringUtils.isNotBlank(thirdPartyProviderDescription) && isDomainAProvider){
                logger.info("About to use description gotten from a third party provider.");
                text = thirdPartyProviderDescription;
                contentString = thirdPartyProviderDescription;
                logger.info("Done third party description. Description: "+ text);
            }
            logger.info("Done getting text extraction from document. URL: "+ url + " ID: "+ sessionId);

            if (StringUtils.isNotBlank(text)) {
                logger.info("Text for analysis ready. Length: "+ text.length() + " ID: "+ sessionId);
                /**
                 * The start of getting potential colors.
                 */
                logger.info("About to get potential colors by regex. ID: "+ sessionId);
                List<String> potentialColor = AppUtils.getColorByRegEx(text);
                List<String> itemColors = new ArrayList<>();
                //Second step is to get available colors from input fields;
                List<String> colorsFromInputFields = classificationService.colorsFromSelectFields(contentString);
                if(!colorsFromInputFields.isEmpty()){
                    potentialColor.addAll(colorsFromInputFields);
                }
                logger.info("Done getting potential colors. "+ potentialColor.toString() + " ID: "+ sessionId);

                if (!potentialColor.isEmpty()) {
                    List<String> getColorsFromRegExObj = AppUtils.getColorsFromRegEx(potentialColor);
                    Map<String, Object> colors = new HashMap<>();
                    colors.put("colors", getColorsFromRegExObj);

                    itemColors.addAll(getColorsFromRegExObj);

                    List<Map<String, Object>> colorsValidation = new ArrayList<>();
                    if (!colors.isEmpty()) {
                        for (String s : getColorsFromRegExObj) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", s);
                            map.put("isValidated", Color.isExisting(s.trim().toLowerCase()));
                            colorsValidation.add(map);
                        }
                    }

                    if(showScore) {
                        colors.put("colorsValidation", colorsValidation);
                        response.putAll(colors);
                    }

                    boolean displayResults = wordNetDictConfig.getDisplayResultsBool();
                    if (displayResults) {
                        List<Map> definitions = new ArrayList<>();
                        if (!getColorsFromRegExObj.isEmpty()) {
                            for (String s : getColorsFromRegExObj) {
                                Map<String, Object> map = new HashMap<>();
//                                 List<Map> m = wordNetService.getResponse(s);
//                                 map.put(s, m);
//                                 List<Map> m = wordNetService.findStemmers(s);
//                                 map.put(s, m);
                                List<Map> m = wordNetService.glosses(s);
                                map.put(s, m);
                                definitions.add(map);
                            }
                        }
                        if(showScore) {
                            response.put("definitions", definitions);
                        }
                    }

                    //Learning of non-validated colors and updating outgoing colors after verification.
                    if(!colorsValidation.isEmpty()){
                        List<String> validatedColors = classificationService.colorsVerified(colorsValidation);
                        List<String> verifiedColors = classificationService.colorsVerification(colorsValidation);

                        if(!verifiedColors.isEmpty() && !itemColors.isEmpty()) {
                            List<String> intersectionOfVerifiedWithColors =
                                    classificationService.getIntersection(verifiedColors, itemColors);
                            if (!intersectionOfVerifiedWithColors.isEmpty()) {
                                itemColors.clear();
                                itemColors.addAll(intersectionOfVerifiedWithColors);
                                itemColors.addAll(validatedColors);
                            } else {
                                itemColors.clear();
                                itemColors.addAll(verifiedColors);
                                itemColors.addAll(validatedColors);
                            }
                        } else {
                            itemColors.clear();
                            itemColors.addAll(validatedColors);
                        }
                    }
                }
                //End of getting potential colors.


                logger.info("About to get sizes from url. ID: "+ sessionId);
                //Trying to get size of the item if exist, ideal for shoes and clothing
                List<String> sizesFromContent = classificationService.sizeFromSelectFields(contentString);
                if(showScore) {
                    response.put("availableSizes", sizesFromContent);
                }
                //End of getting size from content.
                logger.info("Done getting sizes for url. Size: "+ sizesFromContent.toString() + " ID: "+ sessionId);

                logger.info("About to get fabric name from URL ID: "+ sessionId);
                //Get potential material make of the said item.
                List<FabricName> fabricNames = classificationService.getFabricsFromContent(text);
                List<String> materialsFound = new ArrayList<>();
                if(!fabricNames.isEmpty()){
                    for(FabricName fabricName : fabricNames){
                        materialsFound.add(fabricName.getName().trim().toLowerCase());
                    }
                }
                //End of potential material of make.
                logger.info("Done getting fabric names from URL. Fabrics: "+ fabricNames.toString() + " ID: "+ sessionId);

                //Start of content analysis of content page.
                logger.info("About to tokenize content . ID: "+ sessionId);
                String[] tokens = classificationService.tokenize(text.toLowerCase().trim());
                logger.info("Done generating tokens. Length: "+ tokens.length + " ID: "+ sessionId);

                logger.info("About to detect sentences.");
                String[] sentences = classificationService.sentenceDetection(text.toLowerCase().trim());
                logger.info("Done detecting sentences. Length: "+ sentences.length + " ID: "+ sessionId);

                String article = null;
                if (sentences != null && sentences.length > 0) {
                    article = classificationService.getSentencesAsString(sentences);
                }

                List<Categories> categoriesList = classificationService.getCategories();
                List<String> multiWordAttributes = new ArrayList<>();
                List<String> allAttributes = null;
                List<String> allCategories = null;

                if (categoriesList != null && !categoriesList.isEmpty()) {
                    allAttributes = new ArrayList<>();
                    allCategories = new ArrayList<>();
                    for (Categories c : categoriesList) {
                        allCategories.add(c.getCategory());
                        allAttributes.addAll(c.getAttributes());
                        multiWordAttributes.addAll(classificationService.getMultiWordedAttributes(c));
                    }
                }

                List<String> foundInSentences = new ArrayList<>();
                if (!multiWordAttributes.isEmpty()) {
                    for (String s : multiWordAttributes) {
                        boolean termFound = classificationService.termFoundInSentences(sentences, s);
                        if (termFound) {
                            foundInSentences.add(s);
                        }
                    }
                }

                //Get possible title or item description from first list
                String possibleTitle = classificationService.getPossibleTitle(sentences);
                //End of getting a possible title.

                //Addition analysis of title and content meta data
                List<String> keywordsList = null;
                List<String> descriptionList = null;

                String description = null;
                String keywords = null;

                List<Map> metaKeyValuePair = null;
                if(metaList != null) {
                    metaKeyValuePair = classificationService.generateKeyValuePairs(metaList);
                    if (!metaKeyValuePair.isEmpty()) {
                        description = classificationService
                                .getContentMetaDataValue(NameAndContentMetaData.NAME,
                                        metaKeyValuePair, WebMetaName.DESCRIPTION);
                        keywords = classificationService
                                .getContentMetaDataValue(NameAndContentMetaData.NAME,
                                        metaKeyValuePair, WebMetaName.KEYWORDS);

                        if (StringUtils.isNotBlank(keywords)) {
                            keywordsList = classificationService
                                    .prepareTokens(Arrays.asList(classificationService.tokenize(keywords)));
                        }

                        if (StringUtils.isNotBlank(description)) {
                            descriptionList = classificationService
                                    .prepareTokens(Arrays.asList(classificationService.tokenize(description)));
                        }
                    }
                }
                //end of content meta data

                /**
                 * About to execute the method below to retrieve price of the said item.
                 */
                Map<String, Object> priceMap = null;
                if(StringUtils.isNotBlank(contentString) && metaKeyValuePair != null) {
                    priceMap = classificationService.getPrice(contentString);
                    if (priceMap != null && !priceMap.isEmpty()) {
                        //response.put("price", priceMap);
                    }
                }
                //end of get price

                /**
                 * About to retrieve brand of a given url or item.
                 */
                String brand = classificationService.getBrand(contentString, possibleTitle);
                //end of retrieving brand of a given url.

                //Sentences from content, description and keywords from meta all merged into a holistic data set.
                List<String> doubleWordedFoundInContent = new ArrayList<>();
                if (!multiWordAttributes.isEmpty()) {
                    for (String s : multiWordAttributes) {
                        boolean isPresent = classificationService.termFoundInSentences(sentences, s);
                        if (isPresent) {
                            doubleWordedFoundInContent.add(s);
                        }
                    }
                }
                //end of getting content from description and keywords


                List<Map> posList = null;
                List<Map> scoredTermsFromContent = new ArrayList<>();
                List<TotalTermToGroup> totalTermToGroups = new ArrayList<>();

                if (tokens != null && tokens.length > 0) {
                    List<String> tokensAsList = classificationService.prepareTokens(Arrays.asList(tokens));

                    //Added keywords and description from meta data to the list to used in computing TF-IDF
                    if (keywordsList != null && !keywordsList.isEmpty()) {
                        tokensAsList.addAll(keywordsList);
                    }
                    if (descriptionList != null && !descriptionList.isEmpty()) {
                        tokensAsList.addAll(descriptionList);
                    }
                    if (possibleTitle != null) {
                        tokensAsList.addAll(classificationService
                                .prepareTokens(Arrays.asList(classificationService.tokenize(possibleTitle))));
                    }
                    //End of keywords and description to token list.


                    List<String> intersect = classificationService.getIntersection(tokensAsList, allAttributes);

                    if(intersect.isEmpty()) {
                        intersect = classificationService.getIntersection(tokensAsList, allCategories);
                    }

                    if (intersect != null && !intersect.isEmpty()) {
                        List<TFIDFWeightedScore> tfIdfWeightedScores = new ArrayList<>();
                        for (String i : intersect) {
                            TFIDFWeightedScore tfidfWeightedScore =
                                    classificationService.getTfIdfWeightedScore(tokens, i);
                            tfIdfWeightedScores.add(tfidfWeightedScore);
                        }

                        //Include multi-worded from data set to TF_IDF Weighted score
                        if (!foundInSentences.isEmpty()) {
                            for (String s : foundInSentences) {
                                TFIDFWeightedScore tfidfWeightedScore = new TFIDFWeightedScore();
                                tfidfWeightedScore.setTerm(s);
                                tfidfWeightedScore.setIdfScore(0D);
                                tfidfWeightedScore.setTfScore(0D);
                                tfidfWeightedScore.setScore(0D);
                                tfIdfWeightedScores.add(tfidfWeightedScore);
                            }
                        }

                        Collections.sort(tfIdfWeightedScores, TFIDFWeightedScore.tfidfWeightedScoreComparator);

                        if (!tfIdfWeightedScores.isEmpty()) {
                            List<Map> tfIdfWeightedScoresMap = new ArrayList<>();
                            for (TFIDFWeightedScore tfidfWeightedScore : tfIdfWeightedScores) {
                                tfIdfWeightedScoresMap.add(tfidfWeightedScore.toMap());
                            }
                            scoredTermsFromContent.addAll(tfIdfWeightedScoresMap);
                        }
                    }
                    posList = classificationService.getPos(tokens);
                }
                //end of content analysis...

                //Aggregate keywords and merge with double worded description found.
                List<String> allKeywords = new LinkedList<>();
                if (!scoredTermsFromContent.isEmpty()) {
                    for (Map mapScore : scoredTermsFromContent) {
                        for (Object keySet : mapScore.keySet()) {
                            if (keySet instanceof String) {
                                if (((String) keySet).equalsIgnoreCase("term")) {
                                    allKeywords.add(mapScore.get(keySet).toString());
                                }
                            }
                        }
                    }
                }

                if (!doubleWordedFoundInContent.isEmpty()) {
                    for (String s : doubleWordedFoundInContent) {
                        allKeywords.add(s);
                    }
                }

                if(showScore) {
                    response.put("allKeywords", allKeywords);
                }


                /**
                 * About to build a frequency computation on all keywords in respect to where in the document
                 * they are found.
                 */
                if (!allKeywords.isEmpty()) {
                    List<TermToGroupScore> groupScoreList = new ArrayList<>();
                    List<ContentAreaGroupings> cList = ContentAreaGroupings.contentAreaGroupingsList();

                    for (ContentAreaGroupings c : cList) {
                        for (String s : allKeywords) {
                            TermToGroupScore termToGroupScore = new TermToGroupScore();
                            termToGroupScore.setGroup(c);
                            termToGroupScore.setTerm(s);

                            switch (c) {
                                case BODY:
                                    termToGroupScore.setScore(1);
                                    break;
                                case TITLE:
                                    if (StringUtils.isNotBlank(possibleTitle)) {
                                        Integer tScore = classificationService.getTermToGroupScore(s, possibleTitle);
                                        termToGroupScore.setScore(tScore);
                                    } else {
                                        termToGroupScore.setScore(0);
                                    }
                                    break;
                                case DESCRIPTION:
                                    if (StringUtils.isNotBlank(description)) {
                                        Integer tDesc = classificationService.getTermToGroupScore(s, description);
                                        termToGroupScore.setScore(tDesc);
                                    } else {
                                        termToGroupScore.setScore(0);
                                    }
                                    break;
                                case KEYWORDS:
                                    if (StringUtils.isNotBlank(keywords)) {
                                        Integer tKeywords = classificationService.getTermToGroupScore(s, keywords);
                                        termToGroupScore.setScore(tKeywords);
                                    } else {
                                        termToGroupScore.setScore(0);
                                    }
                                    break;
                                default:
                                    termToGroupScore.setScore(0);
                                    break;
                            }
                            groupScoreList.add(termToGroupScore);
                        }
                    }

                    if (!groupScoreList.isEmpty()) {
                        Map<String, List<TermToGroupScore>> m = new HashMap<>();
                        for (ContentAreaGroupings c : cList) {
                            List<TermToGroupScore> t = classificationService
                                    .getTermToGroupByContentAreaGroupings(groupScoreList, c);
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
                                            Integer x = i + t.getScore();
                                            totalTermToGroup.put(t.getTerm(), x);
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
                    }
                }

                //Present total scoring on all terms found.
                if (!totalTermToGroups.isEmpty()) {
                    Collections.sort(totalTermToGroups, TotalTermToGroup.totalTermToGroupComparator);

                    if(showScore) {
                        response.put("totalWeightedScore", totalTermToGroups);
                    }
                }

                //Scored terms crossed referenced with sentences to predict the order
                if(!totalTermToGroups.isEmpty()){
                    for(TotalTermToGroup t : totalTermToGroups){
                        String[] sentenceToTerm = classificationService.getSentencesWithTerm(sentences, t.getTerm());
                    }
                }
                //end of sentences crossed referenced.

                //Filter total term to group score by scoring threshold.
                Double termScoringThreshold = classificationService.getTermScoringThreshold();
                List<TotalTermToGroup> totalTermToGroupsFiltered = new ArrayList<>();
                if (!totalTermToGroups.isEmpty()) {
                    for (TotalTermToGroup t : totalTermToGroups) {
                        if (t.getWeightTotalScore() >= termScoringThreshold) {
                            totalTermToGroupsFiltered.add(t);
                        }
                    }
                }

                /**
                 * The method is to help determine whether a given content is gender specific or neutral. If former is
                 * is found it should be surfaced and otherwise that should be surfaced as well.
                 */
                logger.info("About to get gender for a given URL. ID: "+ sessionId);
                Map<String, Object> gender = classificationService.getGender(sentences, keywords, description);
                if(!gender.isEmpty()){
                    if(showScore) {
                        response.putAll(gender);
                    }
                }
                logger.info("Done getting gender. Gender: "+ gender.toString() + "ID: "+ sessionId);
                //end of get gender.

                //Get the top level category that an attribute belongs to and score 'em
                if(!totalTermToGroupsFiltered.isEmpty()){
                    Set<String> terms = new HashSet<>();
                    for(TotalTermToGroup totalTermToGroup : totalTermToGroupsFiltered){
                        String singular = English.plural(totalTermToGroup.getTerm(), 1);
                        terms.add(singular);
                    }

                    List<ResponseCategoryToAttribute> responseCategoryToAttributeList = new ArrayList<>();
                    if(!terms.isEmpty()){
                        for(String s : terms){
                            ResponseCategoryToAttribute responseCategoryToAttribute =
                                    new ResponseCategoryToAttribute();
                            responseCategoryToAttribute.setCategory(classificationService.getCategoryByTerm(s));
                            List<String> attributes = new ArrayList<>();
                            attributes.add(English.plural(s, 1));
                            responseCategoryToAttribute.setAttributes(attributes);
                            responseCategoryToAttribute.setColors(itemColors);

                            if(!gender.isEmpty()) {
                                if(gender.containsKey("gender")) {
                                    responseCategoryToAttribute.setGender(gender);
                                }
                            }

                            if(!materialsFound.isEmpty()){
                                responseCategoryToAttribute.setMaterials(materialsFound);
                            }

                            if(!sizesFromContent.isEmpty()){
                                responseCategoryToAttribute.setSizes(sizesFromContent);
                            }

                            if(priceMap != null && !priceMap.isEmpty()){
                                responseCategoryToAttribute.setPricing(priceMap);
                            }

                            if(StringUtils.isNotBlank(brand)){
                                responseCategoryToAttribute.setBrand(brand);
                            }

                            responseCategoryToAttributeList.add(responseCategoryToAttribute);
                        }
                    }

                    /**
                        Run response to category against combination matrix, only if the list of response to category
                        is more than one.
                     */
                    logger.info("About to get combination matrix. Using Response Category to attributes list. "+
                            responseCategoryToAttributeList.toString() + " ID:"+ sessionId);
                    List<ResponseCategoryToAttribute> updated = null;
                    if(responseCategoryToAttributeList.size() > 1){
                        updated = classificationService
                                .getCombinedMatrix(responseCategoryToAttributeList);
                        if(!updated.isEmpty()) {
                            if(showScore) {
                                response.put("combinedDecision", updated);
                            }
//                            Set<ResponseCategoryToAttribute> set = new HashSet<>();
//                            responseCategoryToAttributeList.addAll(updated);
//                            set.addAll(responseCategoryToAttributeList);
//
//                            responseCategoryToAttributeList.clear();
//                            responseCategoryToAttributeList.addAll(set);
                        }
                    }
                    if(updated != null) {
                        logger.info("Done with getting combination matrix. Combined response to category : "
                                + updated.toString() + " ID: "+ sessionId);
                    } else {
                        logger.info("Done with getting combination matrix. Combined response to category : None. ID:"
                                + sessionId);
                    }

                    /**
                     * Merge all response to categories which share the same category.
                     */
                    logger.info("About to merge responses based on categories. ID: "+ sessionId);
                    List<ResponseCategoryToAttribute> mergeResponseToCategories =
                            classificationService.groupResponseByCategory(responseCategoryToAttributeList);
                    if(!mergeResponseToCategories.isEmpty()){
                        for(ResponseCategoryToAttribute r : mergeResponseToCategories){
                            List<String> attributes = r.getAttributes();
                            for(String attribute : attributes) {
                                String[] sentenceInAttribute = classificationService
                                        .getSentencesWithTerm(sentences, attribute);
                            }
                        }
                        if(showScore) {
                            response.put("mergedResponseCategoryToAttributes", mergeResponseToCategories);
                        }
                    }
                    logger.info("Done merging responses by categories. Response to category: "
                            + mergeResponseToCategories.toString() + " ID:"+ sessionId);

                    logger.info("About to get response matrix threshold. ID: "+ sessionId);
                    Integer responseMatrixThreshold = Integer.parseInt(classificationConfig.getResponseMatrixThreshold());
                    logger.info("Done getting response matrix threshold. Value: "+ responseMatrixThreshold + " ID: "+ sessionId);

                    logger.info("About to merge response to category based on rules engine data set. ID: "+ sessionId);
                    if(!mergeResponseToCategories.isEmpty() &&
                            mergeResponseToCategories.size() > responseMatrixThreshold){
                        RulesEngineDataSet rulesEngineDataSet = new RulesEngineDataSet();
                        rulesEngineDataSet.setTitle(possibleTitle);
                        rulesEngineDataSet.setBody(text);

                        if(metaKeyValuePair != null) {
                            rulesEngineDataSet.setMetas(metaKeyValuePair);
                        }

                        //Added response to attributes result from combined decision to merged responses.
                        if(updated != null && !updated.isEmpty()){
                            mergeResponseToCategories.addAll(updated);
                        }

                        ResponseCategoryToAttribute responseCategoryToAttribute =
                                classificationService.refineResultSet(mergeResponseToCategories, rulesEngineDataSet);

                        if(responseCategoryToAttribute != null) {
                            logger.info("Response to category based on rules engine. Results: "+
                                    responseCategoryToAttribute.toString() + " ID: "+ sessionId);

                            response.put(ResponseMap.CLASSIFICATION.toString(),
                                    responseCategoryToAttribute.toResponseMap());
                        }
                        logger.info("Merged responses is greater than 1:"+ responseMatrixThreshold + " ID:"+ sessionId);
                    } else {
                        if(mergeResponseToCategories.size() == 1) {
                            response.put(ResponseMap.CLASSIFICATION.toString(), mergeResponseToCategories.get(0).toResponseMap());
                        }
                    }

                    if(showScore) {
                        response.put("responseCategoryToAttribute", responseCategoryToAttributeList);
                    }
                }

                if(showScore) {
                    response.put("scoreThreshold", totalTermToGroupsFiltered);
                }
            }
        } else {
            response.put(RestResponseKeys.MESSAGE.toString(), "empty or missing url. ID: "+ sessionId);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String outputString = null;
        try {
            outputString = objectMapper.writeValueAsString(response);
        } catch (Exception e){
            logger.debug("Error in parsing response as string. Message: "+ e.getMessage() +" ID: "+ sessionId);
        }

        return outputString;
    }

    @RequestMapping("/v1/parts-of-speech")
        public ModelAndView getPOSByTerms(@RequestParam(required = true) String query){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String[] tokens = classificationService.tokenize(query);
            if(tokens != null && tokens.length > 0) {
                List<Map> pos = classificationService.getPos(tokens);
                if(pos != null && !pos.isEmpty()){
                    response.put("parts-of-speech", pos);
                }
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping("/v2/parts-of-speech")
    public ModelAndView getPOSByTerms(@RequestParam(required = true) String query,
                                      @RequestParam(required = false) String pos,
                                      @RequestParam(required = false, defaultValue = "false") Boolean groupByPos){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String[] tokens = classificationService.tokenize(query);
            if(tokens != null && tokens.length > 0){
                List<Map> posResults = null;
                List<POSRESPONSES> posresponsesList = null;
                if(StringUtils.isNotBlank(pos)) {
                    List<String> requestPos = Arrays.asList(pos.split(","));
                    if (!requestPos.isEmpty()) {
                        posresponsesList = new ArrayList<>();
                        for(String selectedPos : requestPos){
                            POSRESPONSES posresponses = classificationService.getPOSRESPONSES(selectedPos);
                            if(posresponses != null){
                                posresponsesList.add(posresponses);
                            }
                        }

                        if(posresponsesList.isEmpty()){
                            posresponsesList = null;
                            String message = "Unsupported part-of-speech initial passed. Request : "+ pos;
                            List<POSRESPONSES> supportedPos = Arrays.asList(POSRESPONSES.values());
                            response.put("supportsPartsOfSpeech", supportedPos);
                            response.put("massage", message);
                        }
                    }
                } else {
                    String message = "Supported part-of-speech initial passed.";
                    List<POSRESPONSES> supportedPos = Arrays.asList(POSRESPONSES.values());
                    response.put("supportsPartsOfSpeech", supportedPos);
                    response.put("massage", message);
                }

                posResults = StringUtils.isNotBlank(pos) && posresponsesList != null ?
                        classificationService.getPos(tokens, posresponsesList) : classificationService.getPos(tokens);

                if(groupByPos && !posResults.isEmpty()){
                    List<Map> groupedPos = classificationService.groupByPos(posResults);
                    response.clear();
                    response.put("groupedByPos", groupedPos);
                    logger.info("Group by pos passed.");
                }

                if(!groupByPos) { response.put("parts-of-speech", posResults); }
            }
        } else {
            response.put("message", "Query parameter is missing or empty.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/stems", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView getStemmers(@RequestParam(required = true) String query){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String[] tokens = classificationService.tokenize(query);
            if(tokens != null && tokens.length > 0) {
                String[] correctedTokens = new String[tokens.length];
                int x = 0;

                for(String token  : tokens){
                    String corrected = spellCheckerService.getCorrectedLine(token);
                    if(StringUtils.isNotBlank(corrected)) {
                        correctedTokens[x] = corrected;
                    }
                    x++;
                }

                if(correctedTokens.length > 0) {
                    List<String> stems = classificationService.getStems(correctedTokens);
                    if (stems != null && !stems.isEmpty()) {
                        response.put("stems", stems);
                    }
                }
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping("/v1/meaning")
    public ModelAndView getLexicalMeaning(@RequestParam(name = "query", required = true) String query){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String[] tokens = classificationService.tokenize(query);
            List<Map> responseMap = null;
            if(tokens != null && tokens.length > 0) {
                int len = tokens.length;
                if(len == 1) {
                    responseMap = wordNetService.getResponse(query);
                }

                if(len > 1){
                    responseMap = new ArrayList<>();
                    for(String token : tokens) {
                        Map<String, Object> tokenMap = new HashMap<>();
                        tokenMap.put("query", token);
                        tokenMap.put("results", wordNetService.getResponse(token));
                        responseMap.add(tokenMap);
                    }
                }

                response.put("query", query);
                if (responseMap != null && !responseMap.isEmpty()) {
                    response.put("results", responseMap);
                }
            }
        } else {
            response.put("message", "Invalid or empty query passed.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping("/v1/spell/checker")
    public ModelAndView getSpellChecker(@RequestParam(name = "query", required = true) String query){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String line = spellCheckerService.getCorrectedLine(query);
            response.put("correctedLine", line);
        } else {
            response.put("message", "Invalid or empty query passed.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping("/v1/spell/suggestions")
    public ModelAndView getSpellSuggestions(@RequestParam(name = "query", required = true) String query,
                                            @RequestParam(name = "limit", required = false) Integer limit,
                                            @RequestParam(name = "includeWordForms", required = false,
                                                    defaultValue = "false")
                                            Boolean includeWordForms){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            Set<String> setTokens = new HashSet<>();
            String[] tokens = classificationService.tokenize(query, " ");

            if(tokens != null && tokens.length > 0){
                setTokens.addAll(Arrays.asList(tokens));
            }

            tokens = classificationService.tokenize(query);
            if(tokens != null && tokens.length > 0){
                setTokens.addAll(Arrays.asList(tokens));
            }

            tokens = setTokens.toArray(new String[setTokens.size()]);

            if(tokens.length > 0) {
                int len = tokens.length;
                if(len == 1) {
                    boolean isCorrect = spellCheckerService.isCorrect(query);
                    if (!isCorrect) {
                        List<String> suggestions = spellCheckerService.getSuggestions(query);

                        List<String> updatedSuggestions = new ArrayList<>();
                        List<String> wordForms = null;

                        if (suggestions != null && !suggestions.isEmpty()) {
                            updatedSuggestions.addAll(spellCheckerService.updateSuggestions(query, suggestions));
                            response.put("term", query);
                        } else {
                            response.put("message", "no suggestions.");
                        }

                        //Get word forms for query passed
                        if(!updatedSuggestions.isEmpty()) {
                            query = updatedSuggestions.get(0);
                            if(includeWordForms) {
                                List<Map> wordNetServiceResponse = wordNetService.getResponse(query);
                                if (wordNetServiceResponse != null && !wordNetServiceResponse.isEmpty()) {
                                    wordForms = new ArrayList<>();
                                    for (Map wordMap : wordNetServiceResponse) {
                                        if (wordMap.containsKey("wordForms")) {
                                            Object arrWordForms = wordMap.get("wordForms");
                                            if (arrWordForms instanceof String[]) {
                                                wordForms.addAll(Arrays.asList((String[]) arrWordForms));
                                            }
                                        }
                                    }
                                }
                            }

                            List<Map> searchResponse = wordNetService.search(query);
                            if (searchResponse != null && !searchResponse.isEmpty()) {
                                List<String> getStringsFromSearchResponse =
                                        HelperUtility.getValueOfMapAsList(searchResponse, "word");
                                List<String> searchResponsesUpdated =
                                        spellCheckerService.updateSuggestions(query, getStringsFromSearchResponse);

                                if (searchResponsesUpdated != null && !searchResponsesUpdated.isEmpty()) {
                                    updatedSuggestions.clear();
                                    updatedSuggestions.addAll(searchResponsesUpdated);
                                }

                            }
                        }

                        if(wordForms != null && !wordForms.isEmpty()){
                            response.put("wordForms", wordForms);
                        }
                        response.put("suggestion", updatedSuggestions);

                    } else {
                        List<String> suggestions = spellCheckerService.getSuggestions(query);
                        List<String> updatedSuggestions = new ArrayList<>();
                        List<String> wordForms = null;

                        if (suggestions != null && !suggestions.isEmpty()) {
                            updatedSuggestions.addAll(spellCheckerService.updateSuggestions(query, suggestions));
                            response.put("term", query);
                        }

                        //Get word forms from Lexical database based query passed
                        if(includeWordForms) {
                            List<Map> wordNetServiceResponse = wordNetService.getResponse(query);
                            if (wordNetServiceResponse != null && !wordNetServiceResponse.isEmpty()) {
                                wordForms = new ArrayList<>();
                                for (Map wordMap : wordNetServiceResponse) {
                                    if (wordMap.containsKey("wordForms")) {
                                        Object arrWordForms = wordMap.get("wordForms");
                                        if (arrWordForms instanceof String[]) {
                                            wordForms.addAll(Arrays.asList((String[]) arrWordForms));
                                        }
                                    }
                                }
                            }
                        }

                        List<Map> searchResponse = wordNetService.search(query);
                        if(searchResponse != null && !searchResponse.isEmpty()) {
                            List<String> getStringsFromSearchResponse =
                                    HelperUtility.getValueOfMapAsList(searchResponse, "word");
                            List<String> searchResponsesUpdated =
                                    spellCheckerService.updateSuggestions(query, getStringsFromSearchResponse);
                            if(searchResponsesUpdated != null && !searchResponsesUpdated.isEmpty()){
                                updatedSuggestions.clear();
                                updatedSuggestions.addAll(searchResponsesUpdated);
                            }
                        }

                        if(wordForms != null && !wordForms.isEmpty()){
                            response.put("wordForms", wordForms);
                        }
                        response.put("suggestion", updatedSuggestions);
                    }
                }

                //This part of the code is executed when the set of unique words is greater than 1.
                if(len > 1){
                    List<Map> suggestionsResult = new ArrayList<>();

                    List<String> filteredWords = new ArrayList<>();

                    for(String token : tokens){
                        Map<String, Object> updatedSuggestionsMap = new HashMap<>();
                        boolean isCorrect = spellCheckerService.isCorrect(token);
                        if (!isCorrect) {
                            List<String> suggestions = spellCheckerService.getSuggestions(token);
                            if (suggestions != null && !suggestions.isEmpty()) {
                                List<String> updatedSuggestions =
                                        spellCheckerService.updateSuggestions(token, suggestions);
                                updatedSuggestionsMap.put("term", token);
                                updatedSuggestionsMap.put("suggestion", updatedSuggestions);
                                filteredWords.addAll(updatedSuggestions);
                            }
                        } else {
                            List<String> suggestions = spellCheckerService.getSuggestions(token);
                            if (suggestions != null && !suggestions.isEmpty()) {
                                List<String> updatedSuggestions =
                                        spellCheckerService.updateSuggestions(token, suggestions);
                                updatedSuggestionsMap.put("term", token);
                                updatedSuggestionsMap.put("suggestion",updatedSuggestions);
                                filteredWords.addAll(updatedSuggestions);
                            } else {
                                updatedSuggestionsMap.put(token, "no suggestions.");
                            }
                        }
                        suggestionsResult.add(updatedSuggestionsMap);
                    }

                    //Check all query are sentence without tokenizing
                    String line = spellCheckerService.getCorrectedLine(query);
                    if(StringUtils.isNotBlank(line)){
                        Map<String, Object> sentenceMap = new HashMap<>();
                        sentenceMap.put("term", query);
                        sentenceMap.put("suggestion", line);
                        filteredWords.add(line);
                        suggestionsResult.add(sentenceMap);
                    }

                    if (!suggestionsResult.isEmpty()) {
                        response.put("suggestions", suggestionsResult);
                    }

                    if(!filteredWords.isEmpty()){
                        List<Map> similarityScoreMapList = new ArrayList<>();
                        for(String word : filteredWords){
                            Map<String, Object> similarityScoreMap = new HashMap<>();
                            double similarityScore = spellCheckerService.getSimilarityScore(query, word);
                            similarityScoreMap.put("term", word);
                            similarityScoreMap.put("score", similarityScore);
                            similarityScoreMapList.add(similarityScoreMap);
                        }

                        Collections.sort(similarityScoreMapList, new Comparator<Map>() {
                            @Override
                            public int compare(Map o1, Map o2) {
                                Double d1 = (Double) o1.get("score");
                                Double d2 = (Double) o2.get("score");
                                int answer = 0;
                                if (d1 >= d2) {
                                    answer = -1;
                                } else {
                                    answer = 1;
                                }
                                return answer;
                            }
                        });

                        filteredWords.clear();
                        for(Map wordsMap : similarityScoreMapList){
                            if(wordsMap.containsKey("term")){
                                filteredWords.add(wordsMap.get("term").toString());
                            }
                        }

                        if(!filteredWords.isEmpty()) {
                            response.clear();
                            response.put("term", query);
                            response.put("suggestion", filteredWords);
                        }
                    }
                }
            }
        } else {
            response.put("message", "Invalid or empty query passed.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping("/v2/spell/suggestions")
    public ModelAndView getSpellAndSentenceSuggestions(@RequestParam(name = "query", required = true) String query,
                                                       @RequestParam(name = "includeSentence",
                                                               required = false, defaultValue = "false")
                                                       Boolean suggestSentence){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            boolean isCorrect = spellCheckerService.isCorrect(query);
            if(!isCorrect){
                List<String> suggestions = spellCheckerService.getSuggestions(query);
                if(suggestions != null && !suggestions.isEmpty()){
                    List<String> updatedSuggestions = spellCheckerService.updateSuggestions(query, suggestions);
                    response.put("suggestions", updatedSuggestions);
                } else {
                    response.put("message", "no suggestions.");
                }

                if(suggestSentence){

                }
            } else {
                List<String> suggestions = spellCheckerService.getSuggestions(query);
                if(suggestions != null && !suggestions.isEmpty()){
                    List<String> updatedSuggestions = spellCheckerService.updateSuggestions(query, suggestions);
                    response.put("suggestions", updatedSuggestions);
                }
            }
        } else {
            response.put("message", "Invalid or empty query passed.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping("/v1/sentence/suggestion")
    public ModelAndView getSentenceSuggestions(@RequestParam(name = "query", required = true) String query){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)) {
            String[] tokens = classificationService.tokenize(query);
            List<Map> wordNetResponse = null;
            if(tokens != null && tokens.length > 0){
                wordNetResponse = new ArrayList<>();
                int len = tokens.length;
                if( len == 1){
                    wordNetResponse = wordNetService.getResponse(tokens[0]);
                }

                if(len > 1) {
                    wordNetResponse = new ArrayList<>();
                    for (String token : tokens) {
                        Map<String, Object> tokenToLexicalMeaning = new HashMap<>();
                        tokenToLexicalMeaning.put(token, wordNetService.getResponse(token));
                        wordNetResponse.add(tokenToLexicalMeaning);
                    }
                }
            }

            Map<String, Object> tokenToWordForms = null;
            if(wordNetResponse != null && !wordNetResponse.isEmpty()){
                tokenToWordForms = new HashMap<>();
                for(Map wordNetMap : wordNetResponse) {
                    if (wordNetMap.containsKey("wordForms")) {

                    }
                }
                response.put("suggestions", wordNetResponse);
            }
        } else {
            response.put("message", "Invalid or empty query passed.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/text/keywords", method = RequestMethod.POST, produces = "application/json")
    public ModelAndView getKeywordsFromText(@RequestParam(name = "text", required = false) String text,
                                            @RequestBody(required = true) String body){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        body = StringUtils.isNotBlank(body) ? body : text;
        if(StringUtils.isNotBlank(body)){

            body = body.replace("\\n", " ")
                    .replace(")", "")
                    .replace("(","")
                    .replace("”", "")
                    .replace("“", "")
                    .replace("|", "")
                    .replace("/"," ")
                    .replace("\\", "")
                    .replace("[", "")
                    .replace("]", "");

            String[] tokens = classificationService.tokenize(body.toLowerCase().trim());

            //Get default language symbol to be used to remove symbols from tokens.
            Languages languages = Languages.EN;
            LanguageSymbols symbols = LanguageSymbols.loadLanguageSymbols(languages);
            if(symbols != null) {
                tokens = LanguageSymbols.removeSymbolsFromList(tokens, symbols);
            }

            Set<String> cleanUpTokens = new HashSet<>();
            cleanUpTokens.addAll(Arrays.asList(tokens));
            tokens = cleanUpTokens.toArray(new String[cleanUpTokens.size()]);

//            Set<String> tokenWithoutStem = new HashSet<>();
//            tokenWithoutStem.addAll(Arrays.asList(tokens));

            //Get all the stems of the tokens.
            List<String> stemTokens = classificationService.getStems(tokens);
            Collections.sort(stemTokens);

            tokens = stemTokens.toArray(new String[stemTokens.size()]);

//            Set<String> stemFoundTokens = new HashSet<>();
//            stemFoundTokens.addAll(Arrays.asList(tokens));
//            tokens = stemFoundTokens.toArray(new String[stemFoundTokens.size()]);

//            Set<String> diff = Sets.difference(tokenWithoutStem, stemFoundTokens);
//            logger.info("Diff");

            List<String> listTokens = Arrays.asList(tokens);
            if(!listTokens.isEmpty()){
                List<String> supportedTokens = new ArrayList<>();

                Set<String> adjectives = new HashSet<>();
                Set<String> nouns = new HashSet<>();
                Set<String> nnNouns = new HashSet<>();
                Set<String> verbs = new HashSet<>();
//                Set<String> conjunctions = new HashSet<>();

                List<Map> posMapList = classificationService.getPos(tokens);
                if(posMapList != null && !posMapList.isEmpty()) {
                    for (Map posMap: posMapList){
                        String token = null;
                        if(posMap.containsKey("token")){
                            token = posMap.get("token").toString();
                        }

                        if(posMap.containsKey("pos")){
                            String pos = posMap.get("pos").toString();
                            POSRESPONSES posresponses = POSRESPONSES.valueOf(pos);
                            if(posresponses != null) {
                                switch (posresponses){
                                    case JJ:
                                        adjectives.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case NN:
                                        nouns.add(token);
                                        nnNouns.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case NNS:
                                        nouns.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case NNP:
                                        nnNouns.add(token);
                                        nouns.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case NNPS:
                                        nouns.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case VB:
                                        verbs.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case VBD:
                                        verbs.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case VBG:
                                        verbs.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case VBN:
                                        verbs.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case VBP:
                                        verbs.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case VBZ:
                                        verbs.add(token);
                                        supportedTokens.add(token);
                                        break;
                                    case CC:
                                        //conjunctions.add(token);
                                        break;
                                }
                            }
                        }
                    }
                }

                //The adjective phrase can be placed before, or after, the noun or pronoun in the sentence.
                //Get visual content JJ+NN1530
                List<String> jjAndNN = new ArrayList<>();
                for(String adjective : adjectives){
                    for(String nnNoun : nouns){
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(adjective);
                        stringBuilder.append(" ");
                        stringBuilder.append(nnNoun);
                        jjAndNN.add(stringBuilder.toString());
                    }
                }


                //An adjective phrase is a group of words that describe a noun or pronoun in a sentence.
//                List<String> nnAndJJ = new ArrayList<>();
//                for(String nn : nouns){
//                    for(String adj : adjectives){
//                        StringBuilder stringBuilder = new StringBuilder();
//                        stringBuilder.append(nn);
//                        stringBuilder.append(" ");
//                        stringBuilder.append(adj);
//                        nnAndJJ.add(stringBuilder.toString());
//                    }
//                }

                //Potential names
                List<String> nnAndNN = new ArrayList<>();
                for(String nn : nnNouns){
                    for(String nn1 : nnNouns){
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(nn);
                        stringBuilder.append(" ");
                        stringBuilder.append(nn1);
                        nnAndNN.add(stringBuilder.toString());
                    }
                }

                //compute potential verb phrases from verbs and all the tokens.
//                List<String> vbPhrasesCombination = new ArrayList<>();
//                for(String vb : verbs){
//                    for(String vbAndToken : tokens){
//                        StringBuilder stringBuilder = new StringBuilder();
//                        stringBuilder.append(vb);
//                        stringBuilder.append(" ");
//                        stringBuilder.append(vbAndToken);
//                        vbPhrasesCombination.add(stringBuilder.toString());
//                    }
//                }


                //Compute potential noun phrases from nouns and all available tokens.
//                List<String> npPhrasesCombination = new ArrayList<>();
//                for(String noun : nouns){
//                    for(String token : tokens){
//                        StringBuilder stringBuilder = new StringBuilder();
//                        stringBuilder.append(noun);
//                        stringBuilder.append(" ");
//                        stringBuilder.append(token);
//                        npPhrasesCombination.add(stringBuilder.toString());
//                    }
//                }
//
//
                Set<String> adjectivalPhrases = new HashSet<>();
                if(!jjAndNN.isEmpty()){
                    for(String phrase : jjAndNN){
                        Pattern pattern = Pattern.compile(phrase);
                        Matcher matcher = pattern.matcher(body);
                        while (matcher.find()){
                            adjectivalPhrases.add(phrase);
                        }
                    }
                }
//
//                if(!nnAndJJ.isEmpty()){
//                    for(String phrase : nnAndJJ){
//                        Pattern pattern = Pattern.compile(phrase);
//                        Matcher matcher = pattern.matcher(body);
//                        while (matcher.find()){
//                            adjectivalPhrases.add(phrase);
//                        }
//                    }
//                }
//
                Set<String> nounPhrasesFound = new HashSet<>();
                if(!nnAndNN.isEmpty()){
                    for(String phrase : nnAndNN){
                        Pattern pattern = Pattern.compile(phrase);
                        Matcher matcher = pattern.matcher(body);
                        while (matcher.find()){
                            nounPhrasesFound.add(phrase);
                        }
                    }
                }


                //a verb with another word or words indicating tense, mood, or person.
//                Set<String> verbPhrases = new HashSet<>();
//                if(!vbPhrasesCombination.isEmpty()){
//                    for(String phrase : vbPhrasesCombination){
//                        Pattern pattern = Pattern.compile(phrase);
//                        Matcher matcher = pattern.matcher(body);
//                        while (matcher.find()){
//                            verbPhrases.add(phrase);
//                        }
//                    }
//                }

                //
//                Set<String> nounPhrases = new HashSet<>();
//                if(!npPhrasesCombination.isEmpty()){
//                    for(String phrase : npPhrasesCombination){
//                        Pattern pattern = Pattern.compile(phrase);
//                        Matcher matcher = pattern.matcher(body);
//                        while (matcher.find()){
//                            nounPhrases.add(phrase);
//                        }
//                    }
//                }

                Set<String> mergeAllPOSPhrases = new HashSet<>();
                mergeAllPOSPhrases.addAll(adjectivalPhrases);
                mergeAllPOSPhrases.addAll(nounPhrasesFound);
//                mergeAllPOSPhrases.addAll(verbPhrases);
//                mergeAllPOSPhrases.addAll(nounPhrases);
//
                Set<String> uniqueTokens = new HashSet<>();
                if(!mergeAllPOSPhrases.isEmpty()){
                    for(String phrase : mergeAllPOSPhrases){
                        String[] phraseToToken = phrase.split(" ");
                        uniqueTokens.addAll(Arrays.asList(phraseToToken));
                    }
                }

                posMapList = classificationService.getPos(uniqueTokens.toArray(new String[uniqueTokens.size()]));

                //Clean out POS that are not need
                if(!posMapList.isEmpty()){
                    for(Map posMap : posMapList){
                        String token = null;
                        if(posMap.containsKey("token")){
                            token = posMap.get("token").toString();
                        }

                        if(posMap.containsKey("pos")) {
                            String pos = posMap.get("pos").toString();
                            POSRESPONSES posresponses = POSRESPONSES.valueOf(pos);
                            switch (posresponses){
                                case DT:
                                    uniqueTokens.remove(token);
                                    break;
                                case CD:
                                    uniqueTokens.remove(token);
                                    break;
                                case CC:
                                    uniqueTokens.remove(token);
                                    break;
                                case IN:
                                    uniqueTokens.remove(token);
                                    break;
                                case RB:
                                    uniqueTokens.remove(token);
                                    break;
                                case RBR:
                                    uniqueTokens.remove(token);
                                    break;
                                case RBS:
                                    uniqueTokens.remove(token);
                                    break;
                                case WRB:
                                    uniqueTokens.remove(token);
                                    break;
                                case PRP:
                                    uniqueTokens.remove(token);
                                    break;
                                case PRP$:
                                    uniqueTokens.remove(token);
                                    break;
                                case WP:
                                    uniqueTokens.remove(token);
                                    break;
                                case WP$:
                                    uniqueTokens.remove(token);
                                    break;
                                case VBZ:
                                    uniqueTokens.remove(token);
                                    break;
                                case VBN:
                                    uniqueTokens.remove(token);
                                    break;
                                case VBG:
                                    uniqueTokens.remove(token);
                                    break;
                                case VBD:
                                    uniqueTokens.remove(token);
                                    break;
                                case VBP:
                                    uniqueTokens.remove(token);
                                    break;
                                case TO:
                                    uniqueTokens.remove(token);
                                    break;
                                case VB:
                                    uniqueTokens.remove(token);
                                    break;
                                case SYM:
                                    uniqueTokens.remove(token);
                                    break;
                                case UH:
                                    uniqueTokens.remove(token);
                                    break;
                            }
                        }
                    }
                }


//                Set<String> visualContent = new HashSet<>();
//                Set<String> contentMarketing = new HashSet<>();
//
//                Map<String, List<POSRESPONSES>> tokenWithPOSTags = new HashMap<>();
//                Map<String, Object> tokenWithStems = new HashMap<>();
//                for(String uniqueToken : uniqueTokens){
//                    //List<Map> lexicalResponse = wordNetService.getResponse(uniqueToken);
//                    //List<Map> lexicalGlosses = wordNetService.glosses(uniqueToken);
//                    List<Map> stem = wordNetService.findStemmers(uniqueToken);
//                    List<POSRESPONSES> posresponsesList = new ArrayList<>();
//                    if(!stem.isEmpty()){
//                        for(Map<Integer, String> stemMap : stem){
//                            for(Map.Entry<Integer, String> entry : stemMap.entrySet()){
//                                POSRESPONSES posresponses = null;
//                                Integer posOrdinal = entry.getKey();
//                                POS posWordNet = POS.getPartOfSpeech(posOrdinal);
//                                switch (posWordNet) {
//                                    case NOUN:
//                                        posresponses = POSRESPONSES.NN;
//                                        break;
//                                    case VERB:
//                                        posresponses = POSRESPONSES.VB;
//                                        break;
//                                    case ADJECTIVE:
//                                        posresponses = POSRESPONSES.JJ;
//                                        break;
//                                    case ADVERB:
//                                        posresponses = POSRESPONSES.RB;
//                                        break;
//                                }
//                                posresponsesList.add(posresponses);
//                            }
//                        }
//                    }
//                    tokenWithPOSTags.put(uniqueToken, posresponsesList);
//                    tokenWithStems.put(uniqueToken, stem);
////                    for(String adjPhrase : adjectivalPhrases){
////                        Pattern pattern = Pattern.compile(uniqueToken);
////                        Matcher matcher = pattern.matcher(adjPhrase);
////                        while (matcher.find()){
////                            visualContent.add(uniqueToken);
////                        }
////                    }
////
////                    for(String nn : nounPhrases){
////                        Pattern pattern = Pattern.compile(uniqueToken);
////                        Matcher matcher = pattern.matcher(nn);
////                        while (matcher.find()){
////                            contentMarketing.add(nn);
////                        }
////                    }
//                }
//
//
//                if(!tokenWithPOSTags.isEmpty()){
//                    uniqueTokens.clear();
//                    for(Map.Entry<String, List<POSRESPONSES>> entry : tokenWithPOSTags.entrySet()){
//                        String uniqueToken = entry.getKey();
//
//                        if(entry.getValue().size() == 1) { uniqueTokens.add(uniqueToken);}
//
//                        if(entry.getValue().size() > 1) {
//                            boolean isPresent = false;
//                            for(String adjPhrase : adjectivalPhrases){
//                                Pattern pattern = Pattern.compile(uniqueToken);
//                                Matcher matcher = pattern.matcher(adjPhrase);
//                                while (matcher.find()){
//                                    isPresent = true;
//                                }
//                            }
//
//                            isPresent = false;
//                            for(String nounPhrase : nounPhrasesFound){
//                                Pattern pattern = Pattern.compile(uniqueToken);
//                                Matcher matcher = pattern.matcher(nounPhrase);
//                                while (matcher.find()){
//                                    isPresent = true;
//                                }
//                            }
//
//                            if(isPresent){ uniqueTokens.add(uniqueToken); }
//                        }
//                    }
//                }
//
//                logger.info("Lexical response: ");

                //Compute TF-IDF on merged phrases.
                List<TFIDFWeightedScore> weightedScoreList = new ArrayList<>();
                String[] posTokens = uniqueTokens.toArray(new String[uniqueTokens.size()]);

                for(String token : posTokens){
                    TFIDFWeightedScore tfidfWeightedScore = classificationService.getTfIdfWeightedScore(tokens, token);
                    weightedScoreList.add(tfidfWeightedScore);
                }

                Collections.sort(weightedScoreList, TFIDFWeightedScore.tfidfWeightedScoreComparator);

                List<String> keywords = new LinkedList<>();

                if(!weightedScoreList.isEmpty()){
                    for(TFIDFWeightedScore tfidfWeightedScore : weightedScoreList){
                        keywords.add(tfidfWeightedScore.getTerm());
                    }
                    //response.put("keywords", keywords);
                }

                //Get name and entities
                List<String> namesAndEntities = classificationService.getPersons(body);
                if(namesAndEntities != null && !namesAndEntities.isEmpty()){ keywords.addAll(namesAndEntities); }

                List<String> dates = classificationService.getTime(body);
                if(dates != null && !dates.isEmpty()){ keywords.addAll(dates); }

                List<String> locations = classificationService.getLocations(body);
                if(locations != null && !locations.isEmpty()){ keywords.addAll(locations); }

                List<String> organizations = classificationService.getOrganizations(body);
                if(organizations != null && !organizations.isEmpty()){ keywords.addAll(organizations); }

                List<String> monies = classificationService.getMoney(body);
                if(monies != null && !monies.isEmpty()){ keywords.addAll(monies); }

                List<String> percentages = classificationService.getPercentage(body);
                if(percentages != null && !percentages.isEmpty()){ keywords.addAll(percentages); }

                logger.info("Entities...");

//                if(!adjectivalPhrases.isEmpty()){
//                    keywords.addAll(adjectivalPhrases);
//                }

                response.put("keywords", keywords);
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }


    @RequestMapping(value = "/v1/testing", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public ModelAndView getMNB(HttpServletRequest request, @RequestBody String requestBody){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();

        String term = "navigating genesis";
        List<String> titles = new ArrayList<>();
        titles.add("The Bible: Scientific fact vs. blind Faith ( For Agnostics, Non-Theists, and those with an open Mind)");
        titles.add("NAVIGATING GENESIS STUDY GUIDE");
        titles.add("Navigating Genesis: A Scientist's Journey through Genesis 1-11 by Hugh Ross (2014-03-01)");
        titles.add("Why the Universe Is the Way It Is");
        titles.add("Navigating Genesis: A Scientist's Journey through Genesis 1-11");
        titles.add("ANDROID TV BOX Ghost Box Pro PLUS (V2) The ultimate android streaming media player WATCH ANYTHING | Kodi / Xbmc fully loaded and unlocked");
        titles.add("Power Acoustik PTID-8920B In-Dash DVD AM/FM Receiver with 7-Inch Flip-Out Touchscreen Monitor and USB/SD Input");

        String[] termTokens = classificationService.tokenize(term.toLowerCase().trim());
        List<String> termTokensAsList = Arrays.asList(termTokens);

        Set<String> setB = new HashSet<>();
        setB.addAll(termTokensAsList);
        termTokensAsList = new ArrayList<>();
        termTokensAsList.addAll(setB);


        List<String> titleAsTokens = new ArrayList<>();

        Map<String, Boolean> foundInText = new HashMap<>();

        List<Map> titleToScores = new LinkedList<>();

        if(!titles.isEmpty()){
            for(String title : titles){
                Map<String, Object> map = new HashMap<>();
                String[] titleToken = classificationService.tokenize(title.toLowerCase().trim());
                List<String> titleTokenList = Arrays.asList(titleToken);
                Set<String> setA = new HashSet<>();
                setA.addAll(titleTokenList);
                titleTokenList = new ArrayList<>();
                titleTokenList.addAll(setA);

                List<Map> termToTokenMap = new ArrayList<>();
                double total = 0;
                double presenceCounter = 0d;

                for(String t : termTokensAsList) {
                    Map<String, Object> m = new HashMap<>();
                    double weightedScore = classificationService.getTfIdfWeightScore(titleToken, t);
                    m.put("term", t);
                    m.put("weightedScore", weightedScore);
                    total += (Double.isNaN(weightedScore)) ? 0 : weightedScore;

                    Pattern pattern = Pattern.compile(t, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(title);
                    while (matcher.find()){
                        presenceCounter += 1;
                    }

                    termToTokenMap.add(m);
                }

                double presenceScore = (presenceCounter > 0) ? (presenceCounter/termTokensAsList.size()) : 0;

                map.put("presenceScore", presenceScore);
                map.put("scores", termToTokenMap);
                map.put("total", total);
                map.put("title", title);
                map.put("isPresent", (presenceScore > 0));

                titleToScores.add(map);
            }
        }

        //Numeric values
        Attribute attributeWeightedFrequencyScore = new Attribute("weightedFrequencyScore");

//        Attribute attributePresenceScore = new Attribute("presenceScore");
//
//        //Nominal values
//        FastVector nominalIsPresence = new FastVector();
//        nominalIsPresence.addElement(1);
//        nominalIsPresence.addElement(0);
//        nominalIsPresence.addElement("0.5");
//        Attribute attributeIsPresence = new Attribute("isPresenceAttributeLabel", nominalIsPresence);
//        logger.info("is nominal : "+ attributeIsPresence.isNominal());

        //Class attribute
        FastVector rankingClassValue = new FastVector(3);
        rankingClassValue.addElement("high");
        rankingClassValue.addElement("moderate");
        rankingClassValue.addElement("low");

        Attribute classAttribute = new Attribute("rankingClass", rankingClassValue);

        //Declare a vector attribute
        FastVector trainingVector = new FastVector(2);
        trainingVector.addElement(attributeWeightedFrequencyScore);
//        trainingVector.addElement(attributeIsPresence);
        //trainingVector.addElement(attributeIsPresence);
        trainingVector.addElement(classAttribute);


        Instances trainingInstances = new Instances("trainingRel", trainingVector, 10);
        trainingInstances.setClassIndex(trainingVector.size() - 1);

        Instance trainingInstance = new Instance(trainingVector.size());
        trainingInstance.setValue((Attribute) trainingVector.elementAt(0), 0.8);
//        trainingInstance.setValue((Attribute) trainingVector.elementAt(1),  1);
        //trainingInstance.setValue((Attribute) trainingVector.elementAt(2), "yes");
        trainingInstance.setValue((Attribute) trainingVector.elementAt(1), "high");


        Instance trainingInstance2 = new Instance(trainingVector.size());
        trainingInstance2.setValue((Attribute) trainingVector.elementAt(0), 0);
//        trainingInstance2.setValue((Attribute) trainingVector.elementAt(1), 0);
//        trainingInstance2.setValue((Attribute) trainingVector.elementAt(2), "no");
        trainingInstance2.setValue((Attribute) trainingVector.elementAt(1), "low");
//
        Instance trainingInstance3 = new Instance(trainingVector.size());
        trainingInstance3.setValue((Attribute) trainingVector.elementAt(0), 0.5);
//        trainingInstance3.setValue((Attribute) trainingVector.elementAt(1), 1);
//        trainingInstance3.setValue((Attribute) trainingVector.elementAt(2), "no");
        trainingInstance3.setValue((Attribute) trainingVector.elementAt(1), "moderate");

        trainingInstances.add(trainingInstance);
        trainingInstances.add(trainingInstance2);
        trainingInstances.add(trainingInstance3);


        Instances isTestingSet = new Instances("trainingData", trainingVector, 10);
        isTestingSet.setClassIndex(trainingVector.size() - 1);


        List<Instance> instanceList = null;
        if(!titleToScores.isEmpty()) {

            instanceList = new ArrayList<>();

            for(Map map : titleToScores) {
                Double total = 0D;
                boolean isPresent = false;

                if(map.containsKey("total")){
                    Object object = map.get("total");
                    if(object instanceof Double) {
                        total = (Double) object;
                    }
                }

                if(map.containsKey("isPresent")){
                    Object object = map.get("isPresent");
                    if(object instanceof Boolean){
                        isPresent = (Boolean) object;
                    }
                }

                Instance instance = new Instance(trainingVector.size());
                instance.setValue((Attribute) trainingVector.elementAt(0), total);
//                instance.setValue((Attribute) trainingVector.elementAt(1), (isPresent) ? 1 : 0);
//                instance.setValue((Attribute) trainingVector.elementAt(2), "yes");

                if(total >= 0.8D) {
                    instance.setValue((Attribute) trainingVector.elementAt(1), "high");
                }

                if(total >= 0.5D && total < 0.8D){
                    instance.setValue((Attribute) trainingVector.elementAt(1), "moderate");
                }

                if(total < 0.4){
                    instance.setValue((Attribute) trainingVector.elementAt(1), "low");
                }

                instance.setDataset(trainingInstances);

                isTestingSet.add(instance);
                instanceList.add(instance);
            }
        }


        Classifier multinomial = (Classifier) new NaiveBayesMultinomial();
        try {
            multinomial.buildClassifier(isTestingSet );
            Evaluation evaluation = new Evaluation(isTestingSet);

            if(instanceList != null && !instanceList.isEmpty()) {
                for (Instance instance : instanceList) {
                    double[] distribution = multinomial.distributionForInstance(instance);
                    double d = evaluation.evaluateModelOnce(distribution, instance);
                    double s = multinomial.classifyInstance(instance);
                    logger.info("Status");
                }
            }
            double[]  evaluationResults = evaluation.evaluateModel(multinomial, isTestingSet);
            evaluation.KBMeanInformation();
            FastVector predictions = evaluation.predictions();

            int size = predictions.size();

            if(size > 0){
                for(int x = 0; x < size; x++){
                    Object object = predictions.elementAt(x);
                    logger.info("Ob"+ object.toString());
                }
            }

            String summary = evaluation.toSummaryString();
            double weightedFMeasure = evaluation.weightedFMeasure();
            double[][] confusionMatrix = evaluation.confusionMatrix();
            logger.info("Summary : "+ summary);
        } catch (Exception e){
            logger.warn("Error in computing multinominal naive bayes. Message : "+ e.getMessage());
        }

        modelAndView.addAllObjects(response);
        return modelAndView;
    }


    @RequestMapping(value = "/v1/sort/", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ModelAndView sortByReleaveToSearchTerm(HttpServletRequest request,
                                                  @RequestParam(name = "searchTerm", required = true) String term,
                                                  @RequestBody(required = true) String requestBody){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();

        List<String> titles = new ArrayList<>();

        if(StringUtils.isNotBlank(requestBody)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                @SuppressWarnings("uncheched")
                List<String> requestTitles = objectMapper.readValue(requestBody, List.class);
                if(requestTitles != null && !requestTitles.isEmpty()){
                    titles.addAll(requestTitles);
                }
            } catch (Exception e) {
                logger.warn("Error in parsing JSON body. Message : " + e.getMessage());
            }
        }

        String[] termTokens = classificationService.tokenize(term.toLowerCase().trim(), " ");
        List<String> termTokensAsList = Arrays.asList(termTokens);

        Set<String> setB = new HashSet<>();
        setB.addAll(termTokensAsList);

        logger.info("About to stem all tokens in search term provided by user. Incoming search terms : "
                + Arrays.toString(setB.toArray()));

        Set<String> stemmedSet = getStemSet(setB);
        setB.addAll(stemmedSet);

        logger.info("Done stemming all tokens in search term provided. Unique terms : "
                + Arrays.toString(setB.toArray()));


        termTokensAsList = new ArrayList<>();
        termTokensAsList.addAll(setB);


        List<String> titleAsTokens = new ArrayList<>();

        Map<String, Boolean> foundInText = new HashMap<>();

        List<Map> titleToScores = new LinkedList<>();

        List<String> orderedTitles = new ArrayList<>();

        if(!titles.isEmpty()){
            for(String title : titles){
                Map<String, Object> map = new HashMap<>();
                String[] titleToken = classificationService.tokenize(title.toLowerCase().trim(), " ");
                List<String> titleTokenList = Arrays.asList(titleToken);
                Set<String> setA = new HashSet<>();
                setA.addAll(titleTokenList);

                logger.info("About to stem all tokens in title term provided by user. Incoming search terms : "
                        + Arrays.toString(setA.toArray()));

                stemmedSet = getStemSet(setA);
                setA.addAll(stemmedSet);

                logger.info("Done stemming all tokens in title term provided. Unique terms : "
                        + Arrays.toString(setA.toArray()));

                titleTokenList = new ArrayList<>();
                titleTokenList.addAll(setA);

                List<Map> termToTokenMap = new ArrayList<>();
                double total = 0;
                double presenceCounter = 0d;

                for(String t : termTokensAsList) {
                    Map<String, Object> m = new HashMap<>();
                    double weightedScore = classificationService.getTfIdfWeightScore(titleToken, t);
                    m.put("term", t);
                    m.put("weightedScore", weightedScore);
                    total += (Double.isNaN(weightedScore)) ? 0 : weightedScore;

                    Pattern pattern = Pattern.compile(t, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(title);
                    while (matcher.find()){
                        presenceCounter += 1;
                    }

                    termToTokenMap.add(m);
                }

                double presenceScore = (presenceCounter > 0) ? (presenceCounter/termTokensAsList.size()) : 0;

                map.put("presenceScore", presenceScore);
                map.put("scores", termToTokenMap);
                map.put("total", total);
                map.put("title", title);
                map.put("isPresent", (presenceScore > 0));

                titleToScores.add(map);
            }
        }

        //Numeric values
        Attribute attributeWeightedFrequencyScore = new Attribute("weightedFrequencyScore");

//        Attribute attributePresenceScore = new Attribute("presenceScore");
//
//        //Nominal values
//        FastVector nominalIsPresence = new FastVector();
//        nominalIsPresence.addElement(1);
//        nominalIsPresence.addElement(0);
//        nominalIsPresence.addElement("0.5");
//        Attribute attributeIsPresence = new Attribute("isPresenceAttributeLabel", nominalIsPresence);
//        logger.info("is nominal : "+ attributeIsPresence.isNominal());

        //Class attribute
        FastVector rankingClassValue = new FastVector(3);
        rankingClassValue.addElement("high");
        rankingClassValue.addElement("moderate");
        rankingClassValue.addElement("low");

        Attribute classAttribute = new Attribute("rankingClass", rankingClassValue);

        //Declare a vector attribute
        FastVector trainingVector = new FastVector(2);
        trainingVector.addElement(attributeWeightedFrequencyScore);
//        trainingVector.addElement(attributeIsPresence);
        //trainingVector.addElement(attributeIsPresence);
        trainingVector.addElement(classAttribute);


        Instances trainingInstances = new Instances("trainingRel", trainingVector, 10);
        trainingInstances.setClassIndex(trainingVector.size() - 1);

        Instance trainingInstance = new Instance(trainingVector.size());
        trainingInstance.setValue((Attribute) trainingVector.elementAt(0), 0.8);
//        trainingInstance.setValue((Attribute) trainingVector.elementAt(1),  1);
        //trainingInstance.setValue((Attribute) trainingVector.elementAt(2), "yes");
        trainingInstance.setValue((Attribute) trainingVector.elementAt(1), "high");


        Instance trainingInstance2 = new Instance(trainingVector.size());
        trainingInstance2.setValue((Attribute) trainingVector.elementAt(0), 0);
//        trainingInstance2.setValue((Attribute) trainingVector.elementAt(1), 0);
//        trainingInstance2.setValue((Attribute) trainingVector.elementAt(2), "no");
        trainingInstance2.setValue((Attribute) trainingVector.elementAt(1), "low");
//
        Instance trainingInstance3 = new Instance(trainingVector.size());
        trainingInstance3.setValue((Attribute) trainingVector.elementAt(0), 0.5);
//        trainingInstance3.setValue((Attribute) trainingVector.elementAt(1), 1);
//        trainingInstance3.setValue((Attribute) trainingVector.elementAt(2), "no");
        trainingInstance3.setValue((Attribute) trainingVector.elementAt(1), "moderate");

        trainingInstances.add(trainingInstance);
        trainingInstances.add(trainingInstance2);
        trainingInstances.add(trainingInstance3);


        Instances isTestingSet = new Instances("trainingData", trainingVector, 10);
        isTestingSet.setClassIndex(trainingVector.size() - 1);


        List<Instance> instanceList = null;
        if(!titleToScores.isEmpty()) {

            instanceList = new ArrayList<>();

            for(Map map : titleToScores) {
                Double total = 0D;
                boolean isPresent = false;

                if(map.containsKey("total")){
                    Object object = map.get("total");
                    if(object instanceof Double) {
                        total = (Double) object;
                    }
                }

                if(map.containsKey("isPresent")){
                    Object object = map.get("isPresent");
                    if(object instanceof Boolean){
                        isPresent = (Boolean) object;
                    }
                }

                Instance instance = new Instance(trainingVector.size());
                instance.setValue((Attribute) trainingVector.elementAt(0), total);
//                instance.setValue((Attribute) trainingVector.elementAt(1), (isPresent) ? 1 : 0);
//                instance.setValue((Attribute) trainingVector.elementAt(2), "yes");

                if(total >= 0.8D) {
                    instance.setValue((Attribute) trainingVector.elementAt(1), "high");
                }

                if(total >= 0.5D && total < 0.8D){
                    instance.setValue((Attribute) trainingVector.elementAt(1), "moderate");
                }

                if(total < 0.4){
                    instance.setValue((Attribute) trainingVector.elementAt(1), "low");
                }

                instance.setDataset(trainingInstances);

                isTestingSet.add(instance);
                instanceList.add(instance);
            }
        }


        Classifier multinomial = (Classifier) new NaiveBayesMultinomial();
        try {
            multinomial.buildClassifier(isTestingSet );
            Evaluation evaluation = new Evaluation(isTestingSet);

            if(instanceList != null && !instanceList.isEmpty()) {
                for (Instance instance : instanceList) {
                    double[] distribution = multinomial.distributionForInstance(instance);
                    double d = evaluation.evaluateModelOnce(distribution, instance);
                    double s = multinomial.classifyInstance(instance);
                    logger.info("Status");
                }
            }
            double[]  evaluationResults = evaluation.evaluateModel(multinomial, isTestingSet);
            double kbMeansInfo = evaluation.KBMeanInformation();
            FastVector predictions = evaluation.predictions();

            int size = predictions.size();

            if(size > 0){
                List<Map> scoredMap = new ArrayList<>();
                for(int x = 0; x < size; x++){
                    Object object = predictions.elementAt(x);
                    if(object instanceof NominalPrediction) {
                        Map<String, Object> map = new HashMap<>();
                        NominalPrediction nominalPrediction = (NominalPrediction) object;
                        double actual = nominalPrediction.actual();
                        map.put("actual", actual);
                        double predicted = nominalPrediction.predicted();
                        map.put("predicted", predicted);
                        double margin = nominalPrediction.margin();
                        map.put("margin", margin);
                        String revision = nominalPrediction.getRevision();
                        map.put("revision", revision);
                        map.put("index", x);
                        scoredMap.add(map);
                    }
                }

                if(!scoredMap.isEmpty()){
                    Collections.sort(scoredMap, new Comparator<Map>() {
                        @Override
                        public int compare(Map o1, Map o2) {
                            int a = 0;
                            Double d1 = (Double) o1.get("margin");
                            Double d2 = (Double) o2.get("margin");

                            if(d1 > d2){
                                a = 1;
                            }

                            if(d1 < d2){
                                a = -1;
                            }
                            return a;
                        }
                    });

                    for(Map map : scoredMap){
                        Integer index = null;
                        if(map.containsKey("index")) {
                            Object indexObj = map.get("index");
                            if(indexObj instanceof Integer) {
                                 index = (Integer) map.get("index");
                            }
                        }

                        if(index != null){
                            String title = titles.get(index);
                            if(StringUtils.isNotBlank(title)){
                                orderedTitles.add(title);
                            }
                        }
                    }
                }
            }

            String summary = evaluation.toSummaryString();
            double weightedFMeasure = evaluation.weightedFMeasure();
            double[][] confusionMatrix = evaluation.confusionMatrix();
            logger.info("Summary : "+ summary);

            if(!orderedTitles.isEmpty()){
                response.put("orderedByTitles", orderedTitles);
            }
        } catch (Exception e){
            logger.warn("Error in computing multi-nominal naive bayes. Message : "+ e.getMessage());
        }

        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/tokenize", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public ModelAndView getTokens(@RequestParam(name = "query", required = true) String query,
                                  @RequestParam(name = "byWhitespace", defaultValue = "false") Boolean byWhitespace){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String[] tokens = (!byWhitespace) ? classificationService.tokenize(query) :
                    classificationService.tokenize(query, " ");
            if(tokens != null && tokens.length > 0){
                List<String> tokensAsList = Arrays.asList(tokens);
                if(!tokensAsList.isEmpty()){
                    response.put("tokens", tokensAsList);
                }
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/synonyms", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public ModelAndView getSynonyms(@RequestParam(name = "query") String query,
                                    @RequestParam(name = "enableTokens", required = false, defaultValue = "true")
                                            Boolean enableTokens){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String checkedSpelling = spellCheckerService.getCorrectedLine(query);
            boolean isSpellCorrected = (checkedSpelling.equalsIgnoreCase(query));
            query = isSpellCorrected ? query : checkedSpelling;

            String[] tokens = enableTokens ? classificationService.tokenize(query) : new String[]{query};

            List<Map> posList = classificationService.getPos(tokens);
            List<POSRESPONSES> posresponses = null;
            if(posList != null){
                posresponses = new ArrayList<>();
                for(Map map : posList){
                    if(map.containsKey("pos")){
                        String posStr = map.get("pos").toString();
                        if(StringUtils.isNotBlank(posStr)) {
                            POSRESPONSES posresponses1 = POSRESPONSES.valueOf(posStr);
                            posresponses.add(posresponses1);
                        }
                    }
                }
            }

            if(tokens != null && tokens.length > 0) {
                StringBuilder queryBuilder = new StringBuilder();
                List<String> queries = new ArrayList<>();
                for(String token : tokens) {
                    Map<String, Object> stemMap = getStemmers(token).getModel();
                    if (stemMap != null && !stemMap.isEmpty()) {
                        if (stemMap.containsKey("stems")) {
                            Object stemList = stemMap.get("stems");
                            if (stemList != null && (stemList instanceof List)) {
                                @SuppressWarnings("unchecked")
                                List<String> stemsStringList = (List<String>) stemList;
                                if (stemsStringList.size() > 0) {
                                    queries.add(stemsStringList.get(0));
                                }
                            }
                        }
                    } else {
                        queries.add(token);
                    }
                }

                if(!queries.isEmpty()){
                    int x = 0;
                    for(String stemmedQuery : queries){
                        if(x < (queries.size() - 1)) {
                            queryBuilder.append(stemmedQuery);
                            queryBuilder.append(" ");
                        } else {
                            queryBuilder.append(stemmedQuery);
                        }
                        x++;
                    }
                }

                query = queryBuilder.toString();
            }

            List<String> synonyms = wordNetService.getSynonyms(query,
                    ((posresponses != null && !posresponses.isEmpty()) ? posresponses.get(0) : null));
            response.put("query", query);
            response.put("synonyms", synonyms);
            response.put("isCorrected", isSpellCorrected);
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/synonyms", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ModelAndView getSynonymsViaPost(@RequestBody String queryAsJsonStr,
                                           @RequestParam(name = "dictWordsOnly", required = false)
                                                   Boolean dictWordsOnly){
        ModelAndView modelAndView = null;
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(queryAsJsonStr)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                @SuppressWarnings("unchecked")
                List<String> queryList = (List<String>) objectMapper.readValue(queryAsJsonStr, List.class);
                if(queryList != null && !queryList.isEmpty()) {
                    modelAndView = new ModelAndView(new MappingJackson2JsonView());
                    for(String query : queryList) {
                        if(StringUtils.isNotBlank(query)) {
                            ModelMap modelMap = getSynonyms(query, true).getModelMap();
                            if(modelMap != null && !modelMap.isEmpty()){
                                response.put(query, modelMap);
                            }
                        }
                    }
                    modelAndView.addAllObjects(response);
                } else {
                    modelAndView = new ModelAndView(new MappingJackson2JsonView());
                    response.put("message", "empty query list provided.");
                }
            } catch (Exception e){
                logger.warn("Error occurred while parsing json string in get synonyms. Message : "+ e.getMessage());
            }
        } else {
            modelAndView = new ModelAndView(new MappingJackson2JsonView());
            response.put("message", "empty or null json query string.");
            modelAndView.addAllObjects(response);
        }
        return modelAndView;
    }

    private <T> Set<T> getStemSet(Set<T> setA){
        if(!setA.isEmpty()){
            List<T> stemmedWords = new ArrayList<>();
            for(T termToStem : setA) {
                Map<String, Object> stemMap = getStemmers(termToStem.toString()).getModel();
                if(stemMap != null && !stemMap.isEmpty()){
                    if(stemMap.containsKey("stems")) {
                        Object obj = stemMap.get("stems");
                        if(obj instanceof  List){
                            stemmedWords.addAll((List<T>) obj);
                        }
                    }
                }
            }

            if(!stemmedWords.isEmpty()){
                setA.addAll(stemmedWords);
            }
        }
        return setA;
    }
}