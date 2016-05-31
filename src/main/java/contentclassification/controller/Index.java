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
    public ModelAndView generateTagsByUrl(@RequestParam(required = true, name = "url") String url) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if (StringUtils.isNotBlank(url)) {
            String title = jsoupService.getTitle(url);
            List<String> metaList = jsoupService.metas(url);

            List<String> titleTokens = new ArrayList<>();
            if (StringUtils.isNotBlank(title)) {
                titleTokens.addAll(classificationService
                        .prepareTokens(Arrays.asList(classificationService.tokenize(title))));
            } else {
                response.put(RestResponseKeys.MESSAGE.toString(), "empty title from document from url.");
                modelAndView.addAllObjects(response);
            }

            String contentString = jsoupService.getContentAsString(url);

            String text = jsoupService.bodyTextByHtmlUnit(url);
            //List<Map> linksMap = jsoupService.getLinksUrlAndValue(url);
            if (StringUtils.isNotBlank(text)) {
                /**
                 * The start of getting potential colors.
                 */
                List<String> potentialColor = AppUtils.getColorByRegEx(text);

                if (!potentialColor.isEmpty()) {
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

                String article = null;
                if (sentences != null && sentences.length > 0) {
                    article = classificationService.getSentencesAsString(sentences);
                }

                List<Categories> categoriesList = classificationService.getCategories();
                List<String> multiWordAttributes = new ArrayList<>();
                List<String> allAttributes = new ArrayList<>();

                if (categoriesList != null && !categoriesList.isEmpty()) {
                    for (Categories c : categoriesList) {
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

                //Addition analysis of title and content meta data
                List<String> keywordsList = null;
                List<String> descriptionList = null;

                String description = null;
                String keywords = null;

                List<Map> metaKeyValuePair = classificationService.generateKeyValuePairs(metaList);
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
                //end of content meta data


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

                    if (intersect != null && !intersect.isEmpty()) {
                        List<TFIDFWeightedScore> tfIdfWeightedScores = new ArrayList<>();
                        for (String i : intersect) {
                            double tfScore = classificationService.getTFScore(tokens, i);
                            double idfScore = classificationService.getIdfScore(tokens, i);
                            double tfIdfWeightScore = classificationService.getTfIdfWeightScore(tokens, i);

                            TFIDFWeightedScore tfidfWeightedScore = new TFIDFWeightedScore();
                            tfidfWeightedScore.setTerm(i);
                            tfidfWeightedScore.setScore(tfIdfWeightScore);
                            tfidfWeightedScore.setIdfScore(idfScore);
                            tfidfWeightedScore.setTfScore(tfScore);
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

                response.put("allKeywords", allKeywords);


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
                    response.put("totalWeightedScore", totalTermToGroups);
                }

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

                //Get the top level category that an attribute belongs to and score 'em


                response.put("scoreThreshold", totalTermToGroupsFiltered);
            }


        } else {
            response.put(RestResponseKeys.MESSAGE.toString(), "empty or missing url.");
            modelAndView.addAllObjects(response);
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }
}
