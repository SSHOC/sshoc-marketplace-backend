package eu.sshopencloud.marketplace.validators.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.repositories.items.DatasetRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DatasetFactory {

    private final DatasetRepository datasetRepository;
    private final ItemFactory itemFactory;


    public Dataset create(DatasetCore datasetCore, Dataset prevDataset) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(datasetCore, "Dataset");

        Dataset dataset = itemFactory.initializeItem(datasetCore, new Dataset(), prevDataset, ItemCategory.DATASET, errors);

        dataset.setDateCreated(datasetCore.getDateCreated());
        dataset.setDateLastUpdated(datasetCore.getDateLastUpdated());

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return dataset;
        }
    }

    public Dataset makeNewVersion(Dataset baseDataset) {
        Dataset newDataset = new Dataset(baseDataset);
        return itemFactory.initializeNewVersion(newDataset, baseDataset);
    }
}
