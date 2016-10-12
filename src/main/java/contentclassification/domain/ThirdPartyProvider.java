package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rsl_prod_005 on 8/2/16.
 */
public class ThirdPartyProvider {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyProvider.class);
    private Long id;
    private String domain;
    private String url;
    private String apiKey;
    private List<String> parameters;
    private Date createdOn;
    private Boolean isActive;
    private String method;
    private String urlKey;
    private List<Map<String, Object>> responseKeys;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public List<Map<String, Object>> getResponseKeys() {
        return responseKeys;
    }

    public void setResponseKeys(List<Map<String, Object>> responseKeys) {
        this.responseKeys = responseKeys;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ThirdPartyProvider)) return false;

        ThirdPartyProvider that = (ThirdPartyProvider) object;

        if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null) return false;
        if (createdOn != null ? !createdOn.equals(that.createdOn) : that.createdOn != null) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (responseKeys != null ? !responseKeys.equals(that.responseKeys) : that.responseKeys != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (urlKey != null ? !urlKey.equals(that.urlKey) : that.urlKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (urlKey != null ? urlKey.hashCode() : 0);
        result = 31 * result + (responseKeys != null ? responseKeys.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ThirdPartyProvider{" +
                "id=" + id +
                ", domain='" + domain + '\'' +
                ", url='" + url + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", parameters=" + parameters +
                ", createdOn=" + createdOn +
                ", isActive=" + isActive +
                ", method='" + method + '\'' +
                ", urlKey='" + urlKey + '\'' +
                ", responseKeys=" + responseKeys +
                '}';
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("domain", this.domain);
        map.put("url", this.url);
        map.put("apiKey", this.apiKey);
        map.put("parameters", this.parameters);
        map.put("createdOn", this.createdOn);
        map.put("isActive", this.isActive);
        map.put("method", this.method);
        map.put("responseKeys", this.responseKeys);
        return map;
    }

    public static List<ThirdPartyProvider> loadProviders(){
        List<ThirdPartyProvider> thirdPartyProviderList = new ArrayList<>();
        ClassLoader classLoader = ThirdPartyProvider.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("default-third-party-providers.yml");
        if(inputStream != null){
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> maps = (List<Map<String, Object>>) yaml.load(inputStream);
                if(maps != null && !maps.isEmpty()){
                    for(Map<String, Object> m : maps){
                        ThirdPartyProvider thirdPartyProvider = new ThirdPartyProvider();
                        if(m.containsKey("id")){

                        }

                        if(m.containsKey("domain")){
                            String domain = m.get("domain").toString();
                            thirdPartyProvider.setDomain(domain);
                        }

                        if(m.containsKey("url")){
                            String url = m.get("url").toString();
                            thirdPartyProvider.setUrl(url);
                        }

                        if(m.containsKey("apiKey")){
                            String apiKey = m.get("apiKey").toString();
                            thirdPartyProvider.setApiKey(apiKey);
                        }

                        if(m.containsKey("parameters")){
                            Object parameters = m.get("parameters");
                            if(parameters instanceof List){
                                List<String> p = (List<String>) parameters;
                                if(!p.isEmpty()){
                                   thirdPartyProvider.setParameters(p);
                                }
                            }
                        }

                        if(m.containsKey("createdOn")){
                            String dateStr = m.get("createdOn").toString();
                            if(StringUtils.isNotBlank(dateStr)){
                                String datePattern = "";
                                Date createdOn = null;
                                try {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
                                    createdOn = simpleDateFormat.parse(dateStr);
                                } catch (Exception e){
                                    logger.debug("Error in parsing date. Message: "+ e.getMessage());
                                }

                                if(createdOn != null){
                                    thirdPartyProvider.setCreatedOn(createdOn);
                                }
                            }
                        }

                        if(m.containsKey("isActive")){
                            String isActive = m.get("isActive").toString();
                            if(StringUtils.isNotBlank(isActive)){
                                thirdPartyProvider.setIsActive(Boolean.parseBoolean(isActive));
                            }
                        }

                        if(m.containsKey("method")){
                            String method = m.get("method").toString();
                            if(StringUtils.isNotBlank(method)){
                                thirdPartyProvider.setMethod(method);
                            }
                        }

                        if(m.containsKey("urlKey")){
                            String urlKey = m.get("urlKey").toString();
                            if(StringUtils.isNotBlank(urlKey)){
                                thirdPartyProvider.setUrlKey(urlKey);
                            }
                        }

                        if(m.containsKey("responseKeys")){
                            Object object = m.get("responseKeys");
                            if(object instanceof List){
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> mapList = (List<Map<String, Object>>) object;
                                if(!mapList.isEmpty()){
                                    thirdPartyProvider.setResponseKeys(mapList);
                                }
                            }
                        }
                        thirdPartyProviderList.add(thirdPartyProvider);
                    }
                }
            } catch (Exception e){
                logger.debug("Error in loading providers. Message: "+ e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
        return thirdPartyProviderList;
    }

    public static Map<String, ThirdPartyProvider> domainToThirdParty(){
        Map<String, ThirdPartyProvider> thirdPartyProviderMap = new HashMap<>();
        List<ThirdPartyProvider> thirdPartyProviderList = loadProviders();
        if(!thirdPartyProviderList.isEmpty()){
            for(ThirdPartyProvider thirdPartyProvider : thirdPartyProviderList){
                thirdPartyProviderMap.put(thirdPartyProvider.getDomain(), thirdPartyProvider);
            }
        }
        return thirdPartyProviderMap;
    }
}
