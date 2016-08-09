package contentclassification.controller;

import contentclassification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 8/8/16.
 */
@Controller
public class AppErrorController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(AppErrorController.class);

    @Autowired
    private NotificationService notificationService;

    private ErrorAttributes errorAttributes;

    private final static String ERROR_PATH = "/error";

    public AppErrorController(){}

    public AppErrorController(ErrorAttributes errorAttributes){
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = ERROR_PATH, produces = "text/html")
    public String errorPage(ModelMap modelMap, HttpServletRequest request){
        try {
            notificationService.sendExceptionEmail(getErrorAttributes(request, true));
        } catch (Exception e){
            logger.debug("Error in sending exception message. Message: "+ e.getMessage());
        }

        modelMap.addAttribute("errorAttributes", getErrorAttributes(request, true));
        return "error";
    }

    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request){
        try {
            notificationService.sendExceptionEmail(getErrorAttributes(request, true));
        } catch (Exception e){
            logger.debug("Error in sending exception message. Message: "+ e.getMessage());
        }

        Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(body, status);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace){
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return this.errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode);
            }
            catch (Exception ex) {
                logger.debug("Error in http status method. Message: "+ ex.getMessage());
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return !"false".equals(parameter.toLowerCase());
    }
}
