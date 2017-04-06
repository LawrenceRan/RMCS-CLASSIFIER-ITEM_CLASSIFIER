package contentclassification.controller;

import contentclassification.domain.JenaImpl;
import contentclassification.domain.POSRESPONSES;
import contentclassification.service.ClassificationServiceImpl;
import contentclassification.service.SpellCheckerService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 3/23/17.
 */
@Controller
public class EntityExtractionController {
    private static Logger logger = LoggerFactory.getLogger(EntityExtractionController.class);

    @Autowired
    private ClassificationServiceImpl classificationService;

    @Autowired
    private SpellCheckerService spellCheckerService;

    @RequestMapping(value = "/v1/entity/extraction", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView getEntityFromText(@RequestParam(value = "query", required = true) String query,
                                          @RequestParam(value = "showPos", required = false) Boolean showPos){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        showPos = (showPos != null) ? showPos : false;
        if(StringUtils.isNotBlank(query)){
            query = spellCheckerService.getCorrectedLine(query);
            String[] tokens = classificationService.tokenize(query);
            if(tokens != null && tokens.length > 0) {
                List<Map> posMaps = classificationService.getPos(tokens);

                if(showPos) {
                    response.put("parts-of-speech", posMaps);
                }

                List<String> nouns = new ArrayList<>();

                if(posMaps != null && !posMaps.isEmpty()) {
                    for(Map posMap : posMaps){
                        if(posMap.containsKey("initial")){
                            String initial = posMap.get("initial").toString();
                            String token = (posMap.containsKey("token")) ? posMap.get("token").toString() : null;

                            POSRESPONSES posresponses = POSRESPONSES.fromString(initial);
                            if(posresponses.equals(POSRESPONSES.NN) || posresponses.equals(POSRESPONSES.NNP)
                                    || posresponses.equals(POSRESPONSES.NNPS) || posresponses.equals(POSRESPONSES.NNS)){
                                if(StringUtils.isNotBlank(token)) {
                                    String stemmed = classificationService.getStem(token);
                                    nouns.add(stemmed);
                                }
                            }
                        }
                    }
                }

                if(!nouns.isEmpty()){
                    List<Map> definitions = new ArrayList<>();
                    for(String noun : nouns) {
                        JenaImpl jena = JenaImpl.setQuery(noun);
                        List<Map> queryResponse = jena.getResponse();
                        if (queryResponse != null && !queryResponse.isEmpty()) {
                            Map<String, Object> responseMap = new HashMap<>();
                            responseMap.put("term", noun);
                            responseMap.put("definition", queryResponse);
                            definitions.add(responseMap);
                        }
                    }
                    response.put("definitions", definitions);
                    logger.info("About...");
                }
            }
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }
}
