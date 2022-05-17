package eu.sshopencloud.marketplace.services.items.exception;

import eu.sshopencloud.marketplace.model.items.Item;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;


public class ItemsComparator implements Comparator<Item> {

    @Override
    public int compare(Item firstPlayer, Item secondPlayer) {
        return StringUtils.compare(firstPlayer.getLabel(), secondPlayer.getLabel());
    }
}