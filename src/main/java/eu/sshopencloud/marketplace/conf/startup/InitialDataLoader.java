package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.conf.startup.activities.ActivityLoader;
import eu.sshopencloud.marketplace.conf.startup.datasets.DatasetLoader;
import eu.sshopencloud.marketplace.conf.startup.tools.ToolLoader;
import eu.sshopencloud.marketplace.conf.startup.trainings.TrainingMaterialLoader;
import eu.sshopencloud.marketplace.model.activities.Activity;
import eu.sshopencloud.marketplace.model.activities.ActivityParthood;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.*;

import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader {

    private final ActorRoleRepository actorRoleRepository;

    private final ItemRelationRepository itemRelationRepository;

    private final ConceptRelationRepository conceptRelationRepository;

    private final UserRepository userRepository;

    private final ActorRepository actorRepository;

    private final IndexService indexService;

    private final ToolLoader toolLoader;

    private final TrainingMaterialLoader trainingMaterialLoader;

    private final DatasetLoader datasetLoader;

    private final ActivityLoader activityLoader;

    private final ItemRelatedItemRepository itemRelatedItemRepository;


    public void loadBasicData() {
        log.debug("Loading basic data");
        Map<String, List<Object>> data = YamlLoader.loadYamlData("initial-data/basic-data.yml");

        List<ActorRole> actorRoles = YamlLoader.getObjects(data, "ActorRole");
        actorRoleRepository.saveAll(actorRoles);
        log.debug("Loaded " + actorRoles.size() + " ActorRole objects");

        List<ItemRelation> itemRelations = YamlLoader.getObjects(data, "ItemRelation");
        itemRelationRepository.saveAll(itemRelations);
        log.debug("Loaded " + itemRelations.size() / 2 + " ItemRelation objects");

        List<ConceptRelation> conceptRelations = YamlLoader.getObjects(data, "ConceptRelation");
        conceptRelationRepository.saveAll(conceptRelations);
        log.debug("Loaded " + conceptRelations.size() / 2 + " ConceptRelation objects");
    }


    public void clearSearchIndexes() {
        log.debug("Clearing item index");
        indexService.clearItemIndex();
        log.debug("Clearing concept index");
        indexService.clearConceptIndex();
    }


    public void loadProfileData(String profile) {
        log.debug("Loading " + profile + " data");
        Map<String, List<Object>> data = YamlLoader.loadYamlData("initial-data/profile/" + profile + "-data.yml");

        List<User> users = YamlLoader.getObjects(data, "User");
        userRepository.saveAll(users);
        log.debug("Loaded " + users.size() + " User objects");

        List<Actor> actors = YamlLoader.getObjects(data, "Actor");
        actorRepository.saveAll(actors);
        log.debug("Loaded " + actors.size() + " Actor objects");

        List<Tool> tools = YamlLoader.getObjects(data, "Tool");
        toolLoader.createTools(tools);
        log.debug("Loaded " + tools.size() + " Tool objects");

        List<TrainingMaterial> trainingMaterials = YamlLoader.getObjects(data, "TrainingMaterial");
        trainingMaterialLoader.createTrainingMaterials(trainingMaterials);
        log.debug("Loaded " + trainingMaterials.size() + " TrainingMaterial objects");

        List<Dataset> datasets = YamlLoader.getObjects(data, "Dataset");
        datasetLoader.createDatasets(datasets);
        log.debug("Loaded " + datasets.size() + " Dataset objects");

        List<Activity> activities = YamlLoader.getObjects(data, "Activity");
        List<ActivityParthood> activityParthoods = YamlLoader.getObjects(data, "ActivityParthood");
        activityLoader.createActivities(activities, activityParthoods);
        log.debug("Loaded " + activities.size() + " Activity objects");

        List<ItemRelatedItem> itemRelatedItems = YamlLoader.getObjects(data, "ItemRelatedItem");
        itemRelatedItemRepository.saveAll(itemRelatedItems);
        log.debug("Loaded " + itemRelatedItems.size() + " ItemRelatedItem objects");
    }

}
