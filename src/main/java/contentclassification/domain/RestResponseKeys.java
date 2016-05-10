package contentclassification.domain;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
public enum RestResponseKeys {
    DATA("data"), MESSAGE("message");

    private final String key;

    RestResponseKeys(String key){
        this.key = key;
    }

    @Override
    public String toString(){
        return this.key;
    }
}
