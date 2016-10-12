package contentclassification.domain;

import contentclassification.config.ClassificationConfig;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by rsl_prod_005 on 5/9/16.
 */
public class Categories {
    private static final Logger logger = LoggerFactory.getLogger(Categories.class);
    private String category;
    private List<String> attributes;

    public Categories() {
    }

    public Categories(String category, List<String> attributes) {
        this.category = category;
        this.attributes = attributes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    @Cacheable("categoriesCache")
    public static List<Categories> loadCategories() {
        List<Categories> categories = new ArrayList<Categories>();

        try {
            ClassLoader classLoader = Categories.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("fashion-categories.yml");
            if(inputStream != null){
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, List<String>> ymlCategories = (Map<String, List<String>>) yaml.load(inputStream);

                if (ymlCategories != null && !ymlCategories.isEmpty()) {
                    int x = 0;
                    for (Map.Entry<String, List<String>> category : ymlCategories.entrySet()) {
                        Categories c = new Categories();
                        c.category = category.getKey();

                        List<String> attributes = category.getValue();
                        List<String> attributesPluralized = new LinkedList<>();
                        if (!attributes.isEmpty()) {
                            for (String s : attributes) {
                                Set<String> pluralized = pluralize(s);
                                if (!pluralized.isEmpty()) {
                                    for (String p : pluralized) {
                                        attributesPluralized.add(p);
                                    }
                                }
                            }

                            Set<String> cleanUp = new HashSet<>();
                            cleanUp.addAll(attributesPluralized);

                            attributesPluralized.clear();
                            attributesPluralized.addAll(cleanUp);

                            c.attributes = attributesPluralized;
                        }
                        categories.add(c);
                        x++;
                    }
                }

                inputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return categories;
    }

    public List<Categories> loadCategoriesFromYml() {
        List<Categories> categories = new ArrayList<Categories>();
        try {
            Yaml yaml = new Yaml();
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream url = classLoader.getResourceAsStream("fashion-categories.yml");
            if(url != null) {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> ymlCategories = (Map<String, List<String>>) yaml.load(url);

                if (ymlCategories != null && !ymlCategories.isEmpty()) {
                    int x = 0;
                    boolean isAddTopTopLevel = Boolean.parseBoolean(System.getProperty("categories.add.top"));
                    for (Map.Entry<String, List<String>> category : ymlCategories.entrySet()) {
                        Categories c = new Categories();
                        c.category = category.getKey();

                        List<String> attributes = category.getValue();

                        if(isAddTopTopLevel) {
                            attributes.add(category.getKey());
                        }

                        List<String> attributesPluralized = new LinkedList<>();
                        if (!attributes.isEmpty()) {
                            for (String s : attributes) {
                                Set<String> pluralized = pluralize(s);
                                if (!pluralized.isEmpty()) {
                                    for (String p : pluralized) {
                                        attributesPluralized.add(p);
                                    }
                                }
                            }

                            Set<String> cleanUp = new HashSet<>();
                            cleanUp.addAll(attributesPluralized);

                            attributesPluralized.clear();
                            attributesPluralized.addAll(cleanUp);

                            c.attributes = attributesPluralized;
                        }
                        categories.add(c);
                        x++;
                    }
                }

                url.close();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return categories;
    }

    public static Categories getCategoryByName(String name){
        Categories categories = null;
        if(StringUtils.isNotBlank(name)){
            try {
                ClassLoader classLoader = Categories.class.getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("fashion-categories.yml");
                if(inputStream != null) {
                    Yaml yaml = new Yaml();
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> ymlCategories = (Map<String, List<String>>) yaml.load(inputStream);
                    if (ymlCategories != null && !ymlCategories.isEmpty()) {
                        int x = 0;
                        if (ymlCategories.containsKey(name)) {
                            List<String> attributes = ymlCategories.get(name);
                            if (attributes != null && !attributes.isEmpty()) {
                                categories = new Categories();
                                categories.setCategory(name);
                                categories.setAttributes(attributes);
                            }
                        }
                    }

                    inputStream.close();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return categories;
    }

    public static Set<String> pluralize(String word) {
        Set<String> pluralize = new HashSet<>();
        if (StringUtils.isNotBlank(word)) {
            String singular = English.plural(word, 1);
            String plural = English.plural(word, 2);
            pluralize.add(singular);
            pluralize.add(plural);
        }
        return pluralize;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Categories)){
            return false;
        }
        return this.category.equals(((Categories) o).category);
    }
}
