package contentclassification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Main {
    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }
}
