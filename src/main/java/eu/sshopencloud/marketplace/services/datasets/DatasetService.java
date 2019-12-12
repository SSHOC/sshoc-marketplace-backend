package eu.sshopencloud.marketplace.services.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.licenses.LicenseService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptDisallowedException;
import eu.sshopencloud.marketplace.services.vocabularies.DisallowedObjectTypeException;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyService;
import eu.sshopencloud.marketplace.services.vocabularies.TooManyObjectTypesException;
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
public class DatasetService {

    private final DatasetRepository datasetRepository;

    private final ItemService itemService;

    private final LicenseService licenseService;

    private final ItemContributorService itemContributorService;

    private final PropertyService propertyService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;

    private final UserRepository userRepository;


    public PaginatedDatasets getDatasets(int page, int perpage) {
        Page<Dataset> datasets = datasetRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Dataset dataset: datasets) {
            complete(dataset);
        }

        return PaginatedDatasets.builder().datasets(datasets.getContent())
                .count(datasets.getContent().size()).hits(datasets.getTotalElements()).page(page).perpage(perpage).pages(datasets.getTotalPages())
                .build();
    }

    public Dataset getDataset(Long id) {
        Optional<Dataset> dataset = datasetRepository.findById(id);
        if (!dataset.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id);
        }
        return complete(dataset.get());
    }

    private Dataset complete(Dataset dataset) {
        dataset.setRelatedItems(itemRelatedItemService.getItemRelatedItems(dataset.getId()));
        dataset.setOlderVersions(itemService.getOlderVersionsOfItem(dataset));
        dataset.setNewerVersions(itemService.getNewerVersionsOfItem(dataset));
        itemService.fillAllowedVocabulariesForPropertyTypes(dataset);
        return dataset;
    }

    private Dataset validate(DatasetCore newDataset, Long datasetId)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        Dataset result = createOrGetDataset(datasetId);
        result.setCategory(ItemCategory.DATASET);
        if (StringUtils.isBlank(newDataset.getLabel())) {
            throw new DataViolationException("label", newDataset.getLabel());
        }
        result.setLabel(newDataset.getLabel());
        result.setVersion(newDataset.getVersion());
        if (StringUtils.isBlank(newDataset.getDescription())) {
            throw new DataViolationException("description", newDataset.getDescription());
        }
        result.setDescription(MarkdownConverter.convertHtmlToMarkdown(newDataset.getDescription()));
        if (result.getLicenses() != null) {
            result.getLicenses().clear();
            result.getLicenses().addAll(licenseService.validate("licenses", newDataset.getLicenses()));
        } else {
            result.setLicenses(licenseService.validate("licenses", newDataset.getLicenses()));
        }
        if (result.getContributors() != null) {
            result.getContributors().clear();
            result.getContributors().addAll(itemContributorService.validate("contributors", newDataset.getContributors(), result));
        } else {
            result.setContributors(itemContributorService.validate("contributors", newDataset.getContributors(), result));
        }
        if (result.getProperties() != null) {
            result.getProperties().clear();
            result.getProperties().addAll(propertyService.validate(ItemCategory.DATASET, "properties", newDataset.getProperties(), result));
        } else {
            result.setProperties(propertyService.validate(ItemCategory.DATASET, "properties", newDataset.getProperties(), result));
        }

        result.setAccessibleAt(newDataset.getAccessibleAt());
        result.setDateCreated(newDataset.getDateCreated());
        result.setDateLastUpdated(newDataset.getDateLastUpdated());
        if (newDataset.getPrevVersionId() != null) {
            Optional<Dataset> prevVersion = datasetRepository.findById(newDataset.getPrevVersionId());
            if (!prevVersion.isPresent()) {
                throw new DataViolationException("prevVersionId", newDataset.getPrevVersionId());
            }
            if (datasetId != null) {
                if (result.getId().equals(newDataset.getPrevVersionId())) {
                    throw new DataViolationException("prevVersionId", newDataset.getPrevVersionId());
                }
            }
            result.setNewPrevVersion(prevVersion.get());
        }
        return result;
    }

    private Dataset createOrGetDataset(Long datasetId) {
        if (datasetId != null) {
            return datasetRepository.getOne(datasetId);
        } else {
            return new Dataset();
        }
    }

    public Dataset createDataset(DatasetCore newDataset)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        Dataset dataset = validate(newDataset, null);
        ZonedDateTime now = ZonedDateTime.now();
        dataset.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (! (authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            List<User> informationContributors = new ArrayList<User>();
            informationContributors.add(user);
            dataset.setInformationContributors(informationContributors);
        }

        Item nextVersion = itemService.clearVersionForCreate(dataset);
        dataset = datasetRepository.save(dataset);
        itemService.switchVersion(dataset, nextVersion);
        indexService.indexItem(dataset);
        return complete(dataset);
    }

    public Dataset updateDataset(Long id, DatasetCore newDataset)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        if (!datasetRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id);
        }
        Dataset dataset = validate(newDataset, id);
        ZonedDateTime now = ZonedDateTime.now();
        dataset.setLastInfoUpdate(now);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.toString());
        if (! (authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findUserByUsername(authentication.getName());
            if (dataset.getInformationContributors() != null) {
                if (!dataset.getInformationContributors().contains(user)) {
                    dataset.getInformationContributors().add(user);
                }
            } else {
                List<User> informationContributors = new ArrayList<User>();
                informationContributors.add(user);
                dataset.setInformationContributors(informationContributors);
            }
        }
        Item prevVersion = dataset.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(dataset);
        dataset = datasetRepository.save(dataset);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(dataset);
        return complete(dataset);
    }

    public void deleteDataset(Long id) {
        // TODO don't allow deleting without authentication (in WebSecurityConfig)
        if (!datasetRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id);
        }
        Dataset dataset = datasetRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(dataset);
        Item prevVersion = dataset.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(dataset);
        datasetRepository.delete(dataset);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.removeItem(dataset);
    }

}
