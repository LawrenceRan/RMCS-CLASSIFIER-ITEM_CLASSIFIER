package contentclassification.controller;

import com.google.common.collect.HashBiMap;
import com.hp.hpl.jena.rdf.model.Model;
import contentclassification.model.Domain;
//import contentclassification.service.DomainGraphDBImpl;
import contentclassification.service.DomainServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.*;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
@RestController
public class DomainController {
    private static final Logger logger = LoggerFactory.getLogger(DomainController.class);
    @Autowired
    private DomainServiceImpl domainService;

    @RequestMapping(value = "/v1/domain", method = RequestMethod.GET)
    public ModelAndView getDomains(){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        try {
            Iterable<Domain> domainIterable = domainService.findAll();
            if (domainIterable != null) {
                List<Map> domainMaps = new ArrayList<>();
                Iterator<Domain> domainIterator = domainIterable.iterator();
                while (domainIterator.hasNext()) {
                    Domain domain = domainIterator.next();
                    domainMaps.add(domain.toMap());
                }
                response.put("domains", domainMaps);
            }
        } catch (Exception e){
            logger.debug("Error in getting all domains from graph. Message: "+ e.getMessage());
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/domain", method = RequestMethod.POST)
    public ModelAndView addDomain(@RequestParam(name = "name") String name){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();
        if(StringUtils.isNotBlank(name)){
            Domain domain = new Domain();
            domain.setName(name.toUpperCase());
            domain.setCreatedOn(new Date());

            domain = domainService.createOrUpdate(domain);
            response.putAll(domain.toMap());
        } else {
            response.put("message", "empty or invalid domain name.");
        }
        modelAndView.addAllObjects(response);
        return modelAndView;
    }

    @RequestMapping(value = "/v1/domain/{id}", method = RequestMethod.DELETE)
    public ModelAndView deleteDomain(@PathVariable(value = "id") Long id){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        return modelAndView;
    }
}
