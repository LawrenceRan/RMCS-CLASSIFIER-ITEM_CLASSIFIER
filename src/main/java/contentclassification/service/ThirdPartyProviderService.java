package contentclassification.service;

import contentclassification.domain.RegExManager;
import contentclassification.domain.ThirdPartyProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rsl_prod_005 on 8/2/16.
 */
@Service
public class ThirdPartyProviderService {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyProviderService.class);

    public boolean isDomainAProvider(String domain){
        boolean answer = false;
        if(StringUtils.isNotBlank(domain)){
            Map<String, ThirdPartyProvider> thirdPartyProviderMap = ThirdPartyProvider.domainToThirdParty();
            if(!thirdPartyProviderMap.isEmpty()){
                for(Map.Entry<String, ThirdPartyProvider> entry : thirdPartyProviderMap.entrySet()){
                    Pattern pattern = Pattern.compile(entry.getKey());
                    Matcher matcher = pattern.matcher(domain);
                    while (matcher.find()){
                        answer = true;
                    }
                }
            }
        }
        return answer;
    }

    public String getSupportedDomain(String domain){
        String answer = null;
        if(StringUtils.isNotBlank(domain)){
            Map<String, ThirdPartyProvider> thirdPartyProviderMap = ThirdPartyProvider.domainToThirdParty();
            if(!thirdPartyProviderMap.isEmpty()){
                for(Map.Entry<String, ThirdPartyProvider> entry : thirdPartyProviderMap.entrySet()){
                    Pattern pattern = Pattern.compile(entry.getKey());
                    Matcher matcher = pattern.matcher(domain);
                    while (matcher.find()){
                        answer = matcher.group(0);
                    }
                }
            }
        }
        return answer;
    }

    public String getItemDescription(String domain, String url){
        String description = null;
        if(StringUtils.isNotBlank(domain)){
            String supportedDomain = getSupportedDomain(domain);
            if(StringUtils.isNotBlank(supportedDomain)) {
                Map<String, ThirdPartyProvider> thirdPartyProviderMap = ThirdPartyProvider.domainToThirdParty();
                ThirdPartyProvider thirdPartyProvider = thirdPartyProviderMap.get(supportedDomain);
                if(thirdPartyProvider != null) {
                    String requestUrl = thirdPartyProvider.getUrl();
                    String keyQueryPara = thirdPartyProvider.getUrlKey();
                    List<NameValuePair> urlParams = null;

                    try{
                         urlParams = URLEncodedUtils.parse(new URI(url),"UTF-8");
                    } catch (Exception e){
                        logger.debug("Error creating URI. Message: "+ e.getMessage());
                    }

                    String id = null;
                    if(urlParams != null){
                        for(NameValuePair nameValuePair : urlParams){
                            Pattern pattern = Pattern.compile(keyQueryPara, Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(nameValuePair.getName());
                            while (matcher.find()) {
                                id = nameValuePair.getValue();
                            }
                        }
                    }

                    if(StringUtils.isBlank(id)){
                        id = retrieveIdFromUrl(url, thirdPartyProvider.getDomain());
                    }

                    if(StringUtils.isNotBlank(requestUrl) && StringUtils.isNotBlank(id)){
                        String idRegEx = "{id}";
                        requestUrl = requestUrl.replace(idRegEx, id) +"?domain="+ supportedDomain;

                        Map<String, Object> responseMap = null;
                        try {
                            RestTemplate restTemplate = new RestTemplate();
                            responseMap = restTemplate.getForObject(requestUrl, HashMap.class);
                        } catch (Exception e){
                            logger.debug("Error making rest call. Message: "+ e.getMessage());
                        }

                        if(responseMap != null){
                            List<String> responseKeys = new ArrayList<>();
                            List<ThirdPartyProviderResponseKeys> thirdPartyProviderResponseKeysList =
                                    Arrays.asList(ThirdPartyProviderResponseKeys.values());

                            Map<String, String> itemDetailsMap =  null;
                            if(responseMap.containsKey("itemDetails")){
                                Object itemDetailsObj = responseMap.get("itemDetails");
                                if(itemDetailsObj instanceof List){
                                    List<Map> itemDetailsListOfMap = (List) itemDetailsObj;
                                    if(!itemDetailsListOfMap.isEmpty()){
                                        if(itemDetailsListOfMap.get(0) instanceof HashMap) {
                                            itemDetailsMap = itemDetailsListOfMap.get(0);
                                        }
                                    }
                                }
                            }

                            Map<String, String> mapResponseKeysToValues = new HashMap<>();

                            if(!thirdPartyProviderResponseKeysList.isEmpty()){
                                for(ThirdPartyProviderResponseKeys t : thirdPartyProviderResponseKeysList){
                                    responseKeys.add(t.toString());
                                }
                            }

                            if(!responseKeys.isEmpty() && itemDetailsMap != null){
                                for(String k : responseKeys){
                                    if(itemDetailsMap.containsKey(k)){
                                        mapResponseKeysToValues.put(k, itemDetailsMap.get(k).toString());
                                    }
                                }
                            }

                            List<Map<String, Object>> n = thirdPartyProvider.getResponseKeys();
                            Collections.sort(n, new Comparator<Map<String, Object>>() {
                                @Override
                                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                                    Integer i1 = (Integer) o1.get("position");
                                    Integer i2 = (Integer) o2.get("position");
                                    return i1.compareTo(i2);
                                }
                            });

                            List<String> paragraphs = new LinkedList<>();
                            if(!n.isEmpty()){
                                for(Map<String, Object> m : n){
                                    String value = null;
                                    String key = m.get("key").toString();
                                    if(StringUtils.isNotBlank(key)){
                                        value = mapResponseKeysToValues.get(key);
                                        paragraphs.add(key + ": " + value);
                                    }
                                }
                            }

                            StringBuilder stringBuilder = new StringBuilder();
                            if(!paragraphs.isEmpty()){
                                int x = 0;
                                for(String s : paragraphs){
                                    if(x < (paragraphs.size() - 1)) {
                                        stringBuilder.append(s.trim());
                                        stringBuilder.append("\n");
                                    } else {
                                        stringBuilder.append(s.trim());
                                    }
                                    x++;
                                }
                            }
                            description = stringBuilder.toString();
                        }
                    }
                }
            }
        }
        return description;
    }

    private String retrieveIdFromUrl(String url, String domain){
        String id = null;
        try{
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("regex-id-in-urls");
            if(inputStream != null) {
                boolean isDomainPresent = false;
                List<String> patterns = null;
                List<String> exclusionList = null;

                Yaml yaml = new Yaml();
                List<Map<String, Object>> mapList = (List<Map<String, Object>>) yaml.load(inputStream);
                if(mapList != null && !mapList.isEmpty()) {
                    for(Map<String, Object> m : mapList) {
                        if (m.containsKey("domain")) {
                            Object object = m.get("domain");
                            if (object instanceof String) {
                                String dbDomain = object.toString();
                                isDomainPresent = dbDomain.equalsIgnoreCase(domain);
                            }

                            if(isDomainPresent){
                                if(m.containsKey("patterns")){
                                    Object patternsObj = m.get("patterns");
                                    if(patternsObj instanceof List){
                                        patterns = (List<String>) patternsObj;
                                    }
                                }

                                if(m.containsKey("exclude")){
                                    Object ex = m.get("exclude");
                                    exclusionList = ex instanceof List ? (List<String>) ex : null;
                                }
                            }
                        }
                    }
                }

                List<String> foundIds = null;
                if(patterns != null && !patterns.isEmpty()){
                    foundIds = new ArrayList<>();
                    for(String pattern : patterns){
                        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                        Matcher matcher = p.matcher(url);
                        while (matcher.find()){
                            foundIds.add(matcher.group());
                        }
                    }

                    if(!foundIds.isEmpty() && exclusionList != null && !exclusionList.isEmpty()){
                        id = foundIds.get(0);
                        StringBuilder regExBuilder = new StringBuilder();
                        regExBuilder.append("(");

                        int x = 0;
                        for(String s : exclusionList){
                            if(x < exclusionList.size() - 1) {
                                regExBuilder.append(s);
                                regExBuilder.append("|");
                            } else {
                                regExBuilder.append("s");
                            }
                            x++;
                        }
                        regExBuilder.append(")");

                        logger.info("Reg: "+ regExBuilder.toString());

                        id = StringUtils.isBlank(id) ? null : id.replaceAll(regExBuilder.toString(), "");
                    }
                }

                logger.info("found.");
            }
        } catch (Exception e){
            logger.debug("Error occurred in retrieving Id from url. Message: "+ e.getMessage());
        }
        return id;
    }
}
