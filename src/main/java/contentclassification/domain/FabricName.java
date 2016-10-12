package contentclassification.domain;

import contentclassification.config.ClassificationConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rsl_prod_005 on 6/1/16.
 */
public class FabricName {
    private static Logger logger = LoggerFactory.getLogger(FabricName.class);
    private String name;
    private String link;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(name);
        hashCodeBuilder.append(link);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof FabricName){
            FabricName fabricName = (FabricName) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.name, fabricName.getName());
            equalsBuilder.append(this.link, fabricName.getLink());
            return equalsBuilder.isEquals();
        }
        return false;
    }
    public static List<FabricName> getFabricNamesFromExternalUri(){
        List<FabricName> fabricNames = new ArrayList<>();
        try {
            String uri = System.getProperty("clothing.fabric.names.uri");
            if(StringUtils.isNotBlank(uri)) {
                JsoupImpl jsoup = JsoupImpl.setUrl(uri);
                Document document = jsoup.getDocument();
                Elements elements = document.select("div#mw-content-text.mw-content-ltr ul li a");
                if(!elements.isEmpty()){
                    Iterator<Element> elementsIterator =  elements.iterator();
                    while(elementsIterator.hasNext()){
                        FabricName fabricName = new FabricName();
                        Element element = elementsIterator.next();
                        String name = element.text();
                        String link = element.attr("abs:href");
                        if(!fabricName.isAnchorLink(link)) {
                            fabricName.setName(name);
                            fabricName.setLink(link);
                            fabricNames.add(fabricName);
                        }
                    }
                }
            }
        } catch (Exception e){
            logger.debug("Error in getting fabric names: "+ e.getMessage());
        }
        return fabricNames;
    }

    @Override
    public String toString() {
        return "FabricName{" +
                "name='" + name + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    public static List<FabricName> getFabrics(){
        List<FabricName> fabricNames = new ArrayList<>();
        ClassLoader classLoader = FabricName.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("fabric-names.yml");
        if(inputStream != null){
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                List<Map> maps = (List<Map>) yaml.load(inputStream);
                if (!maps.isEmpty()) {
                    for (Map<String, String> m : maps) {
                        FabricName fabricName = new FabricName();
                        for (String keySet : m.keySet()) {
                            if (keySet.equals("link")) {
                                fabricName.setLink(m.get(keySet));
                            }

                            if (keySet.equals("name")) {
                                fabricName.setName(m.get(keySet));
                            }
                        }
                        fabricNames.add(fabricName);
                    }
                }
            } catch (Exception e){
                logger.debug("IOException: "+ e.getMessage());
            } finally {
                try{
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
        return fabricNames;
    }

    private boolean isAnchorLink(String href){
        boolean isAnchorLink = false;
        if(StringUtils.isNotBlank(href)){
            Pattern pattern = Pattern.compile("\\#+\\w.*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(href);
            while (matcher.find()){
                isAnchorLink = true;
            }
        }
        return isAnchorLink;
    }

    public Map<String, String> toMap(){
        Map<String, String> map = new HashMap<>();
        map.put("name", this.getName());
        try {
            map.put("link", URLEncoder.encode(this.getLink(), "UTF-8"));
        } catch (Exception e){
            logger.debug("Error in encoding url: "+ e.getMessage());
        }
        return map;
    }

    public static void writeFabricNames(List<FabricName> fabricNames, InputStream resource){
        if(!fabricNames.isEmpty() && resource != null){
            List<Map> maps = new ArrayList<>();
            for(FabricName fabricName : fabricNames){
                maps.add(fabricName.toMap());
            }

            if(!maps.isEmpty()){
                try {
                    Yaml yaml = new Yaml();
                    Path temp = Files.createTempFile("fabric-names", "yml");
                    Files.copy(resource, temp, StandardCopyOption.REPLACE_EXISTING);
                    yaml.dump(maps, new FileWriter(temp.toFile()));
                } catch (IOException io){
                    logger.debug("IOException: "+ io.getMessage());
                } finally {
                    try{
                        resource.close();
                    }catch (Exception e){
                        logger.warn("Error in closing file. Message : "+ e.getMessage());
                    }
                }
            }
        }
    }
}
