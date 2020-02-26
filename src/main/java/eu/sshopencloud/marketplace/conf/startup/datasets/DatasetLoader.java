package eu.sshopencloud.marketplace.conf.startup.datasets;

import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatasetLoader {

    private final DatasetRepository datasetRepository;

    private final ConceptRepository conceptRepository;

    private  final IndexService indexService;

    public void createDatasets(List<Dataset> newDatasets) {
        for (Dataset newDataset: newDatasets) {
            for (Property property: newDataset.getProperties()) {
                if (property.getConcept() != null) {
                    property.setConcept(conceptRepository.findById(ConceptId.builder().code(property.getConcept().getCode()).vocabulary(property.getConcept().getVocabulary().getCode()).build()).get());
                }
            }
            Dataset dataset = datasetRepository.save(newDataset);
            indexService.indexItem(dataset);
        }
    }

}
