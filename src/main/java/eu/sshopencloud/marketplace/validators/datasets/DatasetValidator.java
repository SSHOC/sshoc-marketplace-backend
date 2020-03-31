package eu.sshopencloud.marketplace.validators.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.repositories.datasets.DatasetRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DatasetValidator {

    private final DatasetRepository datasetRepository;

    private final ItemValidator itemValidator;


    public Dataset validate(DatasetCore datasetCore, Long datasetId) throws ValidationException {
        Dataset dataset = getOrCreateDataset(datasetId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(datasetCore, "Dataset");

        itemValidator.validate(datasetCore, ItemCategory.DATASET, dataset, errors);

        dataset.setDateCreated(datasetCore.getDateCreated());
        dataset.setDateLastUpdated(datasetCore.getDateLastUpdated());

        if (datasetCore.getPrevVersionId() != null) {
            if (datasetId != null && dataset.getId().equals(datasetCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous dataset cannot be the same as the current one.");
            }
            Optional<Dataset> prevVersionHolder = datasetRepository.findById(datasetCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous dataset does not exist.");
            } else {
                dataset.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return dataset;
        }
    }

    private Dataset getOrCreateDataset(Long datasetId) {
        if (datasetId != null) {
            return datasetRepository.getOne(datasetId);
        } else {
            return new Dataset();
        }
    }


}
