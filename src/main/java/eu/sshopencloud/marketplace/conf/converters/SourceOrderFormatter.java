package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.sources.SourceOrder;

public class SourceOrderFormatter extends BaseEnumFormatter<SourceOrder> {

    private static final String SOURCE_ORDER_NAME = "source-order";

    @Override
    protected SourceOrder toEnum(String enumValue) {
        return SourceOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return SOURCE_ORDER_NAME;
    }

}
