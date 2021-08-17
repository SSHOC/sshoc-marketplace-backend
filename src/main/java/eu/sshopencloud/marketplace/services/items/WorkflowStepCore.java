package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationsCore;
import eu.sshopencloud.marketplace.dto.items.RelatedItemCore;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.model.workflows.StepsTree;
import eu.sshopencloud.marketplace.validators.CollectionUtils;
import lombok.*;

import java.util.List;
import java.util.Objects;


@Value
class WorkflowStepCore implements ItemRelationsCore {

    StepCore stepCore;
    StepsTree parentTree;

    @Override
    public List<RelatedItemCore> getRelatedItems() {
        if (Objects.isNull(stepCore.getRelatedItems()))
            return null;
        if (CollectionUtils.isAllNulls(stepCore.getRelatedItems()))
            return null;
        return stepCore.getRelatedItems();
    }
}
