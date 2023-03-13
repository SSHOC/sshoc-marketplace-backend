package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.ItemSearchOrder;


public class ItemSearchOrderFormatter extends BaseEnumFormatter<ItemSearchOrder> {

    private static final String SEARCH_ORDER_NAME = "search-order";

    @Override
    protected ItemSearchOrder toEnum(String enumValue) {
        return ItemSearchOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return SEARCH_ORDER_NAME;
    }

}

