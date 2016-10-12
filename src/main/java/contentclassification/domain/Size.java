package contentclassification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/9/16.
 */
public class Size {
    private static final Logger logger = LoggerFactory.getLogger(Size.class);
    private static final String SIZE_EXCLUSION = "size-exclusion";
    private static final String SIZE_FILE = "sizes";
    private String category;
    private List<String> sizes;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public static List<Size> loadSizeFromYaml(){
        List<Size> sizes = null;
        try{
            ClassLoader classLoader = Size.class.getClassLoader();
            URL url = classLoader.getResource(SIZE_FILE);
            if(url != null){
                Yaml yaml = new Yaml();
                Map<String, List<String>> map = (Map<String, List<String>>) yaml.load(url.openStream());
                if(map != null && !map.isEmpty()){
                    sizes = new ArrayList<>();
                    for(String keySet : map.keySet()){
                        Size size = new Size();
                        size.setCategory(keySet);
                        size.setSizes(map.get(keySet));
                        sizes.add(size);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return sizes;
    }

    public static Map<String, List<String>> loadSizeExclusionList(){
        Map<String, List<String>> list = null;
        ClassLoader classLoader = Size.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SIZE_EXCLUSION);
        if(inputStream != null){
            try {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, List<String>> maps = (Map<String, List<String>>) yaml.load(inputStream);
                if(!maps.isEmpty()){
                    list = maps;
                }
            } catch (Exception io){
                logger.debug("IO Exception : "+ io.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
        return list;
    }
}
