package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import org.springframework.core.convert.converter.Converter;

public class WebSearchOrderConverter implements Converter<String, SearchOrder> {

    @Override
    public SearchOrder convert(String source) {
        try {
            return SearchOrder.valueOf(source.toUpperCase().replace('-', '_'));
        } catch(Exception e) {
            throw new IllegalEnumException("order", source);
        }
    }

}
