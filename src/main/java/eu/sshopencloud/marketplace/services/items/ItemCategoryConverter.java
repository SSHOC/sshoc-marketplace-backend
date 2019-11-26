package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemCategoryConverter {

    public ItemCategory convertCategory(String category) {
        return ItemCategory.valueOf(category.toUpperCase().replace('-', '_'));
    }

    public String convertCategory(ItemCategory category) {
        return category.getValue();
    }

    public List<String> convertCategories(List<ItemCategory> categories) {
        if (categories == null) {
            return Collections.emptyList();
        }
        return categories.stream().map(ItemCategoryConverter::convertCategory).collect(Collectors.toList());
    }

}
