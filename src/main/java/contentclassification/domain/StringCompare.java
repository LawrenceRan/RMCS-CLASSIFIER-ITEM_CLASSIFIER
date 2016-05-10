package contentclassification.domain;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class StringCompare {
    public static double compareStrings(String str1, String str2) {
        ArrayList pairs1 = wordLetterPairs(str1.toUpperCase());
        ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());
        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i = 0; i < pairs1.size(); i++) {
            Object pair1 = pairs1.get(i);
            for (int j = 0; j < pairs2.size(); j++) {
                Object pair2 = pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }
            }
        }
        return 2.0D * intersection / union;
    }

    private static ArrayList wordLetterPairs(String str) {
        ArrayList allPairs = new ArrayList();

        String[] words = str.split("s");
        for (int w = 0; w < words.length; w++) {
            String[] pairsInWord = letterPairs(words[w]);
            allPairs.addAll(Arrays.asList(pairsInWord));
        }
        return allPairs;
    }

    private static String[] letterPairs(String str) {
        int numPairs = str.length() - 1;
        String[] pairs = new String[numPairs];
        for (int i = 0; i < numPairs; i++) {
            pairs[i] = str.substring(i, i + 2);
        }
        return pairs;
    }
}
