package eu.sshopencloud.marketplace.services.search.query;

import org.apache.commons.lang3.StringUtils;

public abstract class SearchQueryPhrase implements SearchQueryCriteria {

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

}
