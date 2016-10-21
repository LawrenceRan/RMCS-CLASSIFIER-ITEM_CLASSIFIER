package contentclassification.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 10/19/16.
 */
public class LanguageSymbols {
    private static Logger logger = LoggerFactory.getLogger(LanguageSymbols.class);

    private Languages languages;
    private List<String> symbols;

    public static LanguageSymbols loadLanguageSymbols(Languages languages){
        LanguageSymbols languageSymbol = null;
        if(languages != null){
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("en-punctuations.yml");
            if(inputStream != null){
                try {
                    Yaml yaml = new Yaml();
                    @SuppressWarnings("unchecked")
                    List<Map> symbolsMap = (List) yaml.load(inputStream);
                    if(symbolsMap != null && !symbolsMap.isEmpty()){
                        languageSymbol = new LanguageSymbols();
                        languageSymbol.setLanguages(languages);

                        List<String> symbols = null;
                        for(Map symbolMap : symbolsMap) {
                            if (symbolMap.containsKey("language")){
                                if (symbolMap.get("language").toString().equals(languages.toInitial())) {
                                    symbols = (List) symbolMap.get("symbols");
                                    logger.info("testing language symbols.");
                                }
                            }
                        }

                        if(symbols != null && !symbols.isEmpty()){
                            languageSymbol.setSymbols(symbols);
                        }
                    }
                } catch (Exception e){
                    logger.debug("Error occurred in loading language symbols from config. Language : "
                            + languages.toString() + " Message : "+ e.getMessage());
                } finally {
                    try {
                        inputStream.close();
                    } catch (Exception e){
                        logger.debug("Error occurred during input stream closing. Message : "+ e.getMessage());
                    }
                }
            }
        }
        return languageSymbol;
    }

    public static String[] removeSymbolsFromList(String[] tokens, LanguageSymbols languageSymbols){
        String[] updatedTokens = null;
        if(tokens != null && tokens.length > 0
                && languageSymbols.getSymbols() != null
                && !languageSymbols.getSymbols().isEmpty()){
            List<String> symbols = languageSymbols.getSymbols();
            List<String> nonSymbols = new ArrayList<>();
            for(String token : tokens){
                boolean isPresent = symbols.contains(token);
                if(!isPresent){ nonSymbols.add(token); }
            }

            if(!nonSymbols.isEmpty()){
                updatedTokens = nonSymbols.toArray(new String[nonSymbols.size()]);
            }
        }
        return updatedTokens;
    }

    public Languages getLanguages() {
        return languages;
    }

    public void setLanguages(Languages languages) {
        this.languages = languages;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
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
        LanguageSymbols rhs = (LanguageSymbols) obj;
        return new EqualsBuilder()
                .append(this.languages, rhs.languages)
                .append(this.symbols, rhs.symbols)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(languages)
                .append(symbols)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("languages", languages)
                .append("symbols", symbols)
                .toString();
    }
}
