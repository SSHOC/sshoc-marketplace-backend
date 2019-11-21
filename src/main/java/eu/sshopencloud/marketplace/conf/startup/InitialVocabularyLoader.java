package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.conf.vocabuleries.VocabularyLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialVocabularyLoader {

    private final VocabularyLoader vocabularyLoader;

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

}
