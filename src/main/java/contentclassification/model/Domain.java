package contentclassification.model;


import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.*;

/**
 * Created by rsl_prod_005 on 5/20/16.
 */
@NodeEntity(label = "Domain")
public class Domain {
    @GraphId
    private Long id;
    private String name;
    private Date createdOn;

    @Relationship(type = "IS_A", direction = Relationship.INCOMING)
    private Set<Color> colorSet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Set<Color> getColorSet() {
        return colorSet;
    }

    public void setColorSet(Set<Color> colorSet) {
        this.colorSet = colorSet;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.getId());
        map.put("name", this.getName());
        map.put("createdOn", this.getCreatedOn());
        return map;
    }
}
