package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.dto.items.RelatedStepDto;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.repositories.items.workflow.StepsTreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RelatedItemsConverter {

    private final StepsTreeRepository stepsTreeRepository;


    public RelatedItemDto convertRelatedItemFromSubject(ItemRelatedItem subjectRelatedItem) {
        RelatedItemDto relatedItem = createRelatedItem(subjectRelatedItem.getObject());
        relatedItem.setId(subjectRelatedItem.getObject().getId());
        relatedItem.setPersistentId(subjectRelatedItem.getObject().getPersistentId());
        relatedItem.setRelation(ItemRelationMapper.INSTANCE.toDto(subjectRelatedItem.getRelation()));

        return completeRelatedItem(relatedItem, subjectRelatedItem.getObject());
    }

    public RelatedItemDto convertRelatedItemFromObject(ItemRelatedItem objectRelatedItem) {
        RelatedItemDto relatedItem = createRelatedItem(objectRelatedItem.getSubject());
        relatedItem.setId(objectRelatedItem.getSubject().getId());
        relatedItem.setPersistentId(objectRelatedItem.getSubject().getPersistentId());
        relatedItem.setRelation(ItemRelationMapper.INSTANCE.toDto(objectRelatedItem.getRelation().getInverseOf()));

        return completeRelatedItem(relatedItem, objectRelatedItem.getSubject());
    }

    private RelatedItemDto createRelatedItem(Item target) {
        if (target.getCategory() != ItemCategory.STEP)
            return new RelatedItemDto();

        String stepWorkflowId = stepsTreeRepository.findWorkflowPersistentIdByStepId(target.getId());

        return new RelatedStepDto(stepWorkflowId);
    }

    private RelatedItemDto completeRelatedItem(RelatedItemDto relatedItem, Item item) {
        relatedItem.setCategory(item.getCategory());
        relatedItem.setLabel(item.getLabel());
        relatedItem.setDescription(item.getDescription());
        return relatedItem;
    }
}
