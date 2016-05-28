package contentclassification.domain;

import java.util.Comparator;

/**
 * Created by rsl_prod_005 on 5/28/16.
 */
public class TotalTermToGroup implements Comparable<TotalTermToGroup> {
    private String term;
    private Integer termToGroupScore;
    private Double termFrequencyScore;
    private Double weightTotalScore;

    public TotalTermToGroup(){}
    public TotalTermToGroup(String term,
                            Integer termToGroupScore,
                            Double termFrequencyScore){
        this.term = term;
        this.termToGroupScore = termToGroupScore;
        this.termFrequencyScore = termFrequencyScore;
        this.weightTotalScore = getWeightedScore(termToGroupScore, termFrequencyScore);
    }

    private Double getWeightedScore(Integer termToGroupScore, Double termFrequencyScore){
        Double answer = 0D;
        Double percentile = (termFrequencyScore * 100);
        answer = termToGroupScore + percentile;
        return answer;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getTermToGroupScore() {
        return termToGroupScore;
    }

    public void setTermToGroupScore(Integer termToGroupScore) {
        this.termToGroupScore = termToGroupScore;
    }

    public Double getTermFrequencyScore() {
        return termFrequencyScore;
    }

    public void setTermFrequencyScore(Double termFrequencyScore) {
        this.termFrequencyScore = termFrequencyScore;
    }

    public Double getWeightTotalScore() {
        return weightTotalScore;
    }

    public void setWeightTotalScore(Double weightTotalScore) {
        this.weightTotalScore = weightTotalScore;
    }

    public static Double calculateWeightedScore(Integer termToGroupScore, Double termFrequencyScore){
        Double answer = 0D;
        Double percentile = (termFrequencyScore * 100);
        answer = termToGroupScore + percentile;
        return answer;
    }

    @Override
    public int compareTo(TotalTermToGroup o) {
        return 0;
    }

    public static Comparator<TotalTermToGroup> totalTermToGroupComparator = new Comparator<TotalTermToGroup>() {
        @Override
        public int compare(TotalTermToGroup o1, TotalTermToGroup o2) {
            Double d1 = o1.getWeightTotalScore();
            Double d2 = o2.getWeightTotalScore();

            int value;

            if(d1 >= d2){
                value = -1;
            } else {
                value = 1;
            }

            return value;
        }
    };
}
