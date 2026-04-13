package eu.sshopencloud.marketplace.controllers.search;

import lombok.experimental.UtilityClass;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class UrlParamsExtractor {

    private static final String CONTRIBUTOR_RELATED_PARAM_PREFIX = "c.";
    private static final String DYNAMIC_PROPERTY_RELATED_PARAM_PREFIX = "d.";

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
        return extractExpressionParamsForPrefix(params,DYNAMIC_PROPERTY_RELATED_PARAM_PREFIX);
    }

    public Map<String, String> extractContributorRelatedSolrFilterQueryParams(MultiValueMap<String, String> params) {
        return extractExpressionParamsForPrefix(params,CONTRIBUTOR_RELATED_PARAM_PREFIX);
    }

    public Map<String, String> extractExpressionParamsForPrefix(MultiValueMap<String, String> params, String prefix) {
        Map<String, String> expressionParams = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                if(!entry.getValue().get(0).isEmpty())
                    expressionParams.put(key.substring(2), entry.getValue().get(0));
            }
        }
        return expressionParams;
    }
}
