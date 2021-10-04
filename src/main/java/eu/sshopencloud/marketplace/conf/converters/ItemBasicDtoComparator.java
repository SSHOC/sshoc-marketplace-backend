package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;

import java.util.Comparator;

public class ItemBasicDtoComparator implements Comparator<ItemBasicDto> {
    public int compare(ItemBasicDto obj1, ItemBasicDto obj2) {
        return obj1.getLabel().compareTo(obj2.getLabel());
    }
}
