package eu.sshopencloud.marketplace.validators.trainings;

import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
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
public class TrainingMaterialFactory {

    private final ItemFactory itemFactory;


    public TrainingMaterial create(TrainingMaterialCore trainingMaterialCore, TrainingMaterial prevTrainingMaterial, boolean conflict)
            throws ValidationException {

        TrainingMaterial trainingMaterial =
                (prevTrainingMaterial != null) ? new TrainingMaterial(prevTrainingMaterial) : new TrainingMaterial();

        return setTrainingMaterialValues(trainingMaterialCore, trainingMaterial, conflict);
    }

    public TrainingMaterial modify(TrainingMaterialCore trainingMaterialCore, TrainingMaterial trainingMaterial)
            throws ValidationException {

        return setTrainingMaterialValues(trainingMaterialCore, trainingMaterial, false);
    }

    private TrainingMaterial setTrainingMaterialValues(TrainingMaterialCore trainingMaterialCore,
                                                       TrainingMaterial trainingMaterial, boolean conflict) throws ValidationException {

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(trainingMaterialCore, "TrainingMaterial");


        trainingMaterial = itemFactory.initializeItem(
                trainingMaterialCore, trainingMaterial, conflict, ItemCategory.TRAINING_MATERIAL, errors
        );

        trainingMaterial.setDateCreated(trainingMaterialCore.getDateCreated());
        trainingMaterial.setDateLastUpdated(trainingMaterialCore.getDateLastUpdated());

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return trainingMaterial;
    }

    public TrainingMaterial makeNewVersion(TrainingMaterial trainingMaterial) {
        TrainingMaterial newTrainingMaterial = new TrainingMaterial(trainingMaterial);
        return itemFactory.initializeNewVersion(newTrainingMaterial);
    }
}
