package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemCategoryConverter {

    public ItemCategory convertCategory(String category) {
        return ItemCategory.valueOf(category.toUpperCase().replace('-', '_'));
    }

    public String convertCategory(ItemCategory category) {
        return category.getValue();
    }

    public List<String> convertCategories(List<ItemCategory> categories) {
        List<String> result = new ArrayList<String>();
        if (categories != null) {
            for (ItemCategory category: categories) {
                result.add(convertCategory(category));
            }
        }
        return result;
    }

}
