package contentclassification.service;

import contentclassification.domain.*;

import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/24/16.
 */
public interface ClassificationService {

    public List<String> uniqueCollection(String text);
    public String[] tokenize(String text);
    public String[] sentenceDetection(String text);
    public String getStem(String word);
    public List<String> getStems(String[] tokens);
    public List<Map> getPos(String[] tokens);
    public List<String> getIntersection(List<String> a, List<String> b);
    public List<Categories> getCategories();
    public double getTFScore(String[] document, String term);
    public double getIdfScore(String[] document, String term);
    public double getTfIdfWeightScore(String[] document, String term);
    public List<String> prepareTokens(List<String> tokens);
    public <T> List<Map> generateKeyValuePairs(List<T> object);
    public String getContentMetaDataValue(NameAndContentMetaData n, List<Map> metaList, WebMetaName webMetaName);
    public List<String> getMultiWordedAttributes(Categories categories);
    public boolean termFoundInSentences(String[] sentences, String term);
    public Integer getTermToGroupScore(String term, String group);
    public List<TermToGroupScore> getTermToGroupByContentAreaGroupings(List<TermToGroupScore> termToGroupScores,
                                                                       ContentAreaGroupings contentAreaGroupings);
    public Double getTermScoringThreshold();
    public String getSentencesAsString(String... sentences);
    public String removePossibleUrlFromText(List<Map> links, String text);
    public String removePossibleInputFieldFromText(String text);
    public String removePossibleImagesFromText(String text);
    public String removeNavigationAndMenuBars(String text);
    public List<String> removeSentencesFromList(List<String> list);
    public List<FabricName> getFabricNames();
    public List<FabricName> getFabricsFromContent(String text);
    public List<String> colorsFromSelectFields(String text);
    public List<String> sizeFromSelectFields(String text);
    public String getCategoryByTerm(String term);
    public String[] getSentencesWithTerm(String[] sentences, String term);
    public List<ResponseCategoryToAttribute> getCombinedMatrix(
            List<ResponseCategoryToAttribute> responseCategoryToAttributes);
    public List<ResponseCategoryToAttribute> groupResponseByCategory(
            List<ResponseCategoryToAttribute> responseCategoryToAttributes);
    public Map<String, Object> getPrice(String text, List<Map> metaKeyValuePair);
    public Map<String, Object> getGender(String[] sentences, String keywords, String description);
    public String getPossibleTitle(String[] sentences);
    public TFIDFWeightedScore getTfIdfWeightedScore(String[] tokens, String term);
    public TermToGroupScore getTermToGroupScore(ContentAreaGroupings contentAreaGroupings,
                                                String term, String description, String title, String keywords);
    public ResponseCategoryToAttribute refineResultSet(List<ResponseCategoryToAttribute> responseCategoryToAttributeList,
                                                       RulesEngineDataSet rulesEngineDataSet);
    public double applyRulesEngineOccurrence(ResponseCategoryToAttribute responseCategoryToAttribute,
                                                                  RulesEngineDataSet rulesEngineDataSet,
                                                                  RuleEngineInput ruleEngineInput,
                                                                  RuleEngineDataSet ruleEngineDataset);
    public List<String> colorsVerification(List<Map<String, Object>> colorsValidated);
    public List<String> colorsVerified(List<Map<String, Object>> colorsValidated);
    public String getDomainName(String url);
    public String getBrand(String text, String possibleTitle);
}
