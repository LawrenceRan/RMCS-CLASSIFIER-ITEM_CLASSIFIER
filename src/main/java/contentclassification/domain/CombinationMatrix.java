package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 6/6/16.
 */
public class CombinationMatrix {
    private static final Logger logger = LoggerFactory.getLogger(CombinationMatrix.class);

    private String categories;
    private List<String> combinedCategories;

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public List<String> getCombinedCategories() {
        return combinedCategories;
    }

    public void setCombinedCategories(List<String> combinedCategories) {
        this.combinedCategories = combinedCategories;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.categories);
        hashCodeBuilder.append(this.combinedCategories);
        return hashCodeBuilder.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof CombinationMatrix){
            CombinationMatrix combinationMatrix = (CombinationMatrix) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.categories, combinationMatrix.getCategories());
            equalsBuilder.append(this.combinedCategories, combinationMatrix.getCombinedCategories());
            return equalsBuilder.isEquals();
        }
        return false;
    }

    public static List<CombinationMatrix> getCombinationMatrix(){
        List<CombinationMatrix> combinationMatrixList = new ArrayList<>();
        try {
            ClassLoader classLoader = CombinationMatrix.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("combination-matrix");
            if(inputStream != null) {
                Yaml yaml = new Yaml();
                Object object = yaml.load(inputStream);
                if(object instanceof List){
                    @SuppressWarnings("unchecked")
                    List<Map> rules = (List<Map>) object;
                    if(!rules.isEmpty()){
                        for(Map<String, Object> map : rules){
                            CombinationMatrix combinationMatrix = new CombinationMatrix();
                            for(String keySet : map.keySet()){
                                if(keySet.equals("category")){
                                    combinationMatrix.setCategories(map.get(keySet).toString());
                                }

                                if(keySet.equals("matrix")){
                                    @SuppressWarnings("unchecked")
                                    List<String> combined = (List<String>) map.get(keySet);
                                    combinationMatrix.setCombinedCategories(combined);
                                }
                            }
                            combinationMatrixList.add(combinationMatrix);
                        }
                    }
                }
                inputStream.close();
            }
        } catch (Exception e){
            logger.debug("Exception in getting combination matrix: "+ e.getMessage());
        }
        return combinationMatrixList;
    }
}
