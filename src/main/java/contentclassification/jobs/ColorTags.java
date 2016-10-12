package contentclassification.jobs;

import contentclassification.config.ColorTagsConfig;
import contentclassification.domain.Color;
import contentclassification.domain.JsoupImpl;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

/**
 * Created by rsl_prod_005 on 5/16/16.
 */
@Component
public class ColorTags {
    private static final String CSS_FOR_TAGS = "div.inner-box-panel ul li a";
    private Logger logger = LoggerFactory.getLogger(ColorTags.class);

    @Autowired
    private ColorTagsConfig colorTagsConfig;

    @Scheduled(fixedRate = 14400000)
    public void updateColorTags(){
        List<String> colors = colorsFromSite();

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream colorYmlFile = null;
        try {
            colorYmlFile = classLoader.getResourceAsStream("colors.yml");
        } catch (Exception e){
            logger.debug("Resource colors yml file not found. Message: " + e.getMessage());
        }

        if(colorYmlFile != null) {
            List<Color> exitingColors = Color.loadColorsByInputStream();
            int p = exitingColors.size();
            logger.info("Existing colors size: " + exitingColors.size());

            if (!colors.isEmpty()) {
                for (String c : colors) {
                    Color color = new Color(c);
                    if(!Color.isEqual(exitingColors, color)) {
                        exitingColors.add(color);
                    }
                }

                if(exitingColors.size() > p){
                    writeToColorsDb(exitingColors);
                }
                logger.info("New existing color size: " + exitingColors.size() + " : " + colors.size());
            }

            try {
                colorYmlFile.close();
            } catch (Exception e){
                logger.warn("Error in closing file. Message : "+ e.getMessage());
            }
        }
    }

    private List<String> colorsFromSite(){
        List<String> colors = new ArrayList<>();
        String uri = colorTagsConfig.getUri();
        if(StringUtils.isNotBlank(uri)){
            JsoupImpl jsoup = JsoupImpl.setUrl(uri);
            try {
                Document document = jsoup.getDocument();
                Elements elements = document.select(CSS_FOR_TAGS);
                if(!elements.isEmpty()){
                    Iterator<Element> iterator = elements.iterator();
                    while (iterator.hasNext()){
                        Element element = iterator.next();
                        String color = element.text();
                        colors.add(color.trim());
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting document from jsoup. Message: "+ e.getMessage());
            }
        }
        return colors;
    }

    private void writeToColorsDb(List<Color> updatedColors){
        if(!updatedColors.isEmpty()){
            Yaml yaml = new Yaml();
            List<String> colorsStr = new ArrayList<>();

            for(Color c : updatedColors){
                colorsStr.add(c.toString());
            }

            Set<String> cleanUp = new HashSet<>();
            cleanUp.addAll(colorsStr);
            colorsStr.clear();
            colorsStr.addAll(cleanUp);

            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource("colors.yml");
            if(url != null) {
                String colorYamlFile = url.getFile();

                if (StringUtils.isNotBlank(colorYamlFile)) {
                    try {
                        FileWriter fileWriter = new FileWriter(colorYamlFile);
                        yaml.dump(colorsStr, fileWriter);
                    } catch(Exception e){
                        logger.debug("Error in file writer. Message: "+ e.getMessage());
                    }
                }
            }
        }
    }
}
