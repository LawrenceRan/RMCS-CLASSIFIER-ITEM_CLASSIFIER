package contentclassification.utilities;

import java.util.Arrays;
import java.util.List;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class BM25 {
    /** The constant k_1.*/
    private double k_1 = 1.2d;

    /** The constant k_3.*/
    private double k_3 = 8d;

    /** The parameter b.*/
    private double b;

    private String[] document;
    private String term;
    private List<String> arrayList;

    public BM25(String[] document, String term){
        this.document = document;
        this.term = term;
        this.arrayList = Arrays.asList(document);
        b = 0.75d;
    }

    public double tf(){
        double score = 0d;
        if(this.document != null && this.document.length > 0){
            for(String eachTerm : this.document){
                if(eachTerm.equalsIgnoreCase(this.term)){
                    score++;
                }
            }
        }
        return score/this.document.length;
    }

    public double idf(){
        double score = 0d;
        if(this.document != null && this.document.length > 0){
            for(String d : this.arrayList){
                if (d.equalsIgnoreCase(this.term)) {
                    score++;
                    break;
                }
            }
        }
        return Math.log(this.arrayList.size()/score);
    }

    public double tfIdfWeightScore(){
        double idf = idf();
        double tf = tf();
        return tf * idf;
    }

    /**
     * Returns the name of the model.
     * @return the name of the model
     */
    public final String getInfo() {
        return "BM25 >> b="+b +", k_1=" + k_1 +", k_3=" + k_3;
    }

    /**
     * Sets the b parameter to BM25 ranking formula
     * @param b the b parameter value to use.
     */
    public void setParameter(double b) {
        this.b = b;
    }


    /**
     * Returns the b parameter to the BM25 ranking formula as set by setParameter()
     */
    public double getParameter() {
        return this.b;
    }

    /**
     * Uses BM25 to compute a weight for a term in a document.
     * @param tf The term frequency in the document
     * @param numberOfDocuments number of documents
     * @param docLength the document's length
     * @param averageDocumentLength average document length
     * @return the score assigned to a document with the given
     *         tf and docLength, and other preset parameters
     */
    public final double score(double tf,
                              double numberOfDocuments,
                              double docLength,
                              double averageDocumentLength,
                              double queryFrequency,
                              double documentFrequency) {

        double K = k_1 * ((1 - b) + ((b * docLength) / averageDocumentLength));
        double weight = ( ((k_1 + 1d) * tf) / (K + tf) );	//first part
        weight = weight * ( ((k_3 + 1) * queryFrequency) / (k_3 + queryFrequency) );	//second part

        // multiply the weight with idf
        double idf = weight * Math.log((numberOfDocuments - documentFrequency + 0.5d) / (documentFrequency + 0.5d));
        return idf;
    }
}
