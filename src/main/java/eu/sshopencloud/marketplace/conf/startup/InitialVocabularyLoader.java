package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.conf.startup.vocabuleries.VocabularyLoader;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeVocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialVocabularyLoader {

    private final VocabularyLoader vocabularyLoader;

    private final PropertyTypeRepository propertyTypeRepository;

    private final IndexService indexService;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String jpaDdlAuto;


    @Deprecated
    public void loadVocabularies() {
        log.debug("Loading vocabularies");
        ClassLoader classLoader = InitialDataLoader.class.getClassLoader();
        PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver(classLoader);
        try {
            Resource[] resources = pathResolver.getResources("initial-data/vocabularies/*.ttl");
            vocabularyLoader.createVocabulariesWithConcepts(resources);
        } catch (IOException e) {
            log.error("Error while loading initial vocabularies from 'ttl' files!");
        }
    }

    @Deprecated
    public void loadPropertyTypeData() {
        if (jpaDdlAuto.equals("create") || jpaDdlAuto.equals("create-drop")) {
            log.debug("Clearing concept index");
            indexService.clearConceptIndex();
        }

        log.debug("Loading property type data");
        Map<String, List<Object>> data = YamlLoader.loadYamlData("initial-data/property-type-data.yml");

        List<PropertyType> propertyTypes = YamlLoader.getObjects(data, "PropertyType");
        propertyTypeRepository.saveAll(propertyTypes);
        log.debug("Loaded " + propertyTypes.size()  + " PropertyType objects");

        List<PropertyTypeVocabulary> propertyTypeVocabularies = YamlLoader.getObjects(data, "PropertyTypeVocabulary");
        vocabularyLoader.createPropertyTypeVocabularies(activeProfile, propertyTypeVocabularies);
        log.debug("Loaded " + propertyTypeVocabularies.size()  + " PropertyTypeVocabulary objects");
    }

}
