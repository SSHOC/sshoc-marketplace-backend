package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterialType;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.CategoryService;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptDisallowedException;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
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

    private final CategoryService categoryService;

    private final ItemService itemService;

    private final LicenseService licenseService;

    private final ItemContributorService itemContributorService;

    private final PropertyService propertyService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;

    private final UserRepository userRepository;

    public PaginatedTrainingMaterials getTrainingMaterials(int page, int perpage) {
        Page<TrainingMaterial> trainingMaterials = trainingMaterialRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
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

    private TrainingMaterial validate(TrainingMaterialCore newTrainingMaterial, Long trainingMaterialId) throws DataViolationException, ConceptDisallowedException {
        TrainingMaterial result = createOrGetTrainingMaterial(trainingMaterialId);
        // TODO set type refactor together with vocabularies
        TrainingMaterialType trainingMaterialType = new TrainingMaterialType();
        trainingMaterialType.setCode(newTrainingMaterial.getTrainingMaterialType().getCode());
        result.setTrainingMaterialType(trainingMaterialType);

        result.setCategory(ItemCategory.TRAINING_MATERIAL);
        if (StringUtils.isBlank(newTrainingMaterial.getLabel())) {
            throw new DataViolationException("label", newTrainingMaterial.getLabel());
        }
        result.setLabel(newTrainingMaterial.getLabel());
        result.setVersion(newTrainingMaterial.getVersion());
        if (StringUtils.isBlank(newTrainingMaterial.getDescription())) {
            throw new DataViolationException("description", newTrainingMaterial.getDescription());
        }
        result.setDescription(newTrainingMaterial.getDescription());
        if (result.getLicenses() != null) {
            result.getLicenses().clear();
            result.getLicenses().addAll(licenseService.validate("licenses", newTrainingMaterial.getLicenses()));
        } else {
            result.setLicenses(licenseService.validate("licenses", newTrainingMaterial.getLicenses()));
        }
        if (result.getContributors() != null) {
            result.getContributors().clear();
            result.getContributors().addAll(itemContributorService.validate("contributors", newTrainingMaterial.getContributors(), result));
        } else {
            result.setContributors(itemContributorService.validate("contributors", newTrainingMaterial.getContributors(), result));
        }
        if (result.getProperties() != null) {
            result.getProperties().clear();
            result.getProperties().addAll(propertyService.validate("properties", newTrainingMaterial.getProperties()));
        } else {
            result.setProperties(propertyService.validate("properties", newTrainingMaterial.getProperties()));
        }
        result.setAccessibleAt(newTrainingMaterial.getAccessibleAt());
        result.setDateCreated(newTrainingMaterial.getDateCreated());
        result.setDateLastUpdated(newTrainingMaterial.getDateLastUpdated());
        if (newTrainingMaterial.getPrevVersionId() != null) {
            Optional<TrainingMaterial> prevVersion = trainingMaterialRepository.findById(newTrainingMaterial.getPrevVersionId());
            if (!prevVersion.isPresent()) {
                throw new DataViolationException("prevVersionId", newTrainingMaterial.getPrevVersionId());
            }
            if (trainingMaterialId != null) {
                if (result.getId().equals(newTrainingMaterial.getPrevVersionId())) {
                    throw new DataViolationException("prevVersionId", newTrainingMaterial.getPrevVersionId());
                }
            }
            result.setNewPrevVersion(prevVersion.get());
        }
        return result;
    }

    private TrainingMaterial createOrGetTrainingMaterial(Long trainingMaterialId) {
        if (trainingMaterialId != null) {
            return trainingMaterialRepository.getOne(trainingMaterialId);
        } else {
            return new TrainingMaterial();
        }
    }


    public TrainingMaterial createTrainingMaterial(TrainingMaterialCore newTrainingMaterial)
            throws DataViolationException, ConceptDisallowedException {
        // TODO move validation to the validate method (when vocabularies are refactored)
        String trainingMaterialTypeCode = categoryService.getTrainingMaterialCategoryCode(newTrainingMaterial.getTrainingMaterialType());
        TrainingMaterial trainingMaterial = validate(newTrainingMaterial, null);
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

    public TrainingMaterial updateTrainingMaterial(Long id, TrainingMaterialCore newTrainingMaterial)
            throws DataViolationException, ConceptDisallowedException {
        // TODO move validation to the validate method (when vocabularies are refactored)
        String trainingMaterialTypeCode = categoryService.getTrainingMaterialCategoryCode(newTrainingMaterial.getTrainingMaterialType());
        if (!trainingMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        TrainingMaterial trainingMaterial = validate(newTrainingMaterial, id);
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
