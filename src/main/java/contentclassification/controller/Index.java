package contentclassification.controller;

import contentclassification.domain.AppUtils;
import contentclassification.domain.Color;
import contentclassification.domain.RestResponseKeys;
import contentclassification.service.JsoupService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@RestController
public class Index {
    private static final Logger logger = LoggerFactory.getLogger(Index.class);

    @Autowired
    private JsoupService jsoupService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> data = new HashMap<>();
        data.put("message", "welcome to item classification test application.");
        modelAndView.addObject("data", data);
        return modelAndView;
    }

    @RequestMapping(value = "/feed", method = RequestMethod.GET)
    public ModelAndView analyzeFeed(@RequestParam(required = true) String url) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }

    @RequestMapping(value = "/text", method = RequestMethod.GET)
    public ModelAndView analyzeText(@RequestParam(required = true) String text) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }

    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public ModelAndView generateTags(@RequestParam(required = true) String text) {
        logger.info("Request for custom tags using parameter: " + text);
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }

    @RequestMapping(value = "/learning", method = RequestMethod.GET, produces = "application/json")
    public ModelAndView getExternalData(@RequestParam(required = true) String query) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();

        return modelAndView;
    }

    @RequestMapping(value = "/url", method = RequestMethod.GET, produces = "application/json")
    public ModelAndView generateTagsByUrl(@RequestParam(required = true, name = "url") String url) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(url)){
            String title = jsoupService.getTitle(url);
            if(StringUtils.isNotBlank(title)){

            } else{
                response.put(RestResponseKeys.MESSAGE.toString(), "empty title from document from url.");
                modelAndView.addAllObjects(response);
            }

            String text = jsoupService.bodyTextByHtmlUnit(url);
             if(StringUtils.isNotBlank(text)) {
                List<String> potentialColor = AppUtils.getColorByRegEx(text);
                if(!potentialColor.isEmpty()){
                    List<String> getColorsFromRegExObj = AppUtils.getColorsFromRegEx(potentialColor);
                    response.put("data", getColorsFromRegExObj);
                }
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
