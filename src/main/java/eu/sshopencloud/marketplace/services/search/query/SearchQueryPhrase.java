package eu.sshopencloud.marketplace.services.search.query;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.solr.core.query.Criteria;

@Slf4j
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

    @Override
    public Criteria getQueryCriteria() {
        log.debug("Original query {}: {}", advanced, phrase);
        if (advanced) {
            return getAdvancedQueryCriteria();
        } else {
            return getPhraseQueryCriteria();
        }
    }

    protected abstract Criteria getPhraseQueryCriteria();

    protected abstract Criteria getAdvancedQueryCriteria();

}
