package eu.sshopencloud.marketplace.validators.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
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
public class StepFactory {

    private final ItemFactory itemFactory;


    public Step create(StepCore stepCore, Step prevStep, StepsTree stepsTree) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(stepCore, "Step");

        Step step = itemFactory.initializeItem(stepCore, new Step(), prevStep, ItemCategory.STEP, errors);

        try {
            if (stepCore.getStepNo() == null)
                stepsTree.appendStep(step);
            else
                stepsTree.addStep(step, stepCore.getStepNo());
        }
        catch (IndexOutOfBoundsException e) {
            errors.rejectValue(
                    "stepNo", "field.incorrect",
                    String.format("Incorrect step number: %d", stepCore.getStepNo())
            );
        }

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return step;
    }

    public Step makeNewVersion(Step step) {
        Step newStep = new Step(step);
        return itemFactory.initializeNewVersion(newStep, step);
    }
}
