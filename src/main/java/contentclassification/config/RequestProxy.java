package contentclassification.config;

import org.apache.xpath.operations.Bool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.validation.constraints.NotNull;

/**
 * Created by rsl_prod_005 on 7/28/16.
 */
@Configuration
@ConfigurationProperties(locations = "classpath:request-proxy.properties", prefix = "proxy")
@PropertySource("classpath:request-proxy.properties")
public class RequestProxy {
    @NotNull
    @Value("${proxy.enable}")
    private String isEnable;

    @NotNull
    @Value("${proxy.url}")
    private String proxyUrl;

    public String getIsEnable() {
        return isEnable;
    }

    public boolean isEnable(){
        return Boolean.parseBoolean(getIsEnable());
    }

    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
}
