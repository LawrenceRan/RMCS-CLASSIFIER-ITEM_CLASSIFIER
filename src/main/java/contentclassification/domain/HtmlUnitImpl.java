package contentclassification.domain;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
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

    public String getContentAsString(){
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
            text = page.getWebResponse().getContentAsString();
        } catch (Exception e){
            e.printStackTrace();
        }
        return text;
    }

    public String parseHTMLText(String html, String urlStr){
        String text = null;
        if(StringUtils.isNotBlank(html) && StringUtils.isNotBlank(urlStr)){
            try {
                URL url = new URL(urlStr);
                StringWebResponse stringWebResponse = new StringWebResponse(html, url);
                WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
                client.getOptions().setCssEnabled(false);
                client.getOptions().setJavaScriptEnabled(false);
                client.getCookieManager().setCookiesEnabled(true);
                client.getOptions().setThrowExceptionOnFailingStatusCode(true);
                client.getOptions().setUseInsecureSSL(true);
                try {
                    HtmlPage page = HTMLParser.parseHtml(stringWebResponse, client.getCurrentWindow());
                    text = page.getWebResponse().getContentAsString();
                } catch (IOException e){
                    e.printStackTrace();
                }
            } catch(MalformedURLException e){
                e.printStackTrace();
            }
        }
        return text;
    }
}
