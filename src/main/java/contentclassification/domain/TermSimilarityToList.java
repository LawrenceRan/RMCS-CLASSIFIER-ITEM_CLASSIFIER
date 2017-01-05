package contentclassification.domain;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.JaccardIndexSimilarity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.bayes.NaiveBayesMultinomial;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 11/9/16.
 * this class is used to organize and compute how a term is similar to a given list
 */
public class TermSimilarityToList {
    private static Logger logger = LoggerFactory.getLogger(TermSimilarityToList.class);

    private String term;
    private List<String> list;

    public TermSimilarityToList(String term, List<String> list){
        this.term = term.toLowerCase().trim();
        this.list = list;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, Object> highestScore(){
        Map<String, Object> highestScore = null;
        if(list != null && !list.isEmpty()){
            List<Map> termToResults = termToResults();
            if(termToResults != null && !termToResults.isEmpty()) {
                highestScore = termToResults.get(0);
            }
        }
        return highestScore;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        TermSimilarityToList rhs = (TermSimilarityToList) obj;
        return new EqualsBuilder()
                .append(this.term, rhs.term)
                .append(this.list, rhs.list)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(term)
                .append(list)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("term", term)
                .append("list", list)
                .toString();
    }

    private List<Map> termToResults(){
        List<Map> termToResults = new ArrayList<>();
        String term = this.term;
        List<String> list = this.list;

        return termToResults;
    }

    private double[] toDouble(byte[] bytes){
        double[] doubleValue = new double[bytes.length / 2];
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            int x = 0;
            while(byteBuffer.remaining() > 2) {
                doubleValue[x] = byteBuffer.getDouble();
                x++;
            }
        } catch (Exception e){
            logger.warn("Error in getting double value from bytes. Message : "+ e.getMessage());
        }
        return doubleValue;
    }
}
