package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ItemDifferenceComparator<S extends ItemDto, T extends ItemDto> {

    public ItemsDifferenceDto differentiateItems(S finalItemDto, T finalOtherItem) {

        AtomicBoolean equal = new AtomicBoolean(true);

        ItemsDifferenceDto difference = new ItemsDifferenceDto();
        difference.setItem(finalItemDto);
        difference.setOther(finalOtherItem);

        if (!finalItemDto.getCategory().equals(difference.getOther().getCategory()))
            equal.set(false);

        if (finalItemDto.getLabel().equals(difference.getOther().getLabel()))
            difference.getOther().setLabel(null);
        else
            equal.set(false);

        if (finalItemDto.getDescription().equals(difference.getOther().getDescription()))
            difference.getOther().setDescription(null);
        else
            equal.set(false);

        if ((!Objects.isNull(finalItemDto.getSource()) && !Objects.isNull(difference.getOther().getSource())
                && finalItemDto.getSource().equals(difference.getOther().getSource())) || (
                Objects.isNull(finalItemDto.getSource()) && Objects.isNull(difference.getOther().getSource())))
            difference.getOther().setSource(null);
        else
            equal.set(false);

        if (StringUtils.isNotBlank(finalItemDto.getSourceItemId()) && StringUtils.isNotBlank(
                difference.getOther().getSourceItemId()) && finalItemDto.getSourceItemId()
                .equals(difference.getOther().getSourceItemId()) || (StringUtils.isBlank(finalItemDto.getSourceItemId())
                && StringUtils.isBlank(difference.getOther().getSourceItemId())))
            difference.getOther().setSourceItemId(null);
        else
            equal.set(false);

        if ((!Objects.isNull(finalItemDto.getThumbnail()) && !Objects.isNull(difference.getOther().getThumbnail())
                && finalItemDto.getThumbnail().equals(difference.getOther().getThumbnail())) || (
                Objects.isNull(finalItemDto.getThumbnail()) && Objects.isNull(difference.getOther().getThumbnail())))
            difference.getOther().setThumbnail(null);
        else
            equal.set(false);

        List<ItemContributorDto> itemContributors = new ArrayList<>();
        difference.getOther().getContributors().forEach(itemContributor -> {
            if (difference.getOther().getContributors().indexOf(itemContributor) < finalItemDto.getContributors()
                    .size() && itemContributor.equals(finalItemDto.getContributors()
                    .get(difference.getOther().getContributors().indexOf(itemContributor)))) {
                itemContributors.add(null);
            } else {
                itemContributors.add(itemContributor);
                equal.set(false);
            }
        });
        difference.getOther().setContributors(itemContributors);

        List<PropertyDto> itemProperties = new ArrayList<>();
        difference.getOther().getProperties().forEach(itemProperty -> {
            if (difference.getOther().getProperties().indexOf(itemProperty) < finalItemDto.getProperties().size()
                    && itemProperty.equals(finalItemDto.getProperties()
                    .get(difference.getOther().getProperties().indexOf(itemProperty)))) {
                itemProperties.add(null);
            } else {
                itemProperties.add(itemProperty);
                equal.set(false);
            }
        });
        difference.getOther().setProperties(itemProperties);

        List<String> accessibleAtList = new ArrayList<>();
        difference.getOther().getAccessibleAt().forEach(accessibleAt -> {
            if (difference.getOther().getAccessibleAt().indexOf(accessibleAt) < finalItemDto.getAccessibleAt()
                    .size() && accessibleAt.equals(finalItemDto.getAccessibleAt()
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
            if (difference.getOther().getExternalIds().indexOf(externalId) < finalItemDto.getExternalIds().size()
                    && externalId.equals(finalItemDto.getExternalIds()
                    .get(difference.getOther().getExternalIds().indexOf(externalId)))) {
                itemExternalIds.add(null);
            } else {
                itemExternalIds.add(externalId);
                equal.set(false);
            }
        });
        difference.getOther().setExternalIds(itemExternalIds);

        List<ItemMediaDto> mediaList = new ArrayList<>();
        difference.getOther().getMedia().forEach(media -> {
            if (difference.getOther().getMedia().indexOf(media) < finalItemDto.getMedia().size() && media.equals(
                    finalItemDto.getMedia().get(difference.getOther().getMedia().indexOf(media)))) {
                mediaList.add(null);
            } else {
                mediaList.add(media);
                equal.set(false);
            }
        });
        difference.getOther().setMedia(mediaList);

        List<RelatedItemDto> relatedItems = new ArrayList<>();
        difference.getOther().getRelatedItems().forEach(relatedItem -> {
            if (difference.getOther().getRelatedItems().indexOf(relatedItem) < finalItemDto.getRelatedItems()
                    .size() && relatedItem.equals(finalItemDto.getRelatedItems()
                    .get(difference.getOther().getRelatedItems().indexOf(relatedItem)))) {
                relatedItems.add(null);
            } else {
                relatedItems.add(relatedItem);
                equal.set(false);
            }
        });
        difference.getOther().setRelatedItems(relatedItems);

        if (finalItemDto.getLastInfoUpdate().equals(difference.getOther().getLastInfoUpdate()))
            difference.getOther().setLastInfoUpdate(null);

        if (finalItemDto.getInformationContributor().equals(difference.getOther().getInformationContributor()))
            difference.getOther().setInformationContributor(null);

        difference.setEqual(equal.get());

        return difference;
    }

}
