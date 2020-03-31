package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.trainings.TrainingMaterialValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final TrainingMaterialValidator trainingMaterialValidator;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;

    private final UserRepository userRepository;

    public PaginatedTrainingMaterials getTrainingMaterials(int page, int perpage) {
        Page<TrainingMaterial> trainingMaterials = trainingMaterialRepository.findAll(PageRequest.of(page - 1, perpage, Sort.by(Sort.Order.asc("label"))));
        for (TrainingMaterial trainingMaterial: trainingMaterials) {
            complete(trainingMaterial);
        }

        return PaginatedTrainingMaterials.builder().trainingMaterials(trainingMaterials.getContent())
                .count(trainingMaterials.getContent().size()).hits(trainingMaterials.getTotalElements()).page(page).perpage(perpage).pages(trainingMaterials.getTotalPages())
                .build();
    }

    public TrainingMaterial getTrainingMaterial(Long id) {
        Optional<TrainingMaterial> trainingMaterial = trainingMaterialRepository.findById(id);
        if (!trainingMaterial.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        return complete(trainingMaterial.get());
    }

    private TrainingMaterial complete(TrainingMaterial trainingMaterial) {
        trainingMaterial.setRelatedItems(itemRelatedItemService.getItemRelatedItems(trainingMaterial.getId()));
        trainingMaterial.setOlderVersions(itemService.getOlderVersionsOfItem(trainingMaterial));
        trainingMaterial.setNewerVersions(itemService.getNewerVersionsOfItem(trainingMaterial));
        itemService.fillAllowedVocabulariesForPropertyTypes(trainingMaterial);
        return trainingMaterial;
    }

    public TrainingMaterial createTrainingMaterial(TrainingMaterialCore trainingMaterialCore) throws ValidationException {
        TrainingMaterial trainingMaterial = trainingMaterialValidator.validate(trainingMaterialCore, null);
        ZonedDateTime now = ZonedDateTime.now();
        trainingMaterial.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (! (authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            List<User> informationContributors = new ArrayList<User>();
            informationContributors.add(user);
            trainingMaterial.setInformationContributors(informationContributors);
        }

        Item nextVersion = itemService.clearVersionForCreate(trainingMaterial);
        trainingMaterial = trainingMaterialRepository.save(trainingMaterial);
        itemService.switchVersion(trainingMaterial, nextVersion);
        indexService.indexItem(trainingMaterial);
        return complete(trainingMaterial);
    }

    public TrainingMaterial updateTrainingMaterial(Long id, TrainingMaterialCore trainingMaterialCore) throws ValidationException {
        if (!trainingMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        TrainingMaterial trainingMaterial = trainingMaterialValidator.validate(trainingMaterialCore, id);
        ZonedDateTime now = ZonedDateTime.now();
        trainingMaterial.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (! (authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            if (trainingMaterial.getInformationContributors() != null) {
                if (!trainingMaterial.getInformationContributors().contains(user)) {
                    trainingMaterial.getInformationContributors().add(user);
                }
            } else {
                List<User> informationContributors = new ArrayList<User>();
                informationContributors.add(user);
                trainingMaterial.setInformationContributors(informationContributors);
            }
        }
        Item prevVersion = trainingMaterial.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(trainingMaterial);
        trainingMaterial = trainingMaterialRepository.save(trainingMaterial);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(trainingMaterial);
        return complete(trainingMaterial);
    }

    public void deleteTrainingMaterial(Long id) {
        // TODO don't allow deleting without authentication (in WebSecurityConfig)
        if (!trainingMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(trainingMaterial);
        Item prevVersion = trainingMaterial.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(trainingMaterial);
        trainingMaterialRepository.delete(trainingMaterial);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.removeItem(trainingMaterial);
    }

}
