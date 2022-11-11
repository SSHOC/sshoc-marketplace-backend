package eu.sshopencloud.marketplace.conf.startup;

import eu.sshopencloud.marketplace.domain.media.MediaSourceService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.model.actors.ActorSource;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.actors.ActorSourceRepository;
import eu.sshopencloud.marketplace.repositories.actors.ActorRoleRepository;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader {

    private final UserRepository userRepository;
    private final ActorRoleRepository actorRoleRepository;
    private final ItemRelationRepository itemRelationRepository;
    private final ItemSourceRepository itemSourceRepository;
    private final ConceptRelationRepository conceptRelationRepository;
    private final SourceRepository sourceRepository;
    private final ActorSourceRepository actorSourceRepository;

    private final MediaSourceService mediaSourceCrudService;


    public void loadBasicData() {
        log.debug("Loading basic data");
        Map<String, List<Object>> data = YamlLoader.loadYamlData("initial-data/basic-data.yml");

        long usersCount = userRepository.count();
        if (usersCount == 0) {
            List<User> users = YamlLoader.getObjects(data, "User");
            users.forEach(user -> user.setRegistrationDate(ZonedDateTime.now()));
            userRepository.saveAll(users);
            log.debug("Loaded " + users.size() + " User objects");
        } else {
            log.debug("Skipping loading users. {} already present.", usersCount);
        }

        long actorRolesCount = actorRoleRepository.count();
        if (actorRolesCount == 0) {
            List<ActorRole> actorRoles = YamlLoader.getObjects(data, "ActorRole");
            actorRoleRepository.saveAll(actorRoles);
            log.debug("Loaded " + actorRoles.size() + " ActorRole objects");
        } else {
            log.debug("Skipping loading actor roles. {} already present.", actorRolesCount);
        }

        long itemRelationsCount = itemRelationRepository.count();
        if (itemRelationsCount == 0) {
            List<ItemRelation> itemRelations = YamlLoader.getObjects(data, "ItemRelation");
            itemRelationRepository.saveAll(itemRelations);
            log.debug("Loaded " + itemRelations.size() / 2 + " ItemRelation objects");
        } else {
            log.debug("Skipping loading item relations. {} already present.", itemRelationsCount);
        }

        long conceptRelationsCount = conceptRelationRepository.count();
        if (conceptRelationsCount == 0) {
            List<ConceptRelation> conceptRelations = YamlLoader.getObjects(data, "ConceptRelation");
            conceptRelationRepository.saveAll(conceptRelations);
            log.debug("Loaded " + conceptRelations.size() / 2 + " ConceptRelation objects");
        } else {
            log.debug("Skipping loading concept relations. {} already present.", conceptRelationsCount);
        }

        long sourcesCount = sourceRepository.count();
        if (sourcesCount == 0) {
            List<Source> sources = YamlLoader.getObjects(data, "Source");
            sourceRepository.saveAll(sources);
            log.debug("Loaded " + sources.size() + " Source objects");
        } else {
            log.debug("Skipping loading sources. {} already present.", sourcesCount);
        }

        long actorSourcesCount = actorSourceRepository.count();
        if (actorSourcesCount == 0) {
            List<ActorSource> actorSources = YamlLoader.getObjects(data, "ActorSource");
            actorSourceRepository.saveAll(actorSources);
            log.debug("Loaded {} ActorSource objects", actorSources.size());
        } else {
            log.debug("Skipping loading actor sources. {} already present.", actorSourcesCount);
        }

        long itemSourcesCount = itemSourceRepository.count();
        if (itemSourcesCount == 0) {
            List<ItemSource> itemSources = YamlLoader.getObjects(data, "ItemSource");
            itemSourceRepository.saveAll(itemSources);
            log.debug("Loaded {} ItemSource objects", itemSources.size());
        } else {
            log.debug("Skipping loading item sources. {} already present.", itemSourcesCount);
        }

        long mediaSourcesCount = mediaSourceCrudService.countAllMediaSources();
        if (mediaSourcesCount == 0) {
            List<MediaSourceCore> mediaSources = YamlLoader.getObjects(data, "MediaSource");
            mediaSourceCrudService.saveMediaSources(mediaSources);
            log.debug("Loaded {} MediaSource objects", mediaSources.size());
        } else {
            log.debug("Skipping loading media sources. {} already present.", mediaSourcesCount);
        }
    }

}
