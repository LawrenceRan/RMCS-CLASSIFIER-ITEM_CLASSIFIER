package contentclassification.domain;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class HtmlUnitImpl {
    private URL url;

    public static HtmlUnitImpl setUrl(URL url){
        return new HtmlUnitImpl(url);
    }
    private HtmlUnitImpl(URL url){
        this.url = url;
    }


    public String getText(){
        String text = null;
        WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        client.getCookieManager().setCookiesEnabled(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        client.getOptions().setUseInsecureSSL(true);

        try {
            WebRequest webRequest = new WebRequest(url, HttpMethod.GET);
            HtmlPage page = client.getPage(webRequest);
            text = page.asText();
        } catch (Exception e){
            e.printStackTrace();
        }
        return text;
    }
}
