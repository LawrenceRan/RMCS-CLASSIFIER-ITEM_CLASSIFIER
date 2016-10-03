package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public enum POSRESPONSES {
    CC("Coordinating conjunction"), CD("Cardinal number"), DT("Determiner"), EX("Existential there"),
    FW("Foreign word"), IN("Preposition or subordinating conjunction"), JJ("Adjective"), JJR("Adjective, comparative"),
    JJS("Adjective, superlative"), LS("List item marker"),MD("Modal"),NN("Noun singular or mass"),NNS("Noun, plural"),
    NNP("Proper noun, singular"),NNPS("Proper noun, plural"),PDT("Predeterminer"),POS("Possessive ending"),
    PRP("Personal pronoun"),PRP$("Possessive pronoun"),RB("Adverb"),RBR("Adverb, comparative"),RBS("Adverb, superlative"),
    RP("Particle"),SYM("Symbol"),TO("to"),UH("Interjection"),VB("Verb, base form"),VBD("Verb, past tense"),
    VBG("Verb, gerund or present participle"),VBN("Verb, past participle"),VBP("Verb, non­3rd person singular present"),
    VBZ("Verb, 3rd person singular present"),WDT("Wh­determiner"),WP("Wh­pronoun"),WP$("Possessive wh­pronoun"),
    WRB("Wh­adverb");

    private String initial;

    POSRESPONSES(String initial){
        this.initial = initial;
    }

    @Override
    public String toString(){
        return this.initial;
    }

    public static POSRESPONSES fromString(String initial){
        POSRESPONSES posresponses = null;
        List<POSRESPONSES> pos = Arrays.asList(POSRESPONSES.values());
        if(StringUtils.isNotBlank(initial)){
            for(POSRESPONSES p : pos){
                if(initial.equalsIgnoreCase(p.initial)){
                    posresponses = p;
                }
            }
        }
        return posresponses;
    }
}
