package contentclassification.domain;

import com.hp.hpl.jena.rdf.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class JenaImpl {
    private static final Logger logger = LoggerFactory.getLogger(JenaImpl.class);

    private static final String BASE_URL = "http://dbpedia.org/data/";
    private String query;
    private String response;
    private Map<String, Object> responseMap;

    public static JenaImpl setQuery(String query){
        return new JenaImpl(query);
    }

    private JenaImpl(String query){
        this.query = query.substring(0, 1).toUpperCase() + query.substring(1);
    }

    private String requestUrl(){
        return BASE_URL + "" + query + ".rdf";
    }

    public List<Map> getResponse(){
        List<Map> output = new ArrayList<>();
        String requestUrlObj = requestUrl();
        URL url = null;
        try {
            url = new URL(requestUrlObj);
        } catch(MalformedURLException e){
            logger.debug("Malformed URL exception: "+ e.getMessage());
        }

        if(url != null){
            InputStream inputStream = null;
            try {
                inputStream = url.openStream();
            } catch (IOException e){
                logger.debug("IO Exception. URL:"+ requestUrlObj +" Message : "+ e.getMessage() + "");
            }

            if(inputStream != null){
                Model model = ModelFactory.createDefaultModel();
                model.read(inputStream, null);

                if(!model.isEmpty()) {
                    Set<String> urns = new HashSet<>();
                    StmtIterator stmtIterator = model.listStatements();
                    NodeIterator nodeIterator = model.listObjects();

                    List<Map> types = new ArrayList<>();
                    List<Map> comments = new ArrayList<>();
                    List<Map> labels = new ArrayList<>();
                    List<Map> subjects = new ArrayList<>();
                    List<Map> abstracts = new ArrayList<>();

                    while (stmtIterator.hasNext()){
                        Statement statement = stmtIterator.nextStatement();
                        Property predicate = statement.getPredicate();
                        Resource resource = statement.getSubject();
                        RDFNode rdfNode = statement.getObject();


                        String type = null;
                        String comment = null;
                        String label = null;
                        String subject = null;
                        String abstractReview = null;

                        //logger.info("Predicates : " +predicate.getLocalName());

                        if(predicate.getLocalName().equalsIgnoreCase(RDFProperties.TYPE.toString())) {
                            type = rdfNode.toString();
                            types.add(getMap(resource.toString(), type));
                        }

                        if(predicate.getLocalName().equalsIgnoreCase(RDFProperties.COMMENT.toString())){
                            comment = rdfNode.toString();
                            comments.add(getMap(resource.toString(), comment));

                        }

                        if(predicate.getLocalName().equalsIgnoreCase(RDFProperties.LABEL.toString())){
                            label = rdfNode.toString();
                            labels.add(getMap(resource.toString(), label));
                        }

                        if(predicate.getLocalName().equalsIgnoreCase(RDFProperties.SUBJECT.toString())){
                            subject = rdfNode.toString();
                            subjects.add(getMap(resource.toString(), subject));
                        }

                        if(predicate.getLocalName().equalsIgnoreCase(RDFProperties.ABSTRACT.toString())){
                            abstractReview = rdfNode.toString();
                            if(StringUtils.isNotBlank(abstractReview)) {
                                abstracts.add(getMap(resource.toString(), abstractReview));
                            }

                        }
//                        if(resource.isURIResource() && StringUtils.isBlank(resource.getLocalName())) {
//                            logger.info("Predicate local name: "+ predicate.getLocalName());
//                            urns.add(resource.toString());
//                        }

                        urns.add(resource.toString());
                        //logger.info("Resource: " + resource.toString() );
                    }

                    List<String> cSubjects = new ArrayList<>();
                    List<String> cTypes = new ArrayList<>();

                    if(!urns.isEmpty()){
                        Map<String, Object> responseObject = new HashMap<>();
                        for(String urn : urns){
                            if(!types.isEmpty()) {
                                for(Map<String, String> typeMap : types) {
                                    if (typeMap.containsKey(urn)) {
                                        cTypes.add(cleanUpType(typeMap.get(urn)));
                                    }
                                }
                            }

                            if(!comments.isEmpty()) {
                                for(Map<String, String> commentMap : comments) {
                                    if (commentMap.containsKey(urn)) {
                                        boolean checkLanguage = checkLanguage("@en", commentMap);
                                        if(checkLanguage) {
                                            responseObject.put(RDFProperties.COMMENT.toString(), commentMap.get(urn));
                                        }
                                    }
                                }
                            }

                            if(!labels.isEmpty()){
                                for(Map<String, String> labelMap : labels){
                                    if(labelMap.containsKey(urn)){
                                        boolean checkLanguage = checkLanguage("@en", labelMap);
                                        if(checkLanguage) {
                                            responseObject.put(RDFProperties.LABEL.toString(), labelMap.get(urn));
                                        }
                                    }
                                }
                            }


                            if (!subjects.isEmpty()){
                                for(Map<String, String> subjectMap : subjects) {
                                    if (subjectMap.containsKey(urn)) {
                                        cSubjects.add(cleanUpSubject(subjectMap.get(urn)));
                                    }
                                }
                            }

                            if(!abstracts.isEmpty()){
                                for(Map<String, String> abstractMap : abstracts){
                                    if(abstractMap.get(urn) != null) {
                                        boolean checkLanguage = checkLanguage("@en", abstractMap);
                                        if(checkLanguage) {
                                            responseObject.put(RDFProperties.ABSTRACT.toString(),
                                                    abstractMap.get(urn));
                                        }
                                    }
                                }
                            }
                        }

                        responseObject.put(RDFProperties.TYPE.toString(), cTypes);
                        responseObject.put(RDFProperties.SUBJECT.toString(), cSubjects);

                        output.add(responseObject);
                    }
//                    ResIterator resIterator = contentclassification.model.listSubjects();
//                    StmtIterator stmtIterator = contentclassification.model.listStatements();
//                    if(stmtIterator != null){
//                        while (stmtIterator.hasNext()){
//                            Statement statement = stmtIterator.next();
//                            Property property = statement.getPredicate();
//                            logger.info("Property: "+ property.toString());
//                            logger.info("Statement: "+ statement.toString());
////                            if(property.getLocalName().equals(RDFProperties.TYPE.toString())) {
////                                Resource subject = statement.getSubject();
////                                Statement type = subject.getProperty(RDF.type);
////
////                                logger.info("Type: " + type.getObject().asNode());
//////                                RDFNode rdfNode = statement.getObject();
//////                                if(rdfNode != null){
//////                                    if(rdfNode.isResource()) {
//////                                        Object value = rdfNode
//////                                        logger.info("RDF node: " + rdfNode.getModel() + " " + rdfNode.asResource().listProperties().toString());
//////                                    }
//////                                }
//////                                logger.info("Language: " + property.getLocalName() + " : " + property.getNameSpace()
//////                                + " : "+ property.getOrdinal());
////                            }
//                        }
//                    }
//                    //Property comment = contentclassification.model.getProperty("comment");
//                    logger.info("Comment" + resIterator.toString());
                }

                try {
                    inputStream.close();
                } catch (Exception e){
                    logger.warn("Error in closing file. Message : "+ e.getMessage());
                }
            }
        }

        return output;
    }

    private static Map<String, Object> getMap(String key, Object value){
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static String cleanUpType(String typeUrl){
        String type = typeUrl;
        if(StringUtils.isNotBlank(typeUrl)){
            if(typeUrl.contains("#")){
                type = typeUrl.substring(typeUrl.lastIndexOf("#") + 1);
            }

            if(type.contains("/")){
                type = type.substring(type.lastIndexOf("/") + 1);
            }
        }
        return type;
    }

    private static String cleanUpSubject(String str){
        String output = null;
        if(StringUtils.isNotBlank(str)){
            if(str.contains(":")){
                output = str.substring(str.lastIndexOf(":") + 1);
            }
        }
        return output;
    }

    private static boolean checkLanguage(String identifier, Map<String, String> map){
        boolean isChecked = false;
        if(map != null && !map.isEmpty()){
            for(Map.Entry<String, String> m : map.entrySet()){
                if(m.getValue().contains(identifier)){
                    isChecked = true;
                }
            }
        }
        return isChecked;
    }

    @Override
    public String toString(){
        return this.response;
    }
}
