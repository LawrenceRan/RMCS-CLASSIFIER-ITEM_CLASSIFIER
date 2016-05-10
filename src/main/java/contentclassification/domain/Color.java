package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class Color {
    private String name;

    public Color(){}
    public Color(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Color> loadColorsDataSet(){
        List<Color> colors = new ArrayList<Color>();
        Yaml yaml = new Yaml();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String path = classLoader.getResource("ymls/colors").getFile();
            if(StringUtils.isNotBlank(path)) {
                List<String> ymlContent = (List<String>) yaml.load(new FileInputStream(new File(path)));
                if (!ymlContent.isEmpty()) {
                    for (String c : ymlContent) {
                        Color color = new Color(c);
                        colors.add(color);
                    }
                }
            }
        } catch (Exception e){
            System.out.println("Error: "+ e.getMessage());
        }
        return colors;
    }

    public static List<Color> loadColors(){
        List<Color> colors = new ArrayList<Color>();
        Yaml yaml = new Yaml();
        try {
            List<String> yamlContent = (List<String>) yaml.load(new FileInputStream(new File("colors.yml")));
            if(!yamlContent.isEmpty()){
                for(String c : yamlContent){
                    Color color = new Color(c);
                    colors.add(color);
                }
            }
        } catch (Exception e){
            System.out.println("Error: "+ e.getMessage());
        }
        return colors;
    }

    public static List<Color> loadColors(String filePath){
        List<Color> colors = new ArrayList<Color>();
        Yaml yaml = new Yaml();
        try {
            List<String> ymlContent = (List<String>) yaml.load(new FileInputStream(new File(filePath)));
            if(!ymlContent.isEmpty()){
                for(String c : ymlContent){
                    Color color = new Color(c);
                    colors.add(color);
                }
            }
        } catch (Exception e){
            System.out.println("Error: "+ e.getMessage());
        }
        return colors;
    }
}
