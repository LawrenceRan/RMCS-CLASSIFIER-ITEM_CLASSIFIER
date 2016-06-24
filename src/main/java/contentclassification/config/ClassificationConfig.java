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

    @Value("${clothing.fabric.names.uri}")
    @NotNull
    private String fabricNameResource;

    @Value("${enable.javascript}")
    @NotNull
    private String enableJavascript;

    @Value("${enable.css}")
    @NotNull
    private String enableCss;

    @Value("${response.matrix.threshold}")
    @NotNull
    private String responseMatrixThreshold;

    public String getAddTopLevel() {
        return addTopLevel;
    }

    public void setAddTopLevel(String addTopLevel) {
        this.addTopLevel = addTopLevel;
    }

    public String getFabricNameResource() {
        return fabricNameResource;
    }

    public void setFabricNameResource(String fabricNameResource) {
        this.fabricNameResource = fabricNameResource;
    }

    public String getEnableJavascript() {
        return enableJavascript;
    }

    public void setEnableJavascript(String enableJavascript) {
        this.enableJavascript = enableJavascript;
    }

    public String getEnableCss() {
        return enableCss;
    }

    public void setEnableCss(String enableCss) {
        this.enableCss = enableCss;
    }

    public String getResponseMatrixThreshold() {
        return responseMatrixThreshold;
    }

    public void setResponseMatrixThreshold(String responseMatrixThreshold) {
        this.responseMatrixThreshold = responseMatrixThreshold;
    }
}
