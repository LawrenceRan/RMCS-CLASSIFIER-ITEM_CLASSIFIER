package contentclassification.domain;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.PearsonCorrelationCoefficient;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class MLImpl {
    private double[] a , b;

    public MLImpl(double[] a, double[] b){
        this.a = a;
        this.b = b;
    }

    public Object getDistance(){
        PearsonCorrelationCoefficient pearsonCorrelationCoefficient = new PearsonCorrelationCoefficient();
        Instance a1 = createInstance(a);
        Instance b1 = createInstance(b);
        return pearsonCorrelationCoefficient.measure(a1, b1);
    }

    private Instance createInstance(double[] values){

        Instance i = new DenseInstance(values);
        return i;
    }
}
