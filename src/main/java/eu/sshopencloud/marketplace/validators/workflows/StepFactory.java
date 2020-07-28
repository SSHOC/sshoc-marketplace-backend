package eu.sshopencloud.marketplace.validators.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepParent;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import eu.sshopencloud.marketplace.repositories.workflows.StepRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemFactory;
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
public class StepFactory {

    private final StepRepository stepRepository;
    private final ItemFactory itemFactory;


    public Step create(StepCore stepCore, Step prevStep, StepParent parent) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(stepCore, "Step");

        Step step = itemFactory.initializeItem(stepCore, new Step(), prevStep, ItemCategory.STEP, errors);
        copySteps(step, prevStep);

        int numberOfSiblings = parent.getSteps().size();
        if (stepCore.getStepNo() != null) {
            if (prevStep != null) {
                if (stepCore.getStepNo() <= 0 || stepCore.getStepNo() > numberOfSiblings) {
                    errors.rejectValue("stepNo", "field.incorrect", "Incorrect step number.");
                }
            } else {
                if (stepCore.getStepNo() <= 0 || stepCore.getStepNo() > numberOfSiblings + 1) {
                    errors.rejectValue("stepNo", "field.incorrect", "Incorrect step number.");
                }
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return step;
        }
    }

    private void copySteps(Step step, Step prevStep) {
        if (prevStep == null)
            return;

        prevStep.getSubsteps().forEach(step::appendStep);
    }
}
