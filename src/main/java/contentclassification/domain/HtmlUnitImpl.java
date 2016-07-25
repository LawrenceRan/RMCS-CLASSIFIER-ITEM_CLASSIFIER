package contentclassification.domain;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class HtmlUnitImpl {
    private static final Logger logger = LoggerFactory.getLogger(HtmlUnitImpl.class);
    private URL url;

    public static HtmlUnitImpl setUrl(URL url){
        return new HtmlUnitImpl(url);
    }
    private HtmlUnitImpl(URL url){
        this.url = url;
    }


    public String getText(){
        boolean enableJavascript = Boolean.parseBoolean(System.getProperty("enable.javascript"));
        boolean enableCss = Boolean.parseBoolean(System.getProperty("enable.css"));

        String text = null;
        WebClient client = new WebClient(BrowserVersion.FIREFOX_45);
        Set<Cookie> cookies = client.getCookies(this.url);

        CookieManager cookieManager = client.getCookieManager();
        if(cookies != null && !cookies.isEmpty()) {
            for(Cookie cookie : cookies) {
                cookieManager.addCookie(cookie);
            }
        }
        client.getOptions().setCssEnabled(enableCss);
        client.getOptions().setJavaScriptEnabled(enableJavascript);
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
        logger.info("About to process request for content as string.");
        boolean enableJavascript = Boolean.parseBoolean(System.getProperty("enable.javascript"));
        boolean enableCss = Boolean.parseBoolean(System.getProperty("enable.css"));

        String text = null;
        WebClient client = new WebClient(BrowserVersion.FIREFOX_45);
        Set<Cookie> cookies = client.getCookies(this.url);

        CookieManager cookieManager = client.getCookieManager();
        if(cookies != null && !cookies.isEmpty()) {
            for(Cookie cookie : cookies) {
                cookieManager.addCookie(cookie);
            }
        }

        client.getOptions().setCssEnabled(enableCss);
        client.getOptions().setJavaScriptEnabled(enableJavascript);
        client.setCookieManager(cookieManager);
        client.getCookieManager().setCookiesEnabled(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        client.getOptions().setUseInsecureSSL(true);

        try {
            WebRequest webRequest = new WebRequest(url, HttpMethod.GET);
            HtmlPage page = client.getPage(webRequest);
            text = page.getWebResponse().getContentAsString();
        } catch (Exception e){
            logger.debug("Error in getting content as string. Message: "+ e.getMessage());
        }

        logger.info("Done processing request for content as string.");
        return text;
    }

    public String parseHTMLText(String html, String urlStr){
        String text = null;
        if(StringUtils.isNotBlank(html) && StringUtils.isNotBlank(urlStr)){
            try {
                boolean enableJavascript = Boolean.parseBoolean(System.getProperty("enable.javascript"));
                boolean enableCss = Boolean.parseBoolean(System.getProperty("enable.css"));

                URL url = new URL(urlStr);
                StringWebResponse stringWebResponse = new StringWebResponse(html, url);
                WebClient client = new WebClient(BrowserVersion.FIREFOX_45);
                client.getOptions().setCssEnabled(false);
                client.getOptions().setJavaScriptEnabled(enableJavascript);
                client.getCookieManager().setCookiesEnabled(enableCss);
                client.getOptions().setThrowExceptionOnFailingStatusCode(true);
                client.getOptions().setUseInsecureSSL(true);

                if(enableJavascript) {
                    client.waitForBackgroundJavaScript(30 * 1000);
                }

                try {
                    HtmlPage page = HTMLParser.parseHtml(stringWebResponse, client.getCurrentWindow());
                    page.normalize();
                    text = page.asText();
                } catch (IOException e){
                    e.printStackTrace();
                }
            } catch(MalformedURLException e){
                e.printStackTrace();
            }
        }
        return text;
    }

    public String parseHTMLText(String html, String urlStr, boolean enableJavascript){
        String text = null;
        if(StringUtils.isNotBlank(html) && StringUtils.isNotBlank(urlStr)){
            try {
                URL url = new URL(urlStr);
                StringWebResponse stringWebResponse = new StringWebResponse(html, url);
                WebClient client = new WebClient(BrowserVersion.FIREFOX_45);
                client.getOptions().setCssEnabled(false);
                client.getOptions().setJavaScriptEnabled(enableJavascript);
                client.getCookieManager().setCookiesEnabled(true);
                client.getOptions().setThrowExceptionOnFailingStatusCode(true);
                client.getOptions().setUseInsecureSSL(true);
                try {
                    HtmlPage page = HTMLParser.parseHtml(stringWebResponse, client.getCurrentWindow());
                    page.normalize();
                    text = page.asText();
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
