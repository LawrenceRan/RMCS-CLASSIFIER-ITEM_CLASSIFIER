package contentclassification.controller;

import contentclassification.model.Color;
import contentclassification.model.Domain;
//import contentclassification.service.ColorGraphDBImpl;
//import contentclassification.service.DomainGraphDBImpl;
import contentclassification.service.ColorServiceImpl;
import contentclassification.service.DomainServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.*;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
@RestController
public class ColorController {
    @Autowired
    private ColorServiceImpl colorService;

    @Autowired
    DomainServiceImpl domainService;

    @RequestMapping(value = "/v1/color", method = RequestMethod.POST)
    public ModelAndView addColor(@RequestParam(name = "name") String name){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap();
        if(StringUtils.isNotBlank(name)){
            Color color = new Color();
            color.setCreatedOn(new Date());
            color.setName(name.toUpperCase());
            Domain domain =  domainService.find(2L);
            color.setDomain(domain);

            color = colorService.createOrUpdate(color);
            response.putAll(color.toMap());
        } else {
            response.put("message", "empty or invalid name.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/colors", method = RequestMethod.GET)
    public ModelAndView getColors(){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        List<Map> colorsMap = new ArrayList<>();
        Iterable<Color> colorIterable = colorService.findAll();
        Iterator<Color> colorIterator = colorIterable.iterator();
        while(colorIterator.hasNext()){
            Color c = colorIterator.next();
            colorsMap.add(c.toMap());
        }
        response.putAll(response);
        return modelAndView;
    }
}
