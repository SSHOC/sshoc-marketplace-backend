package eu.sshopencloud.marketplace.conf.datasets;

import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatasetLoader {

    private final DatasetRepository datasetRepository;

    private  final IndexService indexService;

    public void createDatasets(List<Dataset> newDatasets) {
        for (Dataset newDataset: newDatasets) {
            Dataset dataset = datasetRepository.save(newDataset);
            indexService.indexItem(dataset);
        }
    }

}
