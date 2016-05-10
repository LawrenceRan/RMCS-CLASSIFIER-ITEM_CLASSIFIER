package contentclassification.domain;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class RomeImpl {
    private String url;

    public static RomeImpl setUrl(String url){
        return new RomeImpl(url);
    }

    private RomeImpl(String url){
        this.url = url;
    }

    public List<Map> getItems(){
        List<Map> items = new ArrayList<Map>();
        try{
            URL urlObj = new URL(this.url);

            SyndFeedInput syndFeedInput = new SyndFeedInput();
            SyndFeed syndFeed = syndFeedInput.build(new XmlReader(urlObj));

            if(syndFeed != null){
                List<SyndEntry> syndEntries = syndFeed.getEntries();
                if(!syndEntries.isEmpty()){
                    for(SyndEntry syndEntry : syndEntries){
                        Map<String, Object> item = new HashMap<>();
                        String title = syndEntry.getTitle();
                        String description = syndEntry.getDescription().getValue();
                        String uri = syndEntry.getUri();

                        item.put("title", title);
                        item.put("description", description);
                        item.put("uri", uri);

                        items.add(item);
                    }
                }
            }
        } catch(Exception e){
            System.out.println("Error: "+ e.getMessage());
        }
        return items;
    }
}
