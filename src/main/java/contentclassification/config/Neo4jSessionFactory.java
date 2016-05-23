package contentclassification.config;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
public class Neo4jSessionFactory {

    private final static SessionFactory sessionFactory = new SessionFactory("base.domain");
    private static Neo4jSessionFactory factory = new Neo4jSessionFactory();

    public static Neo4jSessionFactory getInstance(){
        return factory;
    }

    private Neo4jSessionFactory(){}

    @Bean
    public Session getNeo4jSession() {
        Configuration configuration = new Configuration();
        configuration.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
                .setEncryptionLevel("NONE")
                .setTrustStrategy("TRUST_ON_FIRST_USE")
                .setCredentials("ItemClassification", "o0aiVJy5kktITAOdPKjQ")
                .setURI("http://itemclassification.sb04.stations.graphenedb.com:24789");

        return  new SessionFactory(configuration, "contentclassification.model")
                .openSession();
    }
}
