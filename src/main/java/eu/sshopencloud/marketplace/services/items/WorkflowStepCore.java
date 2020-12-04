package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationsCore;
import eu.sshopencloud.marketplace.dto.items.RelatedItemCore;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import lombok.*;

import java.util.List;


@Value
class WorkflowStepCore implements ItemRelationsCore {

    StepCore stepCore;
    StepsTree parentTree;

    @Override
    public List<RelatedItemCore> getRelatedItems() {
        return stepCore.getRelatedItems();
    }
}
