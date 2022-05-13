package eu.sshopencloud.marketplace.services.items.exception;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;


public class BasicItemsComparator implements Comparator<ItemBasicDto> {

    @Override
    public int compare(ItemBasicDto firstPlayer, ItemBasicDto secondPlayer) {
        return StringUtils.compare(firstPlayer.getLabel(), secondPlayer.getLabel());
    }
}