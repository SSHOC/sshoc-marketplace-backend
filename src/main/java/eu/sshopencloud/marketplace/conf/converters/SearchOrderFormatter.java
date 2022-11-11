package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;


public class SearchOrderFormatter extends BaseEnumFormatter<SearchOrder> {

    private static final String SEARCH_ORDER_NAME = "search-order";

    @Override
    protected SearchOrder toEnum(String enumValue) {
        return SearchOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return SEARCH_ORDER_NAME;
    }

}

