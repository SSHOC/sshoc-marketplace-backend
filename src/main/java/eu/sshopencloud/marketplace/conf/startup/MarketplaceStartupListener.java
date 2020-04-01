package eu.sshopencloud.marketplace.conf.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketplaceStartupListener {

    private final InitialDataLoader initialDataLoader;

    private final InitialLicenseLoader initialLicenseLoader;

    private final InitialVocabularyLoader initialVocabularyLoader;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String jpaDdlAuto;

    @EventListener( classes = { ContextRefreshedEvent.class })
    public void onApplicationRefreshedEvent(ContextRefreshedEvent event) {
        log.debug("The magic begins !");

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
    }

}
