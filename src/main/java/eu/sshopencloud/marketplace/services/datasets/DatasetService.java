package eu.sshopencloud.marketplace.services.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.datasets.DatasetValidator;
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
public class DatasetService {

    private final DatasetRepository datasetRepository;

    private final DatasetValidator datasetValidator;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;

    private final UserRepository userRepository;


    public PaginatedDatasets getDatasets(int page, int perpage) {
        Page<Dataset> datasets = datasetRepository.findAll(PageRequest.of(page - 1, perpage, Sort.by(Sort.Order.asc("label"))));
        for (Dataset dataset: datasets) {
            complete(dataset);
        }

        return PaginatedDatasets.builder().datasets(datasets.getContent())
                .count(datasets.getContent().size()).hits(datasets.getTotalElements()).page(page).perpage(perpage).pages(datasets.getTotalPages())
                .build();
    }

    public Dataset getDataset(Long id) {
        Dataset dataset = datasetRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id));
        return complete(dataset);
    }

    private Dataset complete(Dataset dataset) {
        dataset.setRelatedItems(itemRelatedItemService.getItemRelatedItems(dataset.getId()));
        dataset.setOlderVersions(itemService.getOlderVersionsOfItem(dataset));
        dataset.setNewerVersions(itemService.getNewerVersionsOfItem(dataset));
        itemService.fillAllowedVocabulariesForPropertyTypes(dataset);
        return dataset;
    }

    public Dataset createDataset(DatasetCore datasetCore) {
        Dataset dataset = datasetValidator.validate(datasetCore, null);
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

    public Dataset updateDataset(Long id, DatasetCore datasetCore) {
        if (!datasetRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id);
        }
        Dataset dataset = datasetValidator.validate(datasetCore, id);
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
