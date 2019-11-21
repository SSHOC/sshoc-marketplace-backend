package eu.sshopencloud.marketplace.conf.vocabuleries;

import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyLoader {

    private final VocabularyService vocabularyService;

    public void createVocabulariesWithConcepts(Resource[] resources) {
        Map<String, Resource> vocabularySources = new LinkedHashMap<String, Resource>();
        for (Resource resource: resources) {
            String vocabularyCode = resource.getFilename().substring(0, resource.getFilename().length() - 4);
            vocabularySources.put(vocabularyCode, resource);
        }
        // create vocabularies with simple concepts (without relations between concepts)
        for (String vocabularyCode: vocabularySources.keySet()) {
            try (InputStream turtleInputStream = vocabularySources.get(vocabularyCode).getInputStream()) {
                vocabularyService.createVocabulary(vocabularyCode, turtleInputStream);
            } catch (Exception e) {
                log.error("Error while loading an initial vocabulary from the '" + vocabularyCode + "ttl' file!", e);
            }
        }
    }

}
