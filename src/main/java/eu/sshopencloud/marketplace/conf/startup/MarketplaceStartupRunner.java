package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.tools.ToolRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketplaceStartupRunner implements CommandLineRunner {

    private final InitialDataLoader initialDataLoader;

    private final InitialLicenseLoader initialLicenseLoader;

    private final InitialVocabularyLoader initialVocabularyLoader;

    //
    private final ToolRepository toolRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;

    private final PropertyTypeRepository propertyTypeRepository;
    //


    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String jpaDdlAuto;

    @Override
    public void run(String... args) throws Exception {
        Date start = new Date();

        if (jpaDdlAuto.equals("create") || jpaDdlAuto.equals("create-drop")) {
            initialDataLoader.clearSearchIndexes();
        }

        initialDataLoader.loadBasicData();

        ///
/*
        for (Tool tool: toolRepository.findAll()) {
            Property toRemove = null;
            for (Property property : tool.getProperties()) {
                if (property.getType().getCode().equals("source-id")) {
                    toRemove = property;
                }
            }
            if (toRemove != null) {
                tool.getProperties().remove(toRemove);
                toolRepository.save(tool);
            }
        }

        for (TrainingMaterial trainingMaterial: trainingMaterialRepository.findAll()) {
            Property toRemove = null;
            for (Property property : trainingMaterial.getProperties()) {
                if (property.getType().getCode().equals("source-id")) {
                    toRemove = property;
                }
            }
            if (toRemove != null) {
                trainingMaterial.getProperties().remove(toRemove);
                trainingMaterialRepository.save(trainingMaterial);
            }
        }
        if (propertyTypeRepository.existsById("source-id")) {
            propertyTypeRepository.deleteById("source-id");
        }
*/

        ///


        initialLicenseLoader.loadLicenseData();

        initialVocabularyLoader.loadVocabularies();
        initialVocabularyLoader.loadPropertyTypeData();

        // for test load dev-data and append test-data
        if (activeProfile.equals("test")) {
            initialDataLoader.loadProfileData("dev");
        }
        initialDataLoader.loadProfileData(activeProfile);

        Date stop = new Date();
        double time = ((double)(stop.getTime() - start.getTime())) / 1000;
        log.info("Initialized MarketplaceApplication in " + time + " seconds");
    }

}
