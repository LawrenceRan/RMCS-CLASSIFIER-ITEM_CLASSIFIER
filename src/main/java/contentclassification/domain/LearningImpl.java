package contentclassification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import contentclassification.domain.Languages;
import contentclassification.controller.LearningResponseKeys;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class LearningImpl {
    private static final Logger logger = LoggerFactory.getLogger(LearningImpl.class);
    private String query;

    public static LearningImpl setQuery(String query){
        return new LearningImpl(query);
    }

    private LearningImpl(String query){
        this.query = query;
    }

    private List<String> words(){
        List<String> words = new ArrayList<>();
        Set<String> cleanUpContainer = new HashSet<>();
        List<String> incomingWords = Arrays.asList(this.query.trim().toLowerCase()
                .replace(" - ", " ")
                .replace(" | ", " ")
                .split(" "));
        cleanUpContainer.addAll(incomingWords);
        words.addAll(cleanUpContainer);
        return words;
    }

    public Map<String, Object> updateKnowledgeBase(){
        Map<String, Object> update = new HashMap<>();
        List<String> words = words();
        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream("en-pos-maxent.bin");
            //inputStream = new FileInputStream("en-token.bin");
            POSModel posModel = new POSModel(inputStream);
            POSTaggerME taggerME = new POSTaggerME(posModel);

            //String[] query = this.query.split(" - ");
            String[] query = null;

            if(words != null && !words.isEmpty()){
                query = new String[words.size()];
                int x = 0;
                for(String s : words){
                    query[x] = s;
                    x++;
                }
            }


            //Using tokenizers instead
            //TokenizerModel tokenizerModel = new TokenizerModel(inputStream);
            //Tokenizer tokenizer = new TokenizerME(tokenizerModel);

            ///String[] tagsTokenized = tokenizer.tokenize(this.query);
            String[] tags = taggerME.tag(query);

            Map<String, Object> tagResponses = new HashMap<>();
            if(tags != null && tags.length > 0){
                int x = 0;
                for(String tag : tags){
                    if(StringUtils.isNotBlank(tag)) {
                        if(!tag.equalsIgnoreCase(":") && !tag.equalsIgnoreCase(".")) {
                            POSRESPONSES p = POSRESPONSES.valueOf(tag);
                            logger.info("Query: "+  query[x] +" Tag: " + tag + " f: "+ p);
                            tagResponses.put(query[x], p.toString());
                        }
                    }
                    x++;
                }
            }
            update.put("Parts-Of-Speech", tagResponses);

            //String externalResource = find("r");
        } catch (IOException io){
            logger.debug("Error: "+ io.getMessage());
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e){
                    logger.debug("Error: "+ e.getMessage());
                }
            }
        }
        return update;
    }

    public String find(){
        logger.info("About to send query. Query : "+ this.query);
        String findings = null;
        StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append("PREFIX gas: <http://www.bigdata.com/rdf/gas#>\n" +
                "PREFIX entity: <http://www.wikidata.org/entity/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "SELECT ?x WHERE { \n" +
                "        ?x rdfs:label ?name .\n" +
                "  \t\tFILTER(lang(?name) = 'en')\n" +
                "  \t\tFILTER(CONTAINS(?name, '" + WordUtils.capitalize(this.query.trim()) + "')).\n" +
                "   }\n" +
                "limit 10");
//        queryStringBuilder.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX : <http://dbpedia.org/resource/>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX dbpedia2: <http://dbpedia.org/property/>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX dbpedia: <http://dbpedia.org/>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("PREFIX bif: <http://www.openlinksw.com/schemas/bif#>");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("SELECT ?x WHERE {?x rdfs:label ?name. FILTER(bif:contains(?name, \"Tokyo*\"))} LIMIT 10");
//        queryStringBuilder.append("SELECT ?x WHERE {?x rdfs:label ?name} LIMIT 1");
//        queryStringBuilder.append("SELECT ?x ");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("WHERE {");
//        queryStringBuilder.append("\t?x rdfs:label ?name . ");
//        queryStringBuilder.append("\n");
//        queryStringBuilder.append("\tFILTER REGEX(?name, ");
//        queryStringBuilder.append("\"^");
//        queryStringBuilder.append(WordUtils.capitalize(this.query));
//        queryStringBuilder.append("([(].*[)])?$\"");
//        queryStringBuilder.append(")");
//        queryStringBuilder.append("}");

        //String queryStr = queryStringBuilder.toString();
//        String queryStr =
//                "PREFIX wd: <http://www.wikidata.org/entity/> \n" +
//                        "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
//                        "PREFIX dbpprop: <http://dbpedia.org/property/>"+
//                        "PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
//                        "PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
//                        "PREFIX p: <http://www.wikidata.org/prop/>\n" +
//                        "PREFIX ps: <http://www.wikidata.org/prop/statement/>\n" +
//                        "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n" +
//                        "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
//                        "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
//                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
//                        "\n"+
//                        "SELECT ?item ?itemLabel ?_PubMed_ID WHERE {\n" +
//                        "  ?item wdt:P1716 wd:Q13442814.\n" +
//                        "  SERVICE wikibase:label {\n" +
//                        "    bd:serviceParam wikibase:language \"en\".\n" +
//                        "    ?item rdfs:label ?itemLabel.\n" +
//                        "  }\n" +
//                        "  FILTER(CONTAINS(LCASE(?itemLabel), \"zika\"))\n" +
//                        "  OPTIONAL { ?item wdt:P698 ?_PubMed_ID. }\n" +
//                        "}\n" +
//                        "LIMIT 1000";
        QueryExecution queryExecution = null;
        try{
            Query qry = QueryFactory.create(queryStringBuilder.toString());
            String wikidata = "https://query.wikidata.org/sparql";
            String dbpedia = "http://dbpedia.org/sparql";
            String sparqlEr = "http://sparql.org/sparql";
            queryExecution = QueryExecutionFactory.sparqlService(wikidata, qry);
            queryExecution.setTimeout(120000, TimeUnit.MILLISECONDS);
            ResultSet resultSet = queryExecution.execSelect();

            List<String> uris = new ArrayList<>();

            for(; resultSet.hasNext();){
                QuerySolution querySolution = resultSet.nextSolution();
                RDFNode rdfNode = querySolution.get("x");
                if(rdfNode.isResource()){
                    Resource resource = (Resource) rdfNode;
                    if(!resource.isAnon()){
                        String uri = resource.getURI();
                        if(StringUtils.isNotBlank(uri)) {
                            uris.add(uri);
                        }
                    }
                }
            }

            if(!uris.isEmpty()){
                for(String uri : uris){
                    RestTemplate restTemplate = new RestTemplate();
                    HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory
                            = new HttpComponentsClientHttpRequestFactory();
                    HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy())
                            .build();
                    httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
                    restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory);

                    String responseString = restTemplate.getForObject(uri, String.class);
                    if(StringUtils.isNotBlank(responseString)){
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map<String, Object> responseMap = objectMapper.readValue(responseString, HashMap.class);
                        if(responseMap != null && !responseMap.isEmpty()){
                            if(responseMap.containsKey(LearningResponseKeys.ENTITIES.toString())){
                                Object entities = responseMap.get(LearningResponseKeys.ENTITIES.toString());
                                if(entities instanceof Map) {
                                    Map<String, Object> entitiesMap = (Map<String, Object>) entities;
                                    if (!entitiesMap.isEmpty()) {
                                        Set<String> keySets = entitiesMap.keySet();
                                        if (!keySets.isEmpty()) {
                                            for (String keySet : keySets) {
                                                if (entitiesMap.containsKey(keySet)) {
                                                    Object value = entitiesMap.get(keySet);
                                                    if (value instanceof Map) {
                                                        Map<String, Object> valueMap = (Map<String, Object>) value;
                                                        if(!valueMap.isEmpty()) {
                                                            if (valueMap
                                                                    .containsKey(LearningResponseKeys.DESCRIPTIONS.toString())) {
                                                                Object description = valueMap
                                                                        .get(LearningResponseKeys.DESCRIPTIONS.toString());
                                                                if (description instanceof Map) {
                                                                    Map<String, Object> descriptionMap
                                                                            = (Map<String, Object>) description;
                                                                    if (!descriptionMap.isEmpty()) {
                                                                        if(descriptionMap
                                                                                .containsKey(Languages.EN.toInitial())) {
                                                                            Object englishValue
                                                                                    = descriptionMap.get(Languages.EN.toInitial());
                                                                            if(englishValue instanceof Map) {
                                                                                Map<String, Object> objectMap =
                                                                                        (Map<String, Object>) englishValue;
                                                                                if(!objectMap.isEmpty()) {
                                                                                    if (objectMap
                                                                                            .containsKey(LearningResponseKeys.VALUE.toString())) {
                                                                                        Object obj = objectMap.get(LearningResponseKeys.VALUE.toString());
                                                                                        if(obj instanceof String) {
                                                                                            findings = obj.toString();
                                                                                            logger.info("Description collected. Result : "+ findings);
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            logger.debug("Error: "+ e.getMessage());
        } finally {
            if(queryExecution != null) {
                queryExecution.close();
            }
        }
        return findings;
    }

    /**
     * This method would be used to generate sentences out of a given tags.
     * @param tags
     * @return
     */
    public static String generateSentence(String[] tags){
        String sentence = null;
        if(tags != null && tags.length > 0){
            InputStream inputStream = null;
            try{
                StringBuilder stringBuilder = new StringBuilder();
                int x = 0;
                for(String t : tags){
                    if(x < (tags.length -1)) {
                        stringBuilder.append(t + " ");
                    } else {
                        stringBuilder.append(t);
                    }
                    x++;
                }

                String s = stringBuilder.toString();
            } catch(Exception e){
                logger.debug("Error: "+ e.getMessage());
            } finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception ex) {
                    logger.warn("Error in closing file. Message : "+ ex.getMessage());
                }
            }
        }
        return sentence;
    }

    /**
     * This method is used to get parts-of-speech of a given list of strings.
     * @param collection
     * @return
     */
    public static Map<String, Object> getPartsOfSpeech(List<String> collection){
        Map<String, Object> response = new HashMap<>();
        if(collection != null && !collection.isEmpty()){
            InputStream inputStream = null;
            try {
                ClassLoader classLoader = LearningImpl.class.getClassLoader();
                inputStream = classLoader.getResourceAsStream("en-pos-maxent.bin");
                POSModel posModel = new POSModel(inputStream);
                POSTaggerME taggerME = new POSTaggerME(posModel);

                String[] query = null;

                if(collection != null && !collection.isEmpty()){
                    query = new String[collection.size()];
                    int x = 0;
                    for(String s : collection){
                        query[x] = s;
                        x++;
                    }
                }

                String[] tags = taggerME.tag(query);

                Map<String, Object> tagResponses = new HashMap<>();
                if(tags != null && tags.length > 0){
                    int x = 0;
                    for(String tag : tags){
                        if(StringUtils.isNotBlank(tag)) {
                            if(!tag.equalsIgnoreCase(":") && !tag.equalsIgnoreCase(".")) {
                                POSRESPONSES p = POSRESPONSES.valueOf(tag);
                                tagResponses.put(query[x], p.toString());
                            }
                        }
                        x++;
                    }
                }
                response.put("partsOfSpeech", tagResponses);


            } catch (Exception e){
                logger.debug("Error: Message: "+ e.getMessage());
            } finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e){
                        logger.debug("Error: "+ e.getMessage());
                    }
                }
            }
        }
        return response;
    }
}
