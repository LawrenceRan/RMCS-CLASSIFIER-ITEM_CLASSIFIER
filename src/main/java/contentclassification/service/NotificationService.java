package contentclassification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created by rsl_prod_005 on 8/8/16.
 */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

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
        logger.info("Done sending mail...");
    }
}
