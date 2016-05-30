package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
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

    public static List<Color> loadColorsByInputStream(URL filePath){
        List<Color> colors = new ArrayList<Color>();
        Yaml yaml = new Yaml();
        try {
            if(filePath != null) {
                List<String> ymlContent = (List<String>) yaml.load(filePath.openStream());
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

    @Override
    public int hashCode(){
        return new HashCodeBuilder(15, 39).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Color)){
            return false;
        }

        if(obj == this){
            return true;
        }

        Color color = (Color) obj;
        return new EqualsBuilder().append(name, color.getName()).isEquals();
    }

    @Override
    public String toString(){
        return this.getName();
    }

    public static <Color> boolean containInstance(List<Color> colorList, Class<? extends Color> colorObj){
        if(!colorList.isEmpty()){
            for(Color c : colorList){
                if(colorObj.isInstance(c)){
                    return true;
                }
            }
        }
        return false;
    }

    public static <Color> boolean isEqual(List<Color> colorList, Color colorObj){
        if(!colorList.isEmpty()){
            for(Color c : colorList){
                if(colorObj.equals(c)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValid(String color){
        Color c = new Color(color);
        ClassLoader classLoader = c.getClass().getClassLoader();
        URL url = classLoader.getResource("colors.yml");
        return containInstance(loadColorsByInputStream(url), c.getClass());
    }

    public static boolean isExisting(String color){
        Color c = new Color(color);
        ClassLoader classLoader = c.getClass().getClassLoader();
        URL url = classLoader.getResource("colors.yml");
        return isEqual(loadColorsByInputStream(url), c);
    }

    public static boolean isBreakable(String color, String... params){
        boolean isBreakable = false;
        if(params != null && params.length > 0){
            for(String p : params){
                if(color.contains(p)){
                    isBreakable = true;
                }
            }
        }
        return isBreakable;
    }
}
