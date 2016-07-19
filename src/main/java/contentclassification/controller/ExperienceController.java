package contentclassification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by rsl_prod_005 on 7/19/16.
 */
@Controller
public class ExperienceController {
    private static final Logger logger = LoggerFactory.getLogger(ExperienceController.class);

    @RequestMapping(value = "/scratchpad", method = RequestMethod.GET)
    public String getExperience(ModelMap modelMap){

        return "experience/index";
    }
}
