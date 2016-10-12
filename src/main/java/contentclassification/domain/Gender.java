package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.atlas.test.Gen;
import org.atteo.evo.inflector.English;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsl_prod_005 on 6/20/16.
 */
public class Gender {
    private static final Logger logger = LoggerFactory.getLogger(Gender.class);
    private static final String GENDER_METRICS = "en-gender-attributes";
    private String attribute;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }


    public static List<Gender> loadGenderMatrix(){
        List<Gender> genderList = null;
        try{
            ClassLoader classLoader = Gender.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(GENDER_METRICS);
            if(inputStream != null){
                Yaml yaml = new Yaml();
                try{
                    @SuppressWarnings("unchecked")
                    List<String> genders = (List<String>) yaml.load(inputStream);

                    if(!genders.isEmpty()){
                        //Get the plural forms of words
                        List<String> plurals = new ArrayList<>();
                        for(String s : genders){
                            plurals.add(English.plural(s, 2));
                        }

                        if(!plurals.isEmpty()){
                            genders.addAll(plurals);
                        }

                        genderList = new ArrayList<>();
                        for(String s : genders){
                            if(StringUtils.isNotBlank(s)) {
                                Gender gender = new Gender();
                                gender.setAttribute(s.trim().toLowerCase());
                                genderList.add(gender);
                            }
                        }
                    }
                } catch (Exception e){
                    logger.debug("Error in getting yaml. Message: "+ e.getMessage());
                } finally {
                  try{
                      inputStream.close();
                  } catch (Exception e){
                      logger.warn("Error in closing file. Message : "+ e.getMessage());
                  }
                }
            }
        } catch (Exception e){
            logger.debug("Error in getting gender matrix. Message: "+ e.getMessage());
        }
        return genderList;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.attribute);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Gender){
            Gender gender = (Gender) o;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.attribute, gender.getAttribute());
            return equalsBuilder.isEquals();
        }
        return false;
    }

    @Override
    public String toString(){
        return "[" + this.getAttribute() + "]";
    }
}
