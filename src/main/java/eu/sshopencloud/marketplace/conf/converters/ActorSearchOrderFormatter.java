package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.ActorSearchOrder;


public class ActorSearchOrderFormatter extends BaseEnumFormatter<ActorSearchOrder> {

    private static final String SEARCH_ORDER_NAME = "actor-search-order";

    @Override
    protected ActorSearchOrder toEnum(String enumValue) {
        return ActorSearchOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return SEARCH_ORDER_NAME;
    }

}

