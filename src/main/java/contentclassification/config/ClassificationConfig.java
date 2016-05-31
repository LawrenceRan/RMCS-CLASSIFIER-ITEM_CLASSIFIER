package contentclassification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rsl_prod_005 on 5/30/16.
 */
@Configuration
public class ClassificationConfig {
    @Value("${categories.add.top}")
    @NotNull
    private String addTopLevel;

    public String getAddTopLevel() {
        return addTopLevel;
    }

    public void setAddTopLevel(String addTopLevel) {
        this.addTopLevel = addTopLevel;
    }
}
