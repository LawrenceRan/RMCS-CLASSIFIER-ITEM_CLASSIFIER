package contentclassification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class ContentClassifierMain {
    public static void main(String[] args){
        SpringApplication.run(ContentClassifierMain.class, args);
    }
}
