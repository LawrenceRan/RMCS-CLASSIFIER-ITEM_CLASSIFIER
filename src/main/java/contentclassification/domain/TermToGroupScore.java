package contentclassification.domain;


import com.github.jsonldjava.utils.Obj;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/27/16.
 * This class is a term to group score data structure, it represent the score of
 * a given term in content grouped areas.
 */
public class TermToGroupScore {
    private String term;
    private ContentAreaGroupings group;
    private Integer score;

    public TermToGroupScore(){

    }

    public TermToGroupScore(String term, ContentAreaGroupings group, Integer score){
        this.term = term;
        this.group = group;
        this.score = score;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public ContentAreaGroupings getGroup() {
        return group;
    }

    public void setGroup(ContentAreaGroupings group) {
        this.group = group;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString(){
        return "[ term:"+ this.getTerm() + " group:"+ this.getGroup().toString() + " score:"+ this.getScore() +" ]";
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("term", this.getTerm());
        map.put("group", this.getGroup().toString());
        map.put("score", this.getScore());
        return map;
    }
}
