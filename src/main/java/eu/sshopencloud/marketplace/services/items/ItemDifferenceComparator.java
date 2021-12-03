package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ItemDifferenceComparator<S extends ItemDto, T extends ItemDto, I extends Item> {

    public ItemsDifferenceDto differentiateItems(I item, List<RelatedItemDto> itemRelatedItems, I otherItem,
            List<RelatedItemDto> otherItemRelatedItems) {

        AtomicBoolean equal = new AtomicBoolean(true);
        ItemsDifferenceDto difference = new ItemsDifferenceDto();

        difference.setItem(map(item));
        difference.setOther(map(otherItem));
        difference.getItem().setRelatedItems(itemRelatedItems);
        difference.getOther().setRelatedItems(otherItemRelatedItems);

        if (!difference.getItem().getCategory().equals(difference.getOther().getCategory()))
            equal.set(false);

        if (difference.getItem().getLabel().equals(difference.getOther().getLabel()))
            difference.getOther().setLabel(null);
        else
            equal.set(false);

        if (difference.getItem().getDescription().equals(difference.getOther().getDescription()))
            difference.getOther().setDescription(null);
        else
            equal.set(false);

        if ((!Objects.isNull(difference.getItem().getSource()) && !Objects.isNull(difference.getOther().getSource())
                && difference.getItem().getSource().equals(difference.getOther().getSource())) || (
                Objects.isNull(difference.getItem().getSource()) && Objects.isNull(difference.getOther().getSource())))
            difference.getOther().setSource(null);
        else
            equal.set(false);

        if (StringUtils.isNotBlank(difference.getItem().getSourceItemId()) && StringUtils.isNotBlank(
                difference.getOther().getSourceItemId()) && difference.getItem().getSourceItemId()
                .equals(difference.getOther().getSourceItemId()) || (
                StringUtils.isBlank(difference.getOther().getSourceItemId()) && StringUtils.isBlank(
                        difference.getOther().getSourceItemId())))
            difference.getOther().setSourceItemId(null);
        else
            equal.set(false);

        List<ItemContributorDto> itemContributors = new ArrayList<>();
        difference.getOther().getContributors().forEach(itemContributor -> {
            if (difference.getOther().getContributors().indexOf(itemContributor) < difference.getItem()
                    .getContributors().size() && itemContributor.equals(difference.getItem().getContributors()
                    .get(difference.getOther().getContributors().indexOf(itemContributor)))) {
                itemContributors.add(null);
            } else {
                itemContributors.add(itemContributor);
                equal.set(false);
            }
        });
        difference.getOther().setContributors(itemContributors);

        List<String> accessibleAtList = new ArrayList<>();
        difference.getOther().getAccessibleAt().forEach(accessibleAt -> {
            if (difference.getOther().getAccessibleAt().indexOf(accessibleAt) < difference.getItem().getAccessibleAt()
                    .size() && accessibleAt.equals(difference.getItem().getAccessibleAt()
                    .get(difference.getOther().getAccessibleAt().indexOf(accessibleAt)))) {
                accessibleAtList.add(null);
            } else {
                accessibleAtList.add(accessibleAt);
                equal.set(false);
            }
        });
        difference.getOther().setAccessibleAt(accessibleAtList);

        List<ItemExternalIdDto> itemExternalIds = new ArrayList<>();
        difference.getOther().getExternalIds().forEach(externalId -> {
            if (difference.getOther().getExternalIds().indexOf(externalId) < difference.getItem().getExternalIds()
                    .size() && externalId.equals(difference.getItem().getExternalIds()
                    .get(difference.getOther().getExternalIds().indexOf(externalId)))) {
                itemExternalIds.add(null);
            } else {
                itemExternalIds.add(externalId);
                equal.set(false);
            }
        });
        difference.getOther().setExternalIds(itemExternalIds);

        List<RelatedItemDto> relatedItems = new ArrayList<>();
        difference.getOther().getRelatedItems().forEach(relatedItem -> {
            if (difference.getOther().getRelatedItems().indexOf(relatedItem) < difference.getItem().getRelatedItems()
                    .size() && relatedItem.equals(difference.getItem().getRelatedItems()
                    .get(difference.getOther().getRelatedItems().indexOf(relatedItem)))) {
                relatedItems.add(null);
            } else {
                relatedItems.add(relatedItem);
                equal.set(false);
            }
        });
        difference.getOther().setRelatedItems(relatedItems);

        if (difference.getItem().getLastInfoUpdate().equals(difference.getOther().getLastInfoUpdate()))
            difference.getOther().setLastInfoUpdate(null);

        if (difference.getItem().getInformationContributor().equals(difference.getOther().getInformationContributor()))
            difference.getOther().setInformationContributor(null);

        difference.setEqual(equal.get());

        return difference;
    }


    public <S extends ItemDto> ItemDto map(I item) {
        switch (item.getCategory()) {
            case TOOL_OR_SERVICE:
                return ToolMapper.INSTANCE.toDto(item);
            case TRAINING_MATERIAL:
                return TrainingMaterialMapper.INSTANCE.toDto(item);
            case PUBLICATION:
                return PublicationMapper.INSTANCE.toDto(item);
            case DATASET:
                return DatasetMapper.INSTANCE.toDto(item);
            case WORKFLOW:
                return WorkflowMapper.INSTANCE.toDto(item);
            case STEP:
                return StepMapper.INSTANCE.toDto(item);
            default:
                return null;
        }
    }


    public ItemsDifferenceDto<S, T> diffMedia(ItemsDifferenceDto difference) {

        if ((!Objects.isNull(difference.getItem().getThumbnail()) && !Objects.isNull(
                difference.getOther().getThumbnail()) && difference.getItem().getThumbnail()
                .equals(difference.getOther().getThumbnail())) || (Objects.isNull(difference.getItem().getThumbnail())
                && Objects.isNull(difference.getOther().getThumbnail())))
            difference.getOther().setThumbnail(null);
        else
            difference.setEqual(false);

        List<ItemMediaDto> mediaList = new ArrayList<>();
        difference.getOther().getMedia().forEach(media -> {
            if (difference.getOther().getMedia().indexOf(media) < difference.getItem().getMedia().size()
                    && media.equals(
                    difference.getItem().getMedia().get(difference.getOther().getMedia().indexOf(media)))) {
                mediaList.add(null);
            } else {
                mediaList.add(media);
                difference.setEqual(false);
            }
        });
        difference.getOther().setMedia(mediaList);

        List<PropertyDto> itemProperties = new ArrayList<>();
        difference.getOther().getProperties().forEach(itemProperty -> {
            if (difference.getOther().getProperties().indexOf(itemProperty) < difference.getItem().getProperties()
                    .size() && itemProperty.equals(difference.getItem().getProperties()
                    .get(difference.getOther().getProperties().indexOf(itemProperty)))) {
                itemProperties.add(null);
            } else {
                itemProperties.add(itemProperty);
                difference.setEqual(false);
            }
        });
        difference.getOther().setProperties(itemProperties);

        return difference;
    }

}
