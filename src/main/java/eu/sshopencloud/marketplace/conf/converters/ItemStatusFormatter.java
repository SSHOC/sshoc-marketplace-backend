package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.items.ItemStatus;


public class ItemStatusFormatter extends BaseEnumFormatter<ItemStatus> {

    private static final String ITEM_STATUS_NAME = "item-status";

    @Override
    protected ItemStatus toEnum(String enumValue) {
        return ItemStatus.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return ITEM_STATUS_NAME;
    }

}
