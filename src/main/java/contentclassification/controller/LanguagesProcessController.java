package contentclassification.controller;

import com.github.jsonldjava.utils.Obj;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import contentclassification.domain.Classification;
import contentclassification.domain.POSRESPONSES;
import contentclassification.domain.TermsPositionByPos;
import contentclassification.domain.TermsPositionByPosResponse;
import contentclassification.service.ClassificationServiceImpl;
import contentclassification.service.WordNetService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.StatelessKieSession;
import org.neo4j.cypher.internal.compiler.v2_2.ast.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 3/31/17.
 */
@Controller
@RequestMapping(value = "/v1/language")
public class LanguagesProcessController {
    private static Logger logger = LoggerFactory.getLogger(LanguagesProcessController.class);

    @Autowired
    private WordNetService wordNetService;

    @Autowired
    private ClassificationServiceImpl classificationService;

    @Autowired
    private StatelessKieSession kieSession;

    @RequestMapping(value = "/intent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView getActionSubjectAndCondition(@RequestParam(value = "query", required = true) String query,
                                                     @RequestParam(value = "showPos", required = false,
                                                             defaultValue = "false") Boolean showPos ){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(query)){
            String[] tokens = classificationService.tokenize(query);
            List<Map> posObj = null;
            if(tokens != null && tokens.length > 0) {
                Classification classification = new Classification();
                posObj = classification.getPos(tokens);
            }

            Map<String, Object> tokenToDefinition = new HashMap<>();
            Map<String, Object> tokenToPossiblePartsOfSpeech = new HashMap<>();
            ListMultimap<String, Integer> tokenToPosition = ArrayListMultimap.create();
            List<String> posTags = new ArrayList<>();

            LinkedList<String> orderedTokens = new LinkedList<>();
            if(tokens != null && tokens.length > 0) {
                orderedTokens.addAll(Arrays.asList(tokens));
            }

            Integer counter = 0;
            if(!orderedTokens.isEmpty()) {
                for(String token : orderedTokens) {
                    List<Map> definitionMap = wordNetService.getResponse(token);
                    if(definitionMap != null && !definitionMap.isEmpty()){
                        tokenToDefinition.put(token, definitionMap);

                        List<Integer> possiblePartsOfSpeech = getPossibleTokenPos(definitionMap);
                        if(possiblePartsOfSpeech != null && !possiblePartsOfSpeech.isEmpty()) {
                            List<Map> possiblePartsOfSpeechMap = getPossibleTokenPosWithInitial(possiblePartsOfSpeech);

                            List<Map> getPosInitialFromPosTagging = getPosInitialFromPosTagging(token, posObj);

                            if(getPosInitialFromPosTagging != null && !getPosInitialFromPosTagging.isEmpty()){
                                possiblePartsOfSpeechMap.addAll(getPosInitialFromPosTagging);
                            }

                            tokenToPossiblePartsOfSpeech.put(token, possiblePartsOfSpeechMap);
                        }
                    } else {
                        logger.info("WordNet service response for : "+ token + " was null or empty.");
                        List<Map> getPosInitialFromPosTagging = getPosInitialFromPosTagging(token, posObj);
                        tokenToPossiblePartsOfSpeech.put(token, getPosInitialFromPosTagging);
                    }

                    tokenToPosition.put(token, counter);
                    counter++;
                }
            }

            Integer totalCount = counter;
            TermsPositionByPosResponse termsPositionByPosResponse
                    = getTermsPositionByPosResponse(tokenToPossiblePartsOfSpeech, totalCount, tokenToPosition, orderedTokens);
            response.put("query", query);
            response.put("suggestions", termsPositionByPosResponse.getSuggestions());

            if(showPos) {
                response.put("possiblePos", tokenToPossiblePartsOfSpeech);
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    private List<Integer> getPossibleTokenPos(List<Map> definitionMap){
        List<Integer> posList = null;
        if(definitionMap != null && !definitionMap.isEmpty()) {
            posList = new ArrayList<>();
            for (Map map : definitionMap) {
                if(map.containsKey("type")){
                    Object type = map.get("type");
                    if(type != null && (type instanceof String)){
                        try {
                            posList.add(Integer.parseInt(type.toString()));
                        } catch (Exception e){
                            logger.warn("Error in casting string to integer. Message : "+ e.getMessage());
                        }
                    }
                }
            }

            //clean up
            Set<Integer> cleaner = new HashSet<>();
            cleaner.addAll(posList);
            posList.clear();
            posList.addAll(cleaner);
        }
        return posList;
    }

    public List<Map> getPossibleTokenPosWithInitial(List<Integer> synsetTypes){
        List<Map> mapList = null;
        if(synsetTypes != null && !synsetTypes.isEmpty()){
            mapList = new ArrayList<>();
            for(Integer value : synsetTypes){
                String initial = wordNetService.getSynsetTypeInitialByCode(value);
                if(StringUtils.isNotBlank(initial)){
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("typeId", value);
                    objectMap.put("typeIdInitial", initial);
                    POSRESPONSES posresponses = POSRESPONSES.fromString(initial);
                    objectMap.put("pos", posresponses.ordinal());
                    mapList.add(objectMap);
                }
            }
        }
        return mapList;
    }


    private List<Map> getPosInitialFromPosTagging(String token, List<Map> taggedPos){
        List<Map> mapList = null;
        if(taggedPos != null && !taggedPos.isEmpty()){
            mapList = new ArrayList<>();
            for(Map map : taggedPos){
                String taggedToken = null;
                if(map.containsKey("token")){
                    taggedToken = map.get("token").toString();
                }

                if(StringUtils.isNotBlank(taggedToken)) {
                    if (token.equalsIgnoreCase(taggedToken)) {
                        Map<String, Object> typeMap = new HashMap<>();
                        typeMap.put("typeId", null);
                        typeMap.put("typeIdInitial", map.get("initial"));
                        POSRESPONSES posresponses = POSRESPONSES.fromString(map.get("initial").toString());
                        typeMap.put("pos", posresponses.ordinal());
                        mapList.add(typeMap);
                    }
                }
            }
        }
        return mapList;
    }

    private TermsPositionByPosResponse getTermsPositionByPosResponse(Map<String, Object> termsToPos,
                                                                     Integer totalLength,
                                                                     ListMultimap<String, Integer> positionToTerm,
                                                                     LinkedList<String> orderedTokens){
        TermsPositionByPosResponse termsPositionByPosResponse = null;
        if(termsToPos != null && positionToTerm != null){

            List<Object> facts = new ArrayList<>();

            List<String> posTags = getPosTagsForSentence(termsToPos);
            TermsPositionByPos termsPositionByPos = new TermsPositionByPos(termsToPos, totalLength, positionToTerm,
                    posTags);
            termsPositionByPos.setOrderedTokens(orderedTokens);

            facts.add(termsPositionByPos);

            if(!facts.isEmpty()) {
                termsPositionByPosResponse = new TermsPositionByPosResponse();
                kieSession.setGlobal("termsToPosSuggestion", termsPositionByPosResponse);
                kieSession.execute(facts);

                List<String> suggestions = termsPositionByPosResponse.getSuggestions();
                logger.info("Decision made on request pagination. Results : " + suggestions);
            }
        }
        return termsPositionByPosResponse;
    }

    private List<String> getPosTagsForSentence(Map<String, Object> termsToPos){
        List<String> posTags = null;
        if(termsToPos != null && !termsToPos.isEmpty()){
            posTags = new ArrayList<>();
            logger.info("in");
            for(Map.Entry entry : termsToPos.entrySet()){
                Object object = entry.getValue();
                if(object != null && (object instanceof List)){
                    @SuppressWarnings("unchecked")
                    List<Map> posTagMaps = (List<Map>) object;
                    if(!posTagMaps.isEmpty()){
                        for(Map map : posTagMaps){
                            if(map.containsKey("typeIdInitial")){
                                String type = (map.get("typeIdInitial") != null)
                                        ? map.get("typeIdInitial").toString() : null;
                                if(StringUtils.isNotBlank(type)){
                                    posTags.add(type);
                                }
                            }
                        }
                    }
                }
            }
        }

        if(posTags != null && !posTags.isEmpty()){
            Set<String> cleaner = new HashSet<>();
            cleaner.addAll(posTags);
            posTags.clear();
            posTags.addAll(cleaner);
        }
        return posTags;
    }
}
