package eu.sshopencloud.marketplace.validators.trainings;

import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
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
public class TrainingMaterialValidator {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final ItemValidator itemValidator;


    public TrainingMaterial validate(TrainingMaterialCore trainingMaterialCore, Long trainingMaterialId) throws ValidationException {
        TrainingMaterial trainingMaterial = getOrCreateTrainingMaterial(trainingMaterialId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(trainingMaterialCore, "TrainingMaterial");

        itemValidator.validate(trainingMaterialCore, ItemCategory.TRAINING_MATERIAL, trainingMaterial, errors);

        trainingMaterial.setDateCreated(trainingMaterialCore.getDateCreated());
        trainingMaterial.setDateLastUpdated(trainingMaterialCore.getDateLastUpdated());

        if (trainingMaterialCore.getPrevVersionId() != null) {
            if (trainingMaterialId != null && trainingMaterial.getId().equals(trainingMaterialCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous training material cannot be the same as the current one.");
            }
            Optional<TrainingMaterial> prevVersionHolder = trainingMaterialRepository.findById(trainingMaterialCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous training material does not exist.");
            } else {
                trainingMaterial.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return trainingMaterial;
        }
    }

    private TrainingMaterial getOrCreateTrainingMaterial(Long trainingMaterialId) {
        if (trainingMaterialId != null) {
            return trainingMaterialRepository.getOne(trainingMaterialId);
        } else {
            return new TrainingMaterial();
        }
    }

}
