package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;

@UtilityClass
public class IndexConverter {

    public IndexItem covertItem(Item item) {
        IndexItem.IndexItemBuilder builder = IndexItem.builder();
        builder.id(item.getId()).name(item.getLabel()).description(item.getDescription()).category(ItemCategoryConverter.convertCategory(item.getCategory()));
        builder.lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(item.getLastInfoUpdate().withZoneSameInstant(ZoneOffset.UTC)));
        return builder.build();
    }

}
