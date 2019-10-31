package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.model.licenses.LicenseType;
import eu.sshopencloud.marketplace.model.tools.Service;
import eu.sshopencloud.marketplace.model.tools.Software;
import eu.sshopencloud.marketplace.model.tools.ToolType;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterialType;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseRepository;
import eu.sshopencloud.marketplace.repositories.licenses.LicenseTypeRepository;
import eu.sshopencloud.marketplace.repositories.tools.ToolTypeRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialTypeRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.*;

import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.tools.ToolService;
import eu.sshopencloud.marketplace.services.trainings.TrainingMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader {

    private final LicenseTypeRepository licenseTypeRepository;

    private final LicenseRepository licenseRepository;

    private final ActorRoleRepository actorRoleRepository;

    private final ToolTypeRepository toolTypeRepository;

    private final TrainingMaterialTypeRepository trainingMaterialTypeRepository;

    private final ItemRelationRepository itemRelationRepository;

    private final ConceptRelationRepository conceptRelationRepository;

    private final PropertyTypeRepository propertyTypeRepository;

    private final VocabularyRepository vocabularyRepository;

    private final ConceptRepository conceptRepository;

    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;

    private final PropertyTypeVocabularyRepository propertyTypeVocabularyRepository;

    private final UserRepository userRepository;

    private final ActorRepository actorRepository;

    private final ToolService toolService;

    private final TrainingMaterialService trainingMaterialService;

    private final ItemRelatedItemRepository itemRelatedItemRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public void loadConstData() {
        log.debug("Loading const data");

        ClassLoader classLoader = InitialDataLoader.class.getClassLoader();
        InputStream dataStream = classLoader.getResourceAsStream("initial-data/const-data.yml");
        Map<String, List<Object>> data = (Map<String, List<Object>>) new Yaml(new CustomClassLoaderConstructor(classLoader)).load(dataStream);

        List<LicenseType> licenseTypes = getInitialObjects(data, "LicenseType");
        licenseTypeRepository.saveAll(licenseTypes);
        log.debug("Loaded " + licenseTypes.size()  + " LicenseType objects");

        List<License> licenses = getInitialObjects(data, "License");
        licenseRepository.saveAll(licenses);
        log.debug("Loaded " + licenses.size()  + " License objects");

        List<ActorRole> actorRoles = getInitialObjects(data, "ActorRole");
        actorRoleRepository.saveAll(actorRoles);
        log.debug("Loaded " + actorRoles.size()  + " ActorRole objects");

        List<ToolType> toolTypes = getInitialObjects(data, "ToolType");
        toolTypeRepository.saveAll(toolTypes);
        log.debug("Loaded " + toolTypes.size()  + " ToolType objects");

        List<TrainingMaterialType> trainingMaterialTypes = getInitialObjects(data, "TrainingMaterialType");
        trainingMaterialTypeRepository.saveAll(trainingMaterialTypes);
        log.debug("Loaded " + trainingMaterialTypes.size()  + " TrainingMaterialType objects");

        List<ItemRelation> itemRelations = getInitialObjects(data, "ItemRelation");
        itemRelationRepository.saveAll(itemRelations);
        log.debug("Loaded " + itemRelations.size() / 2  + " ItemRelation objects");

        List<ConceptRelation> conceptRelations = getInitialObjects(data, "ConceptRelation");
        conceptRelationRepository.saveAll(conceptRelations);
        log.debug("Loaded " + conceptRelations.size() / 2  + " ConceptRelation objects");

        List<PropertyType> propertyTypes = getInitialObjects(data, "PropertyType");
        propertyTypeRepository.saveAll(propertyTypes);
        log.debug("Loaded " + propertyTypes.size()  + " PropertyType objects");
    }


    public void loadVocabularies() {
        log.debug("Loading vocabularies");

        ClassLoader classLoader = InitialDataLoader.class.getClassLoader();
        InputStream dataStream = classLoader.getResourceAsStream("initial-data/vocabulary-data.yml");
        Map<String, List<Object>> data = (Map<String, List<Object>>) new Yaml(new CustomClassLoaderConstructor(classLoader)).load(dataStream);

        List<Vocabulary> vocabularies = getInitialObjects(data, "Vocabulary");
        vocabularyRepository.saveAll(vocabularies);
        log.debug("Loaded " + vocabularies.size() + " Vocabulary objects");

        List<Concept> conecpts = getInitialObjects(data, "Concept");
        conceptRepository.saveAll(conecpts);
        log.debug("Loaded " + conecpts.size() + " Concept objects");

        List<ConceptRelatedConcept> conceptRelatedConcept = getInitialObjects(data, "ConceptRelatedConcept");
        conceptRelatedConceptRepository.saveAll(conceptRelatedConcept);
        log.debug("Loaded " + conceptRelatedConcept.size()  + " ConceptRelatedConcept objects");

        List<PropertyTypeVocabulary> propertyTypeVocabularies = getInitialObjects(data, "PropertyTypeVocabulary");
        propertyTypeVocabularyRepository.saveAll(propertyTypeVocabularies);
        log.debug("Loaded " + propertyTypeVocabularies.size()  + " PropertyTypeVocabulary objects");
    }

    public void loadProfileData() {
        log.debug("Loading " + activeProfile + " data");

        ClassLoader classLoader = InitialDataLoader.class.getClassLoader();
        InputStream dataStream = classLoader.getResourceAsStream("initial-data/" + activeProfile + "-data.yml");
        Map<String, List<Object>> data = (Map<String, List<Object>>) new Yaml(new CustomClassLoaderConstructor(classLoader)).load(dataStream);

        List<User> users = getInitialObjects(data, "User");
        userRepository.saveAll(users);
        log.debug("Loaded " + users.size()  + " User objects");

        List<Actor> actors = getInitialObjects(data, "Actor");
        actorRepository.saveAll(actors);
        log.debug("Loaded " + actors.size()  + " Actor objects");

        try {
            List<Software> software = getInitialObjects(data, "Software");
            toolService.createTools(software);
            log.debug("Loaded " + software.size() + " Software objects");
        } catch (DataViolationException e) {
            log.error("Some Software objects haven't been loaded", e);
        }

        try {
            List<Service> services = getInitialObjects(data, "Service");
            toolService.createTools(services);
            log.debug("Loaded " + services.size()  + " Service objects");
        } catch (DataViolationException e) {
            log.error("Some Service objects haven't been loaded", e);
        }

        List<TrainingMaterial> trainingMaterials = getInitialObjects(data, "TrainingMaterial");
        trainingMaterialService.createTrainingMaterials(trainingMaterials);
        log.debug("Loaded " + trainingMaterials.size()  + " TrainingMaterial objects");

        List<ItemRelatedItem> itemRelatedItems = getInitialObjects(data, "ItemRelatedItem");
        itemRelatedItemRepository.saveAll(itemRelatedItems);
        log.debug("Loaded " + itemRelatedItems.size()  + " ItemRelatedItem objects");
    }

    private <T> List<T> getInitialObjects(Map<String, List<Object>> data, String label) {
        if (data == null) {
            return Collections.emptyList();
        }
        List<Object> objects = data.get(label);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }
        return (List<T>) objects;
    }

}
