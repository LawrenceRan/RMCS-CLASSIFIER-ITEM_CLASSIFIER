package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
        InputStream inputStream = classLoader.getResourceAsStream(RESOURCE);
        if(inputStream != null){
            try{
                Yaml yaml = new Yaml();
                List<Map> data = (List<Map>) yaml.load(inputStream);
                if (data != null && !data.isEmpty()){
                    for(Map<String, String> m  : data){
                        ColorsDescription colorsDescription = new ColorsDescription();
                        if(m.containsKey("word")){
                            String word = m.get("word");
                            if(StringUtils.isNotBlank(word)) {
                                colorsDescription.setWord(word);
                            }
                        }

                        if(m.containsKey("pos")){
                            String posStr = m.get("pos");
                            if(StringUtils.isNotBlank(posStr)) {
                                POSRESPONSES pos = POSRESPONSES.fromString(posStr);
                                if(pos != null){
                                    colorsDescription.setPosresponses(pos);
                                }
                            }
                        }

                        if (m.containsKey("literary")){
                            String literary = m.get("literary");
                            if(StringUtils.isNotBlank(literary)){
                                colorsDescription.setLiterary(literary);
                            }
                        }

                        colorsDescriptionList.add(colorsDescription);
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting colors descriptors. Message: "+ e.getMessage());
            }
        }
        return colorsDescriptionList;
    }

    public static List<String> colorDescriptorsWords(){
        List<String> colors = new ArrayList<>();
        List<ColorsDescription> colorsDescriptionList = loadColorsDescriptionList();
        if(!colorsDescriptionList.isEmpty()){
            for(ColorsDescription cd : colorsDescriptionList){
                colors.add(cd.getWord());
            }
        }
        return colors;
    }
}
