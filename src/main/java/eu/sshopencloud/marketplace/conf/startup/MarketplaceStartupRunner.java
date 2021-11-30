package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.services.search.IndexService;
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
    private final IndexService indexService;

    @Override
    public void run(String... args) throws Exception {
        initialDataLoader.loadBasicData();
        initialPropertyTypeLoader.loadPropertyTypeData();

        log.debug("reindexing items...");
        indexService.reindexItems();
        indexService.rebuildAutocompleteIndex();
        log.debug("reindexing concepts...");
        indexService.reindexConcepts();
        log.debug("reindexing actors...");
        indexService.reindexActors();

    }

}
