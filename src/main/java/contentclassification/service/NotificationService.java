package contentclassification.service;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by rsl_prod_005 on 8/8/16.
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Autowired
    private Environment environment;

    @Autowired
    private JavaMailSender javaMailSender;

    @Async
    public void sendNotification() throws MailException, InterruptedException {
        logger.info("About to send email,");
        Thread.sleep(10000);

        logger.info("Sending mail...");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo("nanabenyin.otoo@rancardsolutions.com");
        simpleMailMessage.setFrom("auto-notif@rancardsolutions.com");
        simpleMailMessage.setSubject("testing e-mail sender ");
        simpleMailMessage.setText("testing");
        javaMailSender.send(simpleMailMessage);
        logger.info("Done sending mail...");
    }

    @Async
    public void sendExceptionEmail(Map<String, Object> errorAttributes) throws MailException, InterruptedException {
        logger.info("About to send exception as email.");
        Thread.sleep(10000);

        String profile = environment.getActiveProfiles()[0];
        String appName = environment.getProperty("spring.application.name");

        StringBuilder messageBuilder = new StringBuilder();
        if(!errorAttributes.isEmpty()){
            int x = 0;
            for(Map.Entry<String, Object> entry : errorAttributes.entrySet()){
                String message = x < (errorAttributes.size() - 1) ? WordUtils.capitalize(entry.getKey()) + ": " + entry.getValue() + "\n"
                        : WordUtils.capitalize(entry.getKey()) + " : " + entry.getValue();
                messageBuilder.append(message);
            }
        }
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo("nanabenyin.otoo@rancardsolutions.com");
        simpleMailMessage.setFrom("auto-notif@rancardsolutions.com");
        simpleMailMessage.setSubject( appName +" exception occurred : " + WordUtils.capitalize(profile) + "");
        simpleMailMessage.setText(messageBuilder.toString());
        javaMailSender.send(simpleMailMessage);
        logger.info("done sending exception as mail.");
    }
}
