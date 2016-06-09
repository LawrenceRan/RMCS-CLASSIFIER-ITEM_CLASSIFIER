package contentclassification.domain;

import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/9/16.
 */
public class Size {
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
}
