package contentclassification.config;

import contentclassification.controller.AppErrorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsl_prod_005 on 8/9/16.
 */
@Configuration
public class ErrorControllerConfiguration {

    @Autowired
    private ErrorAttributes errorAttributes;

    @Bean
    public AppErrorController appErrorController(){
        return new AppErrorController(errorAttributes);
    }
}
