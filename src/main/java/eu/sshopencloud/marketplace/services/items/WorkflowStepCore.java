package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import lombok.*;


@Value
class WorkflowStepCore {

    StepCore stepCore;
    StepsTree parentTree;
}
