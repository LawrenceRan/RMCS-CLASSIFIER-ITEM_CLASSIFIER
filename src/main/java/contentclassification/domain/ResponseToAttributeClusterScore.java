package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Comparator;

/**
 * Created by rsl_prod_005 on 6/24/16.
 */
public class ResponseToAttributeClusterScore implements Comparable<ResponseToAttributeClusterScore> {
    private ResponseCategoryToAttribute responseCategoryToAttribute;
    private double score;

    public ResponseCategoryToAttribute getResponseCategoryToAttribute() {
        return responseCategoryToAttribute;
    }

    public void setResponseCategoryToAttribute(ResponseCategoryToAttribute responseCategoryToAttribute) {
        this.responseCategoryToAttribute = responseCategoryToAttribute;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(ResponseToAttributeClusterScore o) {
        if(getScore() >= o.getScore()){
            return 1;
        }

        if(getScore() == o.getScore()) {
            return 0;
        }

        if (getScore() < o.getScore()){
            return -1;
        }

        return 0;
    }

    @Override
    public int hashCode(){
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.responseCategoryToAttribute);
        hashCodeBuilder.append(this.score);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof ResponseToAttributeClusterScore){
            ResponseToAttributeClusterScore responseToAttributeClusterScore = (ResponseToAttributeClusterScore) object;
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(this.responseCategoryToAttribute, responseToAttributeClusterScore.getResponseCategoryToAttribute());
            equalsBuilder.append(this.score, responseToAttributeClusterScore.getScore());
            return equalsBuilder.isEquals();
        }
        return false;
    }

    public static Comparator<ResponseToAttributeClusterScore> responseToAttributeClusterScoreComparator = new Comparator<ResponseToAttributeClusterScore>() {
        @Override
        public int compare(ResponseToAttributeClusterScore o1, ResponseToAttributeClusterScore o2) {
            double d1 = o1.getScore();
            double d2 = o2.getScore();
            int value = 0;
            if(d1 >= d2){
                value = 1;
            }
            if( d1 == d2){
                value = 0;
            }

            return value;
        }
    };
}
