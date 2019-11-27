package eu.sshopencloud.marketplace.conf.vocabuleries;

import eu.sshopencloud.marketplace.conf.startup.YamlLoader;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeVocabulary;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeVocabularyRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyLoader {

    private final VocabularyService vocabularyService;

    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    private  final IndexService indexService;

    public void createVocabulariesWithConcepts(Resource[] resources) {
        Map<String, Resource> vocabularySources = new LinkedHashMap<String, Resource>();
        for (Resource resource: resources) {
            String vocabularyCode = resource.getFilename().substring(0, resource.getFilename().length() - 4);
            vocabularySources.put(vocabularyCode, resource);
        }
        for (String vocabularyCode: vocabularySources.keySet()) {
            try (InputStream turtleInputStream = vocabularySources.get(vocabularyCode).getInputStream()) {
                Vocabulary vocabulary = vocabularyService.createVocabulary(vocabularyCode, turtleInputStream);
                log.debug("The vocabulary '" + vocabulary.getLabel() + "' from '" + vocabulary.getCode()  + ".ttl' file loaded successfully");
                log.debug("The vocabulary '" + vocabulary.getLabel() + "' consists of " + vocabulary.getConcepts().size() + " concepts");
            } catch (Exception e) {
                log.error("Error while loading an initial vocabulary from the '" + vocabularyCode + "ttl' file!", e);
            }
        }
    }

    public void createPropertyTypeVocabularies(List<PropertyTypeVocabulary> newPropertyTypeVocabularies) {
        for (PropertyTypeVocabulary newPropertyTypeVocabulary: newPropertyTypeVocabularies) {
            PropertyTypeVocabulary propertyTypeVocabulary = propertyTypeVocabularyRepository.save(newPropertyTypeVocabulary);
            indexService.indexConcepts(propertyTypeVocabulary.getVocabulary());
        }
    }

}
