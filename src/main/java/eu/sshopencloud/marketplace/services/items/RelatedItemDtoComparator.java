package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;

import java.util.Comparator;

public class RelatedItemDtoComparator implements Comparator<RelatedItemDto> {

    @Override
    public int compare(RelatedItemDto relatedItem1, RelatedItemDto relatedItem2) {
        return relatedItem1.getId().compareTo(relatedItem2.getId());
    }

}
