package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.items.ItemCategory;


public class ItemCategoryFormatter extends BaseEnumFormatter<ItemCategory> {

    private static final String ITEM_CATEGORY_NAME = "item-category";

    @Override
    protected ItemCategory toEnum(String enumValue) {
        return ItemCategory.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return ITEM_CATEGORY_NAME;
    }

}
