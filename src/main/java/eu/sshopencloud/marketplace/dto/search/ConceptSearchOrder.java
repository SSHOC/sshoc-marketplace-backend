package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonValue;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import org.apache.solr.client.solrj.SolrQuery;

public enum ConceptSearchOrder {

    SCORE(false, "score"),

    LABEL(true, IndexConcept.LABEL_FIELD);

    private final boolean asc;
    private final String name;

    ConceptSearchOrder(boolean asc, String name) {
        this.asc = asc;
        this.name = name;
    }

    public boolean isAsc() {
        return this.asc;
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    public SolrQuery.SortClause toSort() {
        return asc ? SolrQuery.SortClause.asc(name) : SolrQuery.SortClause.desc(name);
    }
}
