package eu.sshopencloud.marketplace.services.datasets;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.datasets.DatasetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

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


    public PaginatedDatasets getDatasets(PageCoords pageCoords) {
        Page<Dataset> datasetsPage = datasetRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<DatasetDto> datasets = datasetsPage.stream().map(DatasetMapper.INSTANCE::toDto)
                .map(dataset -> {
                    itemService.completeItem(dataset);
                    return dataset;
                })
                .collect(Collectors.toList());

        return PaginatedDatasets.builder().datasets(datasets)
                .count(datasetsPage.getContent().size()).hits(datasetsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(datasetsPage.getTotalPages())
                .build();
    }

    public DatasetDto getDataset(Long id) {
        Dataset dataset = datasetRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id));
        return itemService.completeItem(DatasetMapper.INSTANCE.toDto(dataset));
    }

    public DatasetDto createDataset(DatasetCore datasetCore) {
        Dataset dataset = datasetValidator.validate(datasetCore, null);
        itemService.updateInfoDates(dataset);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(dataset, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(dataset);
        dataset = datasetRepository.save(dataset);
        itemService.switchVersion(dataset, nextVersion);
        indexService.indexItem(dataset);
        return itemService.completeItem(DatasetMapper.INSTANCE.toDto(dataset));
    }

    public DatasetDto updateDataset(Long id, DatasetCore datasetCore) {
        if (!datasetRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Dataset.class.getName() + " with id " + id);
        }
        Dataset dataset = datasetValidator.validate(datasetCore, id);
        itemService.updateInfoDates(dataset);

        // TODO don't allow creating without authentication (in WebSecurityConfig)
        itemService.addInformationContributorToItem(dataset, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = dataset.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(dataset);
        dataset = datasetRepository.save(dataset);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(dataset);
        return itemService.completeItem(DatasetMapper.INSTANCE.toDto(dataset));
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
