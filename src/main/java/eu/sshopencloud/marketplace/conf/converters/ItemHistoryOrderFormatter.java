package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.items.ItemHistoryOrder;

public class ItemHistoryOrderFormatter extends BaseEnumFormatter<ItemHistoryOrder>{

    private static final String ITEM_HISTORY_ORDER_NAME = "item-history-order";

    @Override
    protected ItemHistoryOrder toEnum(String enumValue) {
        return ItemHistoryOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return ITEM_HISTORY_ORDER_NAME;
    }
}
