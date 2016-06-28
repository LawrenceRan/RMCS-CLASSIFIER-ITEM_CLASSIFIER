package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/28/16.
 */
public class ColorsDescription {
    private static final Logger logger = LoggerFactory.getLogger(ColorsDescription.class);
    private static final String RESOURCE = "en-descriptive-words-colors";
    private String word;
    private POSRESPONSES posresponses;
    private String literary;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public POSRESPONSES getPosresponses() {
        return posresponses;
    }

    public void setPosresponses(POSRESPONSES posresponses) {
        this.posresponses = posresponses;
    }

    public String getLiterary() {
        return literary;
    }

    public void setLiterary(String literary) {
        this.literary = literary;
    }

    @Override
    public String toString(){
        return "[ word : "+ this.word +", pos : "+ this.posresponses.toString() +", literary : "+ this.literary+ "]";
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.word);
        hashCodeBuilder.append(this.posresponses);
        hashCodeBuilder.append(this.literary);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof ColorsDescription){
            ColorsDescription colorsDescription = (ColorsDescription) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.word, colorsDescription.getWord());
            equalsBuilder.append(this.posresponses, colorsDescription.getPosresponses());
            equalsBuilder.append(this.literary, colorsDescription.getLiterary());
            return equalsBuilder.isEquals();
        }
        return false;
    }

    public static List<ColorsDescription> loadColorsDescriptionList(){
        List<ColorsDescription> colorsDescriptionList = new ArrayList<>();
        ClassLoader classLoader = ColorsDescription.class.getClassLoader();
        URL url = classLoader.getResource(RESOURCE);
        if(url != null){
            String userDir = System.getProperty("user.dir");
            File file = new File(userDir + "/classes/"+ RESOURCE);

            if(!file.exists() && !file.canRead()){
                file = new File(url.getFile());
            }

            try{
                Yaml yaml = new Yaml();
                List<Map> data = (List<Map>) yaml.load(new FileInputStream(file));
                if (data != null && !data.isEmpty()){
                    for(Map<String, String> m  : data){
                        for(Map.Entry<String, String> m1  : m.entrySet()){
                            
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting colors descriptors. Message: "+ e.getMessage());
            }
        }
        return colorsDescriptionList;
    }
}
