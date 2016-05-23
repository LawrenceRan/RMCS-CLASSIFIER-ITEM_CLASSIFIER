package contentclassification.model;



import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
@NodeEntity(label = "Color")
public class Color {
    @GraphId
    private Long id;
    private String name;
    private Date createdOn;

    @Relationship(type = "IS_A", direction = Relationship.OUTGOING)
    private Domain domain;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
//
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Map toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.getId());
        map.put("name", this.getName());
        map.put("createdOn", this.getCreatedOn());
        return map;
    }


}
