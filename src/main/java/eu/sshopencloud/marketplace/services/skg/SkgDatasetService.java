package eu.sshopencloud.marketplace.services.skg;

import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.mappers.skg.ItemToSkgMapper;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.items.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class SkgDatasetService {

    private final DatasetService datasetService;
    private final ItemToSkgMapper itemToSkgMapper;

    public SkgDatasetService(DatasetService datasetService, ItemToSkgMapper itemToSkgMapper) {
        this.datasetService = datasetService;
        this.itemToSkgMapper = itemToSkgMapper;
    }

    public JsonLdDocument getDataset(String persistentId) {
        DatasetDto latestDataset = datasetService.getLatestDataset(persistentId, false, true, false);
        return itemToSkgMapper.toSkg(latestDataset);
    }


}
