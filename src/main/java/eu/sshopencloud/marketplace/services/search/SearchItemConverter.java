package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.conf.datetime.SolrDateTimeFormatter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.SearchItem;
import eu.sshopencloud.marketplace.model.search.SearchItem.SearchItemBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;

@UtilityClass
public class SearchItemConverter {

    public SearchItem covert(Item item) {
        SearchItemBuilder builder = SearchItem.builder();
        builder.id(item.getId()).name(item.getLabel()).description(item.getDescription()).category(item.getCategory().getValue());
        builder.lastInfoUpdate(SolrDateTimeFormatter.formatDateTime(item.getLastInfoUpdate().withZoneSameInstant(ZoneOffset.UTC)));
        return builder.build();
    }

}
