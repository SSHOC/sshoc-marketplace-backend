package eu.sshopencloud.marketplace.conf.startup.datasets;

import eu.sshopencloud.marketplace.conf.startup.items.ItemLoader;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatasetLoader {

    private final ItemLoader itemLoader;

    private final DatasetRepository datasetRepository;

    private  final IndexService indexService;

    public void createDatasets(String profile, List<Dataset> newDatasets) {
        for (Dataset newDataset: newDatasets) {
            itemLoader.completeItemRelations(newDataset);
            Dataset dataset = datasetRepository.save(newDataset);
            if (!profile.equals("prod")) {
                indexService.indexItem(dataset);
            }
        }
    }

}
