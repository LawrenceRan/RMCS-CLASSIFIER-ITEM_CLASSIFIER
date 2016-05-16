package contentclassification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsl_prod_005 on 5/16/16.
 */
@Configuration
public class ColorTagsConfig {
    @Value("${color.tags.uri}")
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
