package contentclassification.service;

import contentclassification.config.WordNetDictConfig;
import contentclassification.domain.Languages;
import contentclassification.domain.WordAndDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.elasticsearch.index.query.TermQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by rsl_prod_005 on 10/12/16.
 */
@Service
public class DictionaryIndexerService {
    private static Logger logger = LoggerFactory.getLogger(DictionaryIndexerService.class);
    private String LANGUAGE_CONFIG = "language-dictionaries-config.yml";

    @Autowired
    private WordNetDictConfig wordNetDictConfig;

    public List<Languages> getSupportedLanguages(){
        return Arrays.asList(Languages.values());
    }

    /**
     * Get language dictionary file name and path.
     * @param languages
     * @return
     */
    public String getLanguageDictionaryFilePath(Languages languages){
        String fileName = null;
        if(languages != null){
            String language = languages.toString();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(LANGUAGE_CONFIG);
            try {
                if (inputStream != null) {
                    Yaml yaml = new Yaml();
                    @SuppressWarnings("unchecked")
                    List<Map> configMaps = (List<Map>) yaml.load(inputStream);
                    if (configMaps != null && !configMaps.isEmpty()) {
                        for (Map configMap : configMaps) {
                            if (configMap.containsKey("language")) {
                                String configLanguage = configMap.get("language").toString();
                                if (language.equalsIgnoreCase(configLanguage)) {
                                    fileName = configMap.get("fileName").toString();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e){
                logger.debug("Error in getting language dictionary file path. Message : "+ e.getMessage());
            } finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (Exception e){
                        logger.warn("Exception in closing input stream. Message : "+ e.getMessage());
                    }
                }
            }
        }
        return fileName;
    }

    public File getDictionaryFile(String fileName){
        File file = null;
        if(StringUtils.isNotBlank(fileName)){
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(fileName);
            if(url != null){
                file = new File(url.getFile());
            }
        }
        return file;
    }


    public boolean isIndexDir(String indexerDir){
        logger.info("About to check if indexer directory is present.");
        boolean isPresent = false;
        if(StringUtils.isNotBlank(indexerDir)){
            String path = System.getProperty("user.dir");
            File file = new File(path + "/"+ indexerDir);

            if(file.exists()){
                logger.info("Indexer directory already exist in current working directory.");
                isPresent = true;
            } else {
                logger.info("Indexer directory doesn't exit in current working directory.");
                logger.info("About to create indexer directory.");
                isPresent = file.mkdir();
                logger.info("Done creating indexer directory.");
            }
        } else {
            logger.warn("Lucene dictionary indexer directory is not present.");
        }
        logger.info("Done checking indexer directory. Result : "+ isPresent);
        return isPresent;
    }

    /**
     * This method loads default dictionary into indexer.
     */
    public void loadDefaultDictionaryIntoIndexer(){
        logger.info("About to load dictionary into indexer.");
        Languages languages = Languages.fromString("english");
        logger.info("Default language selected : "+ languages.toString());
        if(languages != null){
            String dictFileName = getLanguageDictionaryFilePath(languages);
            logger.info("Language dictionary path selected : "+ (StringUtils.isNotBlank(dictFileName) ? dictFileName : "None"));
            if(StringUtils.isNotBlank(dictFileName)){
                IndexWriter indexWriter = null;
                try {
                    logger.info("About to create index document to be used in indexing.");
                    List<WordAndDefinition> wordAndDefinitions = loadWordsFromFile(dictFileName);
                    if(wordAndDefinitions != null && !wordAndDefinitions.isEmpty()){
                        indexWriter = indexWriter();
                        for(WordAndDefinition wordAndDefinition : wordAndDefinitions){
                            Document document = new Document();
                            document.add(new Field("content", wordAndDefinition.getWord(), Field.Store.YES,
                                    Field.Index.ANALYZED));
                            indexWriter.addDocument(document);
                        }
                        indexWriter.commit();
                    }

                    int numOfRecords = indexWriter.maxDoc();
                    logger.info("Number of records dictionary words indexed : "+ numOfRecords);
                } catch (Exception e){
                    logger.debug("Error in writing dictionary into indexer. Message : "+ e.getMessage());
                } finally {
                    if(indexWriter != null){
                        try {
                            indexWriter.close();
                        } catch (Exception e){
                            logger.debug("Error in closing index writer. Message : "+ e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public IndexWriter indexWriter(){
        IndexWriter indexWriter = null;
        String path = System.getProperty("user.dir");

        String indexerDir = wordNetDictConfig.getLuceneIndexerDir();
        boolean isIndexingDirPresent = isIndexDir(indexerDir);
        if(isIndexingDirPresent) {
            indexerDir = path + "/" + indexerDir;
        }

        if(StringUtils.isNotBlank(indexerDir)){
            Directory directory = null;
            IndexWriterConfig indexWriterConfig = null;

            try{
                directory = FSDirectory.open(new File(indexerDir));
                Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);

                indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                indexWriterConfig.setRAMBufferSizeMB(300);

                indexWriter = new IndexWriter(directory, indexWriterConfig);

            } catch (Exception e){
                logger.debug("Error in index writing to directory. Message : "+ e.getMessage());
            }
        }
        return indexWriter;
    }

    /**
     * this method is used to search for term matches in indexed language dictionary.
     * @param query
     * @return
     */
    public List<WordAndDefinition> searchDictionaryIndex(String query){
        logger.info("About to search within indexed dictionary. Query : "+ query);
        List<WordAndDefinition> wordAndDefinitions = null;
        if(StringUtils.isNotBlank(query)){
            logger.info("About to check which indexer directory to user.");
            String path = System.getProperty("user.dir");
            String indexerDir = path + "/"+ wordNetDictConfig.getLuceneIndexerDir();
            logger.info("Dictionary indexer directory to used. Path : "+ indexerDir);

            if(StringUtils.isNotBlank(indexerDir)){
                try{
                    File dictFile = new File(indexerDir);
                    Directory directory = FSDirectory.open(dictFile);

                    IndexReader reader = IndexReader.open(directory);
                    IndexSearcher searcher = new IndexSearcher(reader);

                    TermQueryParser queryParser = new TermQueryParser();

                    Term term = new Term("content", query);
                    Query searchQuery = new WildcardQuery(term);

                    TopDocs topDocs = searcher.search(searchQuery, 100);

                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    if(scoreDocs != null && scoreDocs.length > 0){
                        wordAndDefinitions = new ArrayList<>();
                        int x = 0;
                        for(ScoreDoc scoreDoc : scoreDocs){
                            int docId = scoreDoc.doc;
                            Document document = searcher.doc(docId);
                            String value = document.getField("content").stringValue();
                            WordAndDefinition wordAndDefinition = new WordAndDefinition(value);
                            wordAndDefinitions.add(wordAndDefinition);
                            x++;
                        }
                    } else {
                        logger.info("No score docs found. Count : "+ ((scoreDocs != null) ? scoreDocs.length : 0));
                    }
                    reader.close();
                } catch (Exception e){
                    logger.debug("Error in opening indexer directory for searches. Message : "+ e.getMessage());
                }
            }
        }
        logger.info("Done searching within indexed dictionary. Query : "+ query + " No. of match words : "+
                ((wordAndDefinitions != null) ? wordAndDefinitions.size() : 0));

        return wordAndDefinitions;
    }

    /**
     * this method is used get the list of words from a file.
     * @param fileName
     * @return
     */
    public List<WordAndDefinition> loadWordsFromFile(String fileName){
        List<WordAndDefinition> words = null;
        if(StringUtils.isNotBlank(fileName)){
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(fileName);
                if(inputStream != null) {
                    List<String> strWords = IOUtils.readLines(inputStream);
                    if (!strWords.isEmpty()) {
                        words = new ArrayList<>();
                        for (String strWord : strWords) {
                            if (StringUtils.isNotBlank(strWord)) {
                                WordAndDefinition wordAndDefinition = new WordAndDefinition(strWord);
                                words.add(wordAndDefinition);
                            }
                        }
                    }
                    inputStream.close();
                }
            } catch (Exception e){
                logger.debug("Error in loading words from file. File name : "+ e.getMessage());
            }
        }
        return words;
    }
}
