package eu.sshopencloud.marketplace.validators.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.repositories.workflows.StepRepository;
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
public class StepValidator {

    private final StepRepository stepRepository;

    private final ItemValidator itemValidator;


    public Step validate(StepCore stepCore, Long stepId) throws ValidationException {
        Step step = getOrCreateStep(stepId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(stepCore, "Step");

        itemValidator.validate(stepCore, ItemCategory.STEP, step, errors);

        if (stepCore.getPrevVersionId() != null) {
            if (stepId != null && step.getId().equals(stepCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous step cannot be the same as the current one.");
            }
            Optional<Step> prevVersionHolder = stepRepository.findById(stepCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous step does not exist.");
            } else {
                step.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return step;
        }
    }

    private Step getOrCreateStep(Long stepId) {
        if (stepId != null) {
            return stepRepository.getOne(stepId);
        } else {
            return new Step();
        }
    }

}
