package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.ConceptSearchOrder;


public class ConceptSearchOrderFormatter extends BaseEnumFormatter<ConceptSearchOrder> {

    private static final String SEARCH_ORDER_NAME = "concept-search-order";

    @Override
    protected ConceptSearchOrder toEnum(String enumValue) {
        return ConceptSearchOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return SEARCH_ORDER_NAME;
    }

}

