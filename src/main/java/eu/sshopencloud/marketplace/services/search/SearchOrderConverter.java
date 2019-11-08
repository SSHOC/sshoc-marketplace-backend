package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class SearchOrderConverter {

    public String convertOrder(SearchOrder order) {
        return order.getValue().replace('-', '_');
    }

    public List<String> convertOrder(List<SearchOrder> order) {
        List<String> result = new ArrayList<String>();
        if (order != null) {
            for (SearchOrder o: order) {
                result.add(convertOrder(o));
            }
        }
        return result;
    }

}
