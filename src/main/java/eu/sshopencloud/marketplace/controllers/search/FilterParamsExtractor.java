package eu.sshopencloud.marketplace.controllers.search;

import lombok.experimental.UtilityClass;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class FilterParamsExtractor {

    public Map<String, List<String>> extractFilterParams(MultiValueMap<String, String> params) {
        Map<String, List<String>> filterParams = new HashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry: params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("f.")) {
                filterParams.put(key.substring(2, key.length()), entry.getValue());
            }
        }
        return filterParams;
    }

}
