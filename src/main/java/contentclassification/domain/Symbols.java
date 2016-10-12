package contentclassification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsl_prod_005 on 8/4/16.
 */
public class Symbols {
    private static final Logger logger = LoggerFactory.getLogger(Symbols.class);
    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Symbols)) return false;

        Symbols that = (Symbols) object;

        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return symbol != null ? symbol.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ListOfSymbols{" +
                "symbol='" + symbol + '\'' +
                '}';
    }

    public static List<Symbols> loadSymbols(){
        List<Symbols> list = new ArrayList<>();
        ClassLoader classLoader = Symbols.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("en-list-of-symbols.yml");
        if(inputStream != null){
            Yaml yaml = new Yaml();
            try {
                List<String> stringList = (List<String>) yaml.load(inputStream);
                if (stringList != null && !stringList.isEmpty()) {
                    for (String s : stringList) {
                        Symbols symbols = new Symbols();
                        symbols.setSymbol(s);
                        list.add(symbols);
                    }
                }
            } catch (Exception e){
                logger.debug("Error in parsing yaml file. Message : "+ e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }
        return list;
    }

    public static List<String> loadSymbolsAsString(){
        List<String> strings = new ArrayList<>();
        List<Symbols> symbolsList = loadSymbols();
        if(!symbolsList.isEmpty()){
            for(Symbols symbols : symbolsList){
                strings.add(symbols.getSymbol());
            }
        }
        return strings;
    }
}
