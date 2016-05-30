package contentclassification.domain;

import org.apache.commons.lang3.StringUtils;
import org.omg.PortableInterceptor.ServerRequestInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rsl_prod_005 on 3/30/16.
 */
public class AppUtils {
    private static final String REGEXP_WEBSITE =
            "(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?";
    private static final String COLOR_EXP = "\\bcol(our|or|OR|OUR)\\b((|\\s)(\\:|\\-)+(\\s|| ).\\w.*|(\\n).*)";

    private static List<String> getColorExp(){
        List<String> colorsExp = new LinkedList<>();
        colorsExp.add("\\bcol(our|or|OR|OUR)\\b(\\n).*");
        colorsExp.add("\\bcol(our|or|OR|OUR)\\b(| )(\\:|\\-)+(\\s| ).*");
        colorsExp.add("\\bcol(our|or|OR|OUR)\\b((| )(\\:|\\-)+(\\s| ).\\w.*|(\\n).*)");
        colorsExp.add("\\bcol(our|or|OR|OUR)\\b((|\\s)(\\:|\\-)+(\\s|| ).\\w.*|(\\n).*)");
        return colorsExp;
    }
    public static String formatUrl(String url){
        return url.replaceAll(" ", "%20").replaceAll("\\+", "%20").replaceAll("%3D", "=").replaceAll("%26", "&");
    }
    public static boolean isValidWebsite(String url){
        boolean isValid = false;
        if(StringUtils.isNotBlank(url)){
            try {
                URL urlObj = new URL(url);
                isValid = true;
            } catch (MalformedURLException e){

            }
        }
        return isValid;
    }

    public static boolean filterByPOS(String pos){
        boolean isAllowed = false;
        List<POSRESPONSES> notAllowed = new ArrayList<>();
        notAllowed.add(POSRESPONSES.VBZ);
        notAllowed.add(POSRESPONSES.IN);
        notAllowed.add(POSRESPONSES.RB);

        if(StringUtils.isNotBlank(pos)){
            POSRESPONSES posresponses = POSRESPONSES.fromString(pos);
            if(notAllowed.contains(posresponses)){
                isAllowed = true;
            }
        }
        return isAllowed;
    }

    public static List<String> getColorByRegEx(String content){
        List<String> text = new ArrayList<>();
        if(StringUtils.isNotBlank(content)) {
            content = cleanUpTextForRegEx(content);
            int flag = Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(COLOR_EXP, flag);
            Matcher matcher = pattern.matcher(content);
            while(matcher.find()){
                text.add(matcher.group());
            }
        }

        Set<String> cleanUp = new HashSet<>();
        cleanUp.addAll(text);
        text.clear();
        text.addAll(cleanUp);
        return text;
    }

    public static URL getUrl(String urlStr){
        URL url = null;
        try{
            url = new URL(urlStr);
        } catch (MalformedURLException ex){
            ex.printStackTrace();
        }
        return url;
    }

    public static String cleanUpTextForRegEx(String text){
        String cleanText = text;
        List<String> blacklist = new ArrayList<>();
        blacklist.add("\\b(s|S)elect\\sa\\scol(or|our)\\b");
        blacklist.add("\\b(c|C)hoose\\san\\soption\\b");
        blacklist.add("\\b(select|SELECT|SORRY|Sorry|not\\savailable)\\b.*");

        if(!blacklist.isEmpty()){
            for(String s : blacklist){
                cleanText = cleanText.replaceAll(".*"+s+".*(\\r?\\n|\\r)?", " ");
            }
        }

        List<String> spiltByNewline = Arrays.asList(cleanText.split("\\n"));
        StringBuilder stringBuilder = new StringBuilder();
        if(!spiltByNewline.isEmpty()){
            int x = 0;
            for(String s : spiltByNewline){
                if(StringUtils.isNotBlank(s)){
                    if(x < (spiltByNewline.size() - 1)) {
                        stringBuilder.append(s);
                        stringBuilder.append("\n");
                    } else {
                        stringBuilder.append(s);
                    }
                }
                x++;
            }
        }

        return stringBuilder.toString();
    }

    public static List<String> getColorsFromRegEx(List<String> potentialColors){
        List<String> colors = new ArrayList<>();
        if (potentialColors != null && !potentialColors.isEmpty()) {
            for(String s : potentialColors){
                String d = s.replaceAll("\\b(Color|Colour|COLOR|COLOUR|color|colour)\\b(\\s|\\:|\\s\\:\\s|\\:\\s||)\\b(\\:|)","");
                colors.add(d.trim().toUpperCase());
            }
        }

        List<String> breakUp = breakUp(colors, new String[]{",","/","AND", "and"});
        Set<String> cleanUp = new HashSet<>();
        cleanUp.addAll(colors);
        if(!breakUp.isEmpty()){
            cleanUp.clear();
            cleanUp.addAll(breakUp);
        }
        colors.clear();
        colors.addAll(cleanUp);

        /**
         * remove items in results set that contains criteria for breaking up.
         */
        if(!colors.isEmpty()){
            List<String> temp = new ArrayList<>();
            for(String c : colors){
                boolean isBrokenUp = Color.isBreakable(c.trim(), new String[]{",", "/", "AND", "and"});
                if(!isBrokenUp){
                    temp.add(c);
                }
            }

            if(!temp.isEmpty()){
                colors.clear();
                colors.addAll(temp);
            }
        }

        /**Check if color is validated or not. Validation here means that whether we have the said color in our
         * curated color data set.
        */
        if(!colors.isEmpty()) {
            for(String d : colors) {
                boolean isValidated = Color.isExisting(d.trim().toLowerCase());
                if (isValidated) {
                    System.out.println("Yes validated: " + d);
                } else {
                    System.out.println("Not validated: " + d);
                }
            }
        }

        return colors;
    }

    public static List<String> breakUp(List<String> hayStack, String... niddles){
        List<String> output = new ArrayList<>();
        if (niddles != null && niddles.length > 0) {
            for(String n : niddles) {
                if (!hayStack.isEmpty()) {
                    for (String h : hayStack) {
                        if(h.contains(n)){
                            List<String> g = Arrays.asList(h.split(n));
                            if(!g.isEmpty()) {
                                for(String f : g) {
                                    output.add(f.trim().toUpperCase());
                                }
                            }
                        } else {
                            output.add(h);
                        }
                    }
                }
            }
        }

        return output;
    }
}