package eu.sshopencloud.marketplace.services.search.query;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class SearchQueryPhrase implements SearchQueryCriteria {
    public static final String QUERY_ALL = "*:*";
    public static final String COLON = ":";
    public static final String WILDCARD = "*";
    public static final String CIRCUMFLEX = "^";
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";

    protected String phrase;

    protected boolean advanced;

    public SearchQueryPhrase(String phrase, boolean advanced) {
        if (StringUtils.isBlank(phrase)) {
            this.phrase = "";
        } else {
            this.phrase = phrase;
        }
        this.advanced = advanced;
    }

    @Override
    public String getQueryCriteria() {
        log.debug("Original query {}: {}", advanced, phrase);
        if (advanced) {
            return getAdvancedQueryCriteria();
        } else {
            return getPhraseQueryCriteria();
        }
    }

    protected abstract String getPhraseQueryCriteria();

    protected abstract String getAdvancedQueryCriteria();

}
