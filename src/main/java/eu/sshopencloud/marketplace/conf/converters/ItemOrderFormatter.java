package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.items.ItemOrder;

public class ItemOrderFormatter extends BaseEnumFormatter<ItemOrder> {

    private static final String ITEM_ORDER_NAME = "item-order";

    @Override
    protected ItemOrder toEnum(String enumValue) {
        return ItemOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return ITEM_ORDER_NAME;
    }

}
