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
    private final InitialVocabularyLoader initialVocabularyLoader;
    private final IndexService indexService;

    /*
    private final SequencerInitializer sequencerInitializer;
    private final InitialLicenseLoader initialLicenseLoader;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String jpaDdlAuto;
     */


    @Override
    public void run(String... args) throws Exception {
        initialDataLoader.loadBasicData();
        initialVocabularyLoader.loadPropertyTypeData();

        log.debug("reindexing items...");
        indexService.reindexItems();
        log.debug("reindexing concepts...");
        indexService.reindexConcepts();
    }

    /*
    private void loadDataOnStartupOld() throws Exception {
        Date start = new Date();

        sequencerInitializer.initSequencers();

        if (jpaDdlAuto.equals("create") || jpaDdlAuto.equals("create-drop")) {
            initialDataLoader.clearSearchIndexes();
        }

        initialDataLoader.loadBasicData();

        initialLicenseLoader.loadLicenseData();

        initialVocabularyLoader.loadVocabularies();
        initialVocabularyLoader.loadPropertyTypeData();

        // for test load dev-data and append test-data
        if (activeProfile.equals("test")) {
            initialDataLoader.loadProfileData("dev");
        }
        initialDataLoader.loadProfileData(activeProfile);
        initialDataLoader.reloadAdditionalSearchStructures();

        Date stop = new Date();
        double time = ((double)(stop.getTime() - start.getTime())) / 1000;
        log.info("Initialized MarketplaceApplication in " + time + " seconds");
    }
     */
}
