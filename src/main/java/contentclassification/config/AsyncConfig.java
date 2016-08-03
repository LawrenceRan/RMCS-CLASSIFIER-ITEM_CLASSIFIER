package contentclassification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Created by rsl_prod_005 on 7/14/16.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Autowired
    private Environment environment;

    @Override
    public Executor getAsyncExecutor() {
        logger.info("About to get async executor.");
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setThreadNamePrefix("3rd-party-provider-thread-");
        threadPoolTaskExecutor.setBeanName("3rd-party-provider-thread");
        threadPoolTaskExecutor.initialize();
        logger.info("Done initializing async executor.");
        return threadPoolTaskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
