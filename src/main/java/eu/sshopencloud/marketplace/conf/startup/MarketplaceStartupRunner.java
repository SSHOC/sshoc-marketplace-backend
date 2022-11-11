package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.services.search.IndexActorService;
import eu.sshopencloud.marketplace.services.search.IndexConceptService;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class MarketplaceStartupRunner implements CommandLineRunner {

    private final InitialDataLoader initialDataLoader;
    private final InitialPropertyTypeLoader initialPropertyTypeLoader;
    private final IndexItemService indexItemService;
    private final IndexConceptService indexConceptService;
    private final IndexActorService indexActorService;

    @Override
    public void run(String... args) throws Exception {
        initialDataLoader.loadBasicData();
        initialPropertyTypeLoader.loadPropertyTypeData();

        log.debug("reindexing items...");
        indexItemService.reindexItems();
        indexItemService.rebuildAutocompleteIndex();
        log.debug("reindexing concepts...");
        indexConceptService.reindexConcepts();
        log.debug("reindexing actors...");
        indexActorService.reindexActors();

    }

}
