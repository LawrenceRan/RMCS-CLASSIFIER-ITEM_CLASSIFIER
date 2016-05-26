package contentclassification.controller;

import contentclassification.config.WordNetDictConfig;
import contentclassification.domain.*;
//import contentclassification.service.DomainGraphDBImpl;
import contentclassification.service.ClassificationServiceImpl;
import contentclassification.service.JsoupService;
import contentclassification.service.WordNetService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import weka.core.ClassloaderUtil;

import java.util.*;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@RestController
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

//    @Autowired
//    private DomainGraphDBImpl domainGraphDB;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(){
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

        return modelAndView;
    }

    @RequestMapping(value = "/v1/tags", method = RequestMethod.GET)
    public ModelAndView generateTags(@RequestParam(required = true) String text) {
        logger.info("Request for custom tags using parameter: " + text);
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }

    @RequestMapping(value = "/v1/learning", method = RequestMethod.GET, produces = "application/json")
    public ModelAndView getExternalData(@RequestParam(required = true) String query) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();

        return modelAndView;
    }

    @RequestMapping(value = "/v1/url", method = RequestMethod.GET, produces = "application/json")
    public ModelAndView generateTagsByUrl(@RequestParam(required = true, name = "url") String url){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(url)){
            String title = jsoupService.getTitle(url);
            List<String> metaList = jsoupService.metas(url);

            List<String> titleTokens = new ArrayList<>();
            if(StringUtils.isNotBlank(title)){
                titleTokens.addAll(classificationService
                        .prepareTokens(Arrays.asList(classificationService.tokenize(title))));
            } else{
                response.put(RestResponseKeys.MESSAGE.toString(), "empty title from document from url.");
                modelAndView.addAllObjects(response);
            }

            String text = jsoupService.bodyTextByHtmlUnit(url);
             if(StringUtils.isNotBlank(text)) {
                 /**
                  * The start of getting potential colors.
                  */
                 List<String> potentialColor = AppUtils.getColorByRegEx(text);

                 if(!potentialColor.isEmpty()) {
                     List<String> getColorsFromRegExObj = AppUtils.getColorsFromRegEx(potentialColor);
                     Map<String, Object> colors = new HashMap<>();
                     colors.put("colors", getColorsFromRegExObj);

                     List<Map> colorsValidation = new ArrayList<>();
                     if (!colors.isEmpty()) {
                         for (String s : getColorsFromRegExObj) {
                             Map<String, Object> map = new HashMap<>();
                             map.put("name", s);
                             map.put("isValidated", Color.isExisting(s.trim().toLowerCase()));
                             colorsValidation.add(map);
                         }
                     }
                     colors.put("colorsValidation", colorsValidation);
                     response.putAll(colors);

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
                         response.put("definitions", definitions);
                     }
                 }
                 //End of getting potential colors.

                 //Start of content analysis of content page.
                 String[] tokens = classificationService.tokenize(text);
                 String[] sentences = classificationService.sentenceDetection(text);

                 List<Categories> categoriesList = classificationService.getCategories();
                 List<String> multiWordAttributes = new ArrayList<>();
                 List<String> allAttributes = new ArrayList<>();

                 if(categoriesList != null && !categoriesList.isEmpty()){
                     for(Categories c : categoriesList){
                         allAttributes.addAll(c.getAttributes());
                         multiWordAttributes.addAll(classificationService.getMultiWordedAttributes(c));
                     }
                 }

                 List<String> foundInSentences = new ArrayList<>();
                 if(!multiWordAttributes.isEmpty()){
                     for(String s : multiWordAttributes){
                         boolean termFound = classificationService.termFoundInSentences(sentences, s);
                         if(termFound){
                             foundInSentences.add(s);
                         }
                     }
                     logger.info("Found in sentences "+ foundInSentences);
                 }

                 List<Map> posList = null;
                 List<Map> scoredTermsFromContent = new ArrayList<>();
                 if(tokens != null && tokens.length > 0){
                     List<String> tokensAsList = classificationService.prepareTokens(Arrays.asList(tokens));
                     List<String> intersect = classificationService.getIntersection(tokensAsList, allAttributes);

                     if(intersect != null && !intersect.isEmpty()){
                         List<TFIDFWeightedScore> tfidfWeightedScores = new ArrayList<>();
                         for(String i : intersect){
                             double tfScore = classificationService.getTFScore(tokens, i);
                             double idfScore = classificationService.getIdfScore(tokens, i);
                             double tfIdfWeightScore = classificationService.getTfIdfWeightScore(tokens, i);

                             TFIDFWeightedScore tfidfWeightedScore = new TFIDFWeightedScore();
                             tfidfWeightedScore.setTerm(i);
                             tfidfWeightedScore.setScore(tfIdfWeightScore);
                             tfidfWeightedScore.setIdfScore(idfScore);
                             tfidfWeightedScore.setTfScore(tfScore);
                             tfidfWeightedScores.add(tfidfWeightedScore);
                         }
                         Collections.sort(tfidfWeightedScores, TFIDFWeightedScore.tfidfWeightedScoreComparator);

                         if(!tfidfWeightedScores.isEmpty()){
                             List<Map> tfIdfWeightedScoresMap = new ArrayList<>();
                             for(TFIDFWeightedScore tfidfWeightedScore : tfidfWeightedScores){
                                 tfIdfWeightedScoresMap.add(tfidfWeightedScore.toMap());
                             }
                             response.put("tokensScore", tfIdfWeightedScoresMap);
                             scoredTermsFromContent.addAll(tfIdfWeightedScoresMap);
                         }
                     }
                     posList = classificationService.getPos(tokens);
                 }

                 //end of content analysis...

                 //Addition analysis of title and content meta data
                 List<String> keywordsList = null;
                 List<String> descriptionList = null;

                 List<Map> metaKeyValuePair = classificationService.generateKeyValuePairs(metaList);
                 if (!metaKeyValuePair.isEmpty()) {
                     String description = classificationService
                             .getContentMetaDataValue(NameAndContentMetaData.NAME,
                                     metaKeyValuePair, WebMetaName.DESCRIPTION);
                     String keywords = classificationService
                             .getContentMetaDataValue(NameAndContentMetaData.NAME,
                                     metaKeyValuePair, WebMetaName.KEYWORDS);
                     keywordsList = Arrays.asList(classificationService.tokenize(keywords));
                     descriptionList = Arrays.asList(classificationService.tokenize(description));
                 }

                 //end of content meta data

                logger.info("Potential Color: "+ potentialColor.toString());
            }

//            String content = jsoupService.bodyText(url);
//            if (StringUtils.isNotBlank(content)) {
//                String color = AppUtils.getColorByRegEx(content);
//                response.put("data", color);
//                logger.info("content");
//            }

        } else {
            response.put(RestResponseKeys.MESSAGE.toString(), "empty or missing url.");
            modelAndView.addAllObjects(response);
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }
}
