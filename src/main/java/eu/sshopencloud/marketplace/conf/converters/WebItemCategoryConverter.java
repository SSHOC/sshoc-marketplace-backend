package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import org.springframework.core.convert.converter.Converter;

public class WebItemCategoryConverter implements Converter<String, ItemCategory> {

    @Override
    public ItemCategory convert(String source) {
        try {
            return ItemCategory.valueOf(source.toUpperCase().replace('-', '_'));
        } catch(Exception e) {
            throw new IllegalEnumException("category", source);
        }
    }

}
