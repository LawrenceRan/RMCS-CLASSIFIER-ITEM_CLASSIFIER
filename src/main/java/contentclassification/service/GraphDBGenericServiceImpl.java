package contentclassification.service;

import contentclassification.config.Neo4jSessionFactory;
import org.neo4j.driver.v1.types.Entity;
import org.neo4j.ogm.session.Session;

/**
 * Created by rsl_prod_005 on 5/23/16.
 */
public abstract class GraphDBGenericServiceImpl<T> implements GraphDBService<T> {
    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;

    private Session session = null;

    @Override
    public Iterable<T> findAll() {
        session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public T find(Long id) {
        session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public void delete(Long id) {
        session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        session.delete(session.load(getEntityType(), id));
    }

    @Override
    public T createOrUpdate(T object) {
        session = Neo4jSessionFactory.getInstance().getNeo4jSession();
        session.save(object, DEPTH_ENTITY);
        return find(((Entity) object).id());
    }

    public abstract Class<T> getEntityType();
}
