package contentclassification.controller.image_text_extraction;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 2/16/17.
 */
@Controller
public class TextExtraction {

    @RequestMapping(value = "/v1/text/extraction", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView extractTextFromImage(MultipartFile file){
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        Map<String, Object> response = new HashMap<>();

        modelAndView.addAllObjects(response);
        return modelAndView;
    }
}
