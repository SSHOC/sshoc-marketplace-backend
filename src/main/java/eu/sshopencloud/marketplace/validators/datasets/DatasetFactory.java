package eu.sshopencloud.marketplace.validators.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DatasetFactory {

    private final ItemFactory itemFactory;


    public Dataset create(DatasetCore datasetCore, Dataset prevDataset, boolean conflict) throws ValidationException {
        Dataset dataset = (prevDataset != null) ? new Dataset(prevDataset) : new Dataset();
        return setDatasetValues(datasetCore, dataset, conflict);
    }

    public Dataset modify(DatasetCore datasetCore, Dataset dataset) throws ValidationException {
        return setDatasetValues(datasetCore, dataset, false);
    }

    private Dataset setDatasetValues(DatasetCore datasetCore, Dataset dataset, boolean conflict) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(datasetCore, "Dataset");

        dataset = itemFactory.initializeItem(datasetCore, dataset, conflict, ItemCategory.DATASET, errors);

        dataset.setDateCreated(datasetCore.getDateCreated());
        dataset.setDateLastUpdated(datasetCore.getDateLastUpdated());


        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }

        return dataset;
    }

    public Dataset makeNewVersion(Dataset dataset) {
        Dataset newDataset = new Dataset(dataset);
        return itemFactory.initializeNewVersion(newDataset);
    }
}
