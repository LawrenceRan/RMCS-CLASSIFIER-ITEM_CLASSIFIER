package contentclassification.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/24/16.
 */
public class TFIDFWeightedScore implements Comparable<TFIDFWeightedScore> {
    private String term;
    private Double score;
    private Double tfScore;
    private Double idfScore;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getIdfScore() {
        return idfScore;
    }

    public void setIdfScore(Double idfScore) {
        this.idfScore = idfScore;
    }

    public Double getTfScore() {
        return tfScore;
    }

    public void setTfScore(Double tfScore) {
        this.tfScore = tfScore;
    }

    public static Comparator<TFIDFWeightedScore> tfidfWeightedScoreComparator = new Comparator<TFIDFWeightedScore>(){

        @Override
        public int compare(TFIDFWeightedScore o1, TFIDFWeightedScore o2) {
            double d1 = o1.getScore();
            double d2 = o2.getScore();
            int value = 0;

            if(d1 > d2){ value = -1; }

            if(d1 < d2){ value = 1; }

            return value;
        }
    };

    @Override
    public int compareTo(TFIDFWeightedScore o) {
        return 0;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("term", this.getTerm());
        map.put("score", this.getScore());
        return map;
    }
}
