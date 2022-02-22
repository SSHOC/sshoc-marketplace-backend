package eu.sshopencloud.marketplace.controllers.search;

import lombok.experimental.UtilityClass;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class UrlParamsExtractor {

    public Map<String, List<String>> extractFilterParams(MultiValueMap<String, String> params) {
        Map<String, List<String>> filterParams = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("f.")) {
                filterParams.put(key.substring(2), entry.getValue());
            }
        }
        return filterParams;
    }

    public Map<String, String> extractExpressionParams(MultiValueMap<String, String> params) {
        Map<String, String> expressionParams = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("d.")) {
                if(!entry.getValue().get(0).isEmpty())
                    expressionParams.put(key.substring(2), entry.getValue().get(0));
            }
        }
        return expressionParams;
    }

}
