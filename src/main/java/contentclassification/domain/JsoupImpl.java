package contentclassification.domain;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by rsl_prod_005 on 3/30/16.
 */
public class JsoupImpl {
    private Logger logger = LoggerFactory.getLogger(JsoupImpl.class);

    private static final String USER_AGENT_VALUE ="Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0";
    private static final String USER_AGENT_KEY = "User-Agent";

    private String url;


    public static JsoupImpl setUrl(String url){
        return new JsoupImpl(AppUtils.formatUrl(url));
    }

    private JsoupImpl(String url){
        this.url = url;
    }

    public Document getDocument() throws IOException {
        Document document = null;
        try {
            document = Jsoup.connect(this.url)
                    .header(USER_AGENT_KEY, USER_AGENT_VALUE)
                    .timeout(5 * 1000)
                    .get();
        } catch (Exception e){
            logger.debug("Error in document for this url: "+ this.url +", Message: "+ e.getMessage());
        }
        return document;
    }

    public String getTitle() throws IOException{
        String title = null;
        try {
            Document document = getDocument();
            title = document.title();
        } catch(Exception e){
            logger.debug("Error in getting title: %s", e.getMessage());
        }
        return title;
    }

    public List<String> getLinks() throws IOException {
        List<String> links = new ArrayList<String>();
        try {
            Document document = getDocument();
            Elements elementLinks = document.select("a[href]");
            if (!elementLinks.isEmpty()) {
                for (Element link : elementLinks) {
                    links.add(link.attr("abs:href"));
                }
            }
        } catch (Exception e){
            logger.debug("Error in getting URLs : %s", e.getMessage());
        }
        return links;
    }

    public List<Map> getLinksUrlAndValue() throws IOException {
        List<Map> links = new ArrayList<>();
        try {
            Document document = getDocument();
            Elements elementLinks = document.select("a[href]");
            if (!elementLinks.isEmpty()) {
                for (Element link : elementLinks) {
                    Map<String, String> m = new HashMap<>();
                    m.put("value", link.text());
                    m.put("link", link.attr("abs:href"));
                    links.add(m);
                }
            }
        } catch (Exception e){
            logger.debug("Error in getting URLs : %s", e.getMessage());
        }
        return links;
    }

    public List<String> getMeta() throws IOException{
        List<String> meta = new ArrayList<>();
        try {
            Document document = getDocument();
            if (document != null) {
                Elements elements = document.select("meta");
                if (elements != null && !elements.isEmpty()) {
                    for (Element element : elements) {
                        meta.add(element.toString());
                    }
                }
            }
        } catch (Exception e){
            logger.debug("Error in getting site meta data : %s", e.getMessage());
        }
        return meta;
    }

    public String getBody() throws IOException {
        String body = null;
        try{
            Document document = getDocument();
            document.normalise();

            Element element = document.body();
            if(element != null){
                body = element.text();
            }
        } catch (Exception e){
            logger.debug("Error in getting site body data : %s", e.getMessage());
        }
        return body;
    }

    public static Document parseHtml(String html) throws IOException {
        Document document = null;
        try {
            document = Jsoup.parse(html);
            document.normalise();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return document;
    }

    public List<String> getAllTags() throws IOException {
        List<String> tags = new ArrayList<>();
        try{
            Document document = getDocument();
            if(document != null){
                Elements elements = document.body().getAllElements();
                Iterator<Element> elementIterator = elements.iterator();
                while (elementIterator.hasNext()){
                    Element element = elementIterator.next();
                    tags.add(element.html());
                }
            }
        } catch (Exception e){
            logger.debug("Error in: "+ e.getMessage());
        }
        return tags;
    }
}
