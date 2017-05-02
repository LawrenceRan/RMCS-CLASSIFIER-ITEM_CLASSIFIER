package contentclassification.config;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.Resource;

/**
 * Created by rsl_prod_005 on 3/29/17.
 */
@Configuration
@EnableElasticsearchRepositories
public class ElasticSearchConfig {
    private static Logger logger = LoggerFactory.getLogger(ElasticSearchConfig.class);

    @Resource
    private Environment environment;

    @Bean
    public Client client(){
        String host = (StringUtils.isNotBlank(environment.getProperty("elasticsearch.host")))
                ? environment.getProperty("elasticsearch.host") : "localhost";

        Integer port = (StringUtils.isNotBlank(environment.getProperty("elasticsearch.port")))
                ? Integer.parseInt(environment.getProperty("elasticsearch.port")) : 9300;

        logger.info("About to connect to elastic search server. Host : "+ host + " Port : "+ port);

        TransportClient transportClient = null;
        try {
            transportClient = new TransportClient();
            TransportAddress transportAddress = new InetSocketTransportAddress(host, port);
            transportClient.addTransportAddress(transportAddress);

            logger.info("Done connection to elastic search server. Client bean loaded.");
        } catch (Exception e){
            logger.warn("Error in setting up transport client for elastic search. Message : "+ e.getMessage());
        }
        return transportClient;
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations(){
        return new ElasticsearchTemplate(client());
    }


}
