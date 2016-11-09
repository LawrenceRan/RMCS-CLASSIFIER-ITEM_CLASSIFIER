package contentclassification.domain;

import info.debatty.java.stringsimilarity.Jaccard;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.yaml.snakeyaml.Yaml;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class Color {
    private static final Logger logger = LoggerFactory.getLogger(Color.class);
    private static final String COLORS = "colors.yml";
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
        ClassLoader classLoader = Color.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(COLORS);
        if(inputStream != null) {
            Yaml yaml = new Yaml();
            try {
                List<String> yamlContent = (List<String>) yaml.load(inputStream);
                if (!yamlContent.isEmpty()) {
                    for (String c : yamlContent) {
                        Color color = new Color(c);
                        colors.add(color);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                try{
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        } else {
            logger.debug("Curated colors resource doesn't exist.");
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

    public static List<Color> loadColorsByInputStream(){
        List<Color> colors = new ArrayList<Color>();
        Yaml yaml = new Yaml();
        try {
            ClassLoader classLoader = Color.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(COLORS);
            if (inputStream != null) {
                @SuppressWarnings("unchecked")
                List<String> ymlContent = (List<String>) yaml.load(inputStream);
                if (!ymlContent.isEmpty()) {
                    for (String c : ymlContent) {
                        Color color = new Color(c);
                        colors.add(color);
                    }
                }
                inputStream.close();
            }
        } catch (Exception e){
            logger.debug("Error in loading colors by input stream. Message : " + e.getMessage());
        }
        return colors;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.name);
        return  hashCodeBuilder.toHashCode();
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
        return containInstance(loadColorsByInputStream(), c.getClass());
    }

    public static boolean isExisting(String color){
        Color c = new Color(color);
        return isEqual(loadColorsByInputStream(), c);
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

    public static Map<String, List<String>> colorExclusionList(){
        Map<String, List<String>> exclude = new HashMap<>();
        ClassLoader classLoader = Color.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("color-exclusion");
        if(inputStream != null){
            try{
                Yaml yaml = new Yaml();
                exclude = (Map<String, List<String>>) yaml.load(inputStream);
            } catch(Exception ex){
                logger.debug("Error in getting colors exclusion list. Message: "+ ex.getMessage());
            } finally {
                try{
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
        return exclude;
    }

    public static void updateExclusionList(String exclude){
        if(StringUtils.isNotBlank(exclude)){
            ClassLoader classLoader = Color.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("color-exclusion");
            if(inputStream != null){
                try {
                    int initialSize = 0;
                    int currentSize = 0;
                    Yaml yaml = new Yaml();
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> list = (Map<String, List<String>>) yaml.load(inputStream);
                    if(list != null && !list.isEmpty()){
                        if(list.containsKey("exclusionList")){
                            List<String> exclusionList = list.get("exclusionList");
                            initialSize = exclusionList.size();
                            if(!exclusionList.isEmpty()){
                                exclusionList.add(exclude);
                                currentSize = exclusionList.size();
                                list.put("exclusionList", exclusionList);
                            }
                        }
                    }

                    if(list != null && !list.isEmpty() && currentSize > initialSize){
                        try {
                            Path temp = Files.createTempFile("color-exclusion", null);
                            Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
                            File file = temp.toFile();

                            FileWriter fileWriter = new FileWriter(file);
                            yaml.dump(list, fileWriter);
                        } catch (IOException e){
                            logger.debug("Error in writing to exclusion list: "+ e.getMessage());
                        }
                    }
                } catch (Exception e){
                    logger.debug("Error in writing: "+ e.getMessage());
                } finally {
                    try{
                        inputStream.close();
                    } catch (Exception e){
                        logger.warn("Error in closing file. Message : "+ e.getMessage());
                    }
                }
            }
        }
    }

    @Async
    public static Future<String> updateExclusionListAsync(String exclude){
        logger.info("About to execute async task for updating exclusion list. "+ Thread.currentThread().getName());
        try{
            Thread.sleep(5000);
            updateExclusionList(exclude);
            return new AsyncResult<String>("Update completed.");
        } catch (InterruptedException e){
            logger.debug("Error : "+ e.getMessage());
        }
        return null;
    }

    public static double similarityAgainstCuratedColors(List<Color> colorList, Color unknownColor){
        double answer = 0d;
        if(unknownColor != null){
//            try {
//                double a = stringSet1.jaccardSimilarity(stringSet2);
//                logger.info("E");
//            } catch (Exception e){
//                logger.debug("Error in kshingling"+ e.getMessage());
//            }
//            double[] d1 = new double[]{10d};
//            double[] d2 = new double[]{10d};
//
//            Instance a = new DenseInstance(d1);
//            Instance b = new DenseInstance(d2);
//
//            JaccardIndexSimilarity jaccardIndexSimilarity = new JaccardIndexSimilarity();
//            double s = jaccardIndexSimilarity.measure(a, b);

            Jaccard jaccard = new Jaccard(2);
            if(!colorList.isEmpty()){
                for(Color color : colorList){
                    answer = jaccard.similarity(unknownColor.getName().trim().toLowerCase(), color.getName());
                }
            }
        }
        return answer;
    }

    public static List<String> colorsAsString(){
        List<String> colors = new ArrayList<>();
        List<Color> colorList = loadColors();
        if(!colorList.isEmpty()){
            for(Color c : colorList){
                colors.add(c.getName());
            }
        }
        return colors;
    }

    public static List<String> colorsAsString(List<Color> colors) {
        List<String> colorList = new ArrayList<>();
        if(colors != null && !colors.isEmpty()){
            for(Color c : colors){
                colorList.add(c.getName());
            }
        }
        return colorList;
    }
}
