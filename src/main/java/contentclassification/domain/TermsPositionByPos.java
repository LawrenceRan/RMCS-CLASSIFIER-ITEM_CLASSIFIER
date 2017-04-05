package contentclassification.domain;

import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 4/3/17.
 */
public class TermsPositionByPos {
    private static Logger logger = LoggerFactory.getLogger(TermsPositionByPos.class);

    private Map<String, Object> termsToPos;
    private Integer totalLength;
    private ListMultimap<String, Integer> positionToTerm;
    private List<String> posTags;
    private Integer foundPosTag;
    private LinkedList<String> orderedTokens;

    public TermsPositionByPos(Map<String, Object> termsToPos, Integer totalLength, ListMultimap<String, Integer> positionToTerm,
                              List<String> posTags) {
        this.termsToPos = termsToPos;
        this.totalLength = totalLength;
        this.positionToTerm = positionToTerm;
        this.posTags = posTags;
    }

    public Map<String, Object> getTermsToPos() {
        return termsToPos;
    }

    public void setTermsToPos(Map<String, Object> termsToPos) {
        this.termsToPos = termsToPos;
    }

    public Integer getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(Integer totalLength) {
        this.totalLength = totalLength;
    }

    public ListMultimap<String, Integer> getPositionToTerm() {
        return positionToTerm;
    }

    public void setPositionToTerm(ListMultimap<String, Integer> positionToTerm) {
        this.positionToTerm = positionToTerm;
    }

    public List<String> getPosTags() {
        return posTags;
    }

    public void setPosTags(List<String> posTags) {
        this.posTags = posTags;
    }

    public Integer getFoundPosTag() {
        return foundPosTag;
    }

    public void setFoundPosTag(Integer foundPosTag) {
        this.foundPosTag = foundPosTag;
    }

    public LinkedList<String> getOrderedTokens() {
        return orderedTokens;
    }

    public void setOrderedTokens(LinkedList<String> orderedTokens) {
        this.orderedTokens = orderedTokens;
    }

    public List<String> getTermByPos(Map<String, Object> termsToPos){
        List<String> terms = null;
        POSRESPONSES incomingPosresponses = (this.foundPosTag != null) ? POSRESPONSES.fromOrdinal(this.foundPosTag) : null;
        if(termsToPos != null && !termsToPos.isEmpty()){
            terms = new ArrayList<>();
            for(Map.Entry entry : termsToPos.entrySet()){
                Object object = entry.getValue();
                if(object != null && (object instanceof List)){
                    @SuppressWarnings("unchecked")
                    List<Map> mapList = (List<Map>) object;
                    if(!mapList.isEmpty()){
                        for(Map map : mapList){
                            Integer typeInitial = null;
                            if(map.containsKey("pos")){
                                Object typeInitialObj = map.get("pos");
                                if(typeInitialObj != null && (typeInitialObj instanceof Integer)){
                                    typeInitial = (Integer) typeInitialObj;
                                    POSRESPONSES posresponses = POSRESPONSES.fromOrdinal(typeInitial);
                                    if(posresponses != null && incomingPosresponses != null){
                                        if(incomingPosresponses.equals(posresponses)){
                                            terms.add(entry.getKey().toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return terms;
    }

    public String getPhrase(String term, ListMultimap<String, Integer> positionToTerm){
        String phrase = null;
        if(!positionToTerm.isEmpty()){
            if(positionToTerm.containsKey(term)){
                Integer position = positionToTerm.get(term).get(0);
                if(position != null){
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int x = (position + 1); x < getTotalLength(); x++) {
                        String value = getOrderedTokens().get(x);
                        stringBuilder.append(value);
                        stringBuilder.append(" ");
                    }
                    phrase = stringBuilder.toString().trim();
                }
            }
        }
        return phrase;
    }
}
