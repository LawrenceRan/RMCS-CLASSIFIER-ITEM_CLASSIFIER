package contentclassification.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by rsl_prod_005 on 1/25/17.
 */
@Component
public class ContextProvider implements ApplicationContextAware {
    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    /**
     * Get bean by type
     */
    public static <T>  T getBean(Class<T> beanClass){
        return CONTEXT.getBean(beanClass);
    }

    /**
     * get a spring bean by name
     *
     */
    public static Object getBean(String beanName){
        return CONTEXT.getBean(beanName);
    }
}
