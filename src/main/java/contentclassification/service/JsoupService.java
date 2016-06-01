package contentclassification.service;

import contentclassification.domain.AppUtils;
import contentclassification.domain.HtmlUnitImpl;
import contentclassification.domain.JsoupImpl;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/6/16.
 */
@Service
public class JsoupService {
    private static final Logger logger = LoggerFactory.getLogger(JsoupService.class);

    public String getTitle(String url){
        String title = null;
        try {
            JsoupImpl jsoup = JsoupImpl.setUrl(url);
            title = jsoup.getTitle();
        } catch (Exception e){
            logger.debug("Error in getting title. Message:" + e.getMessage());
        }
        return title;
    }

    public Document getDocument(String url){
        Document document = null;
        try{
            JsoupImpl jsoup = JsoupImpl.setUrl(url);
            document = jsoup.getDocument();
        } catch (Exception e){
            logger.debug("Error in getting document. Message: "+ e.getMessage());
        }
        return document;
    }

    public List<String> metas(String url){
        List<String> metas = null;
        try{
            JsoupImpl jsoup = JsoupImpl.setUrl(url);
            metas = jsoup.getMeta();
        } catch (Exception e){
            logger.debug("Error in getting meta details. Message: "+ e.getMessage());
        }
        return metas;
    }

    public List<String> links(String url){
        List<String> links = null;
        try{
            JsoupImpl jsoup = JsoupImpl.setUrl(url);
            links = jsoup.getLinks();
        } catch (Exception e){
            logger.debug("Error in getting links. Message: "+ e.getMessage());
        }
        return links;
    }

    public String bodyText(String url){
        String text = null;
        try{
            JsoupImpl jsoup = JsoupImpl.setUrl(url);
            text = jsoup.getBody();
        } catch (Exception e){
            logger.debug("Error in getting body. Message: "+ e.getMessage());
        }
        return text;
    }

    public String bodyTextByHtmlUnit(String url){
        String text = null;
        try {
            HtmlUnitImpl htmlUnit = HtmlUnitImpl.setUrl(AppUtils.getUrl(url));
            text = htmlUnit.getText();
        } catch (Exception e){
            logger.debug("Error in getting text using. Message: "+ e.getMessage());
        }
        return text;
    }

    public String parseHtmlText(String html, String url){
        String text = null;
        try{
            HtmlUnitImpl htmlUnit = HtmlUnitImpl.setUrl(AppUtils.getUrl(url));
            text = htmlUnit.parseHTMLText(html, url);
        } catch (Exception ex){
            logger.debug("Error in getting text by parsing html text: "+ ex.getMessage());
        }
        return text;
    }

    public String getContentAsString(String url){
        String text = null;
        try{
            HtmlUnitImpl htmlUnit = HtmlUnitImpl.setUrl(AppUtils.getUrl(url));
            text = htmlUnit.getContentAsString();
        } catch (Exception e){
            logger.debug("Error in getting content string. Message: "+ e.getMessage());
        }
        return text;
    }

    public List<Map> getLinksUrlAndValue(String url){
        List<Map> links = null;
        try{
            JsoupImpl jsoup = JsoupImpl.setUrl(url);
            links = jsoup.getLinksUrlAndValue();
        } catch (Exception e){
            logger.debug("Error in getting links. Message: "+ e.getMessage());
        }
        return links;
    }
}
