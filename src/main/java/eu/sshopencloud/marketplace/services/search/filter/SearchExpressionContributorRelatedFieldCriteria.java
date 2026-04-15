package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexItem;

import java.util.Map;

/**
 * Generates Solr filter query (fq) expressions for keys related with
 * {@link eu.sshopencloud.marketplace.model.items.ItemContributor}
 */
public class SearchExpressionContributorRelatedFieldCriteria extends SearchExpressionCriteria {

    public static final Map<String, String> KEY_TO_SOLR_FIELD_MAPPING = Map.of("actor_id",
            IndexItem.CONTRIBUTOR_ACTOR_ID_FIELD, "actor_role", IndexItem.CONTRIBUTOR_ACTOR_ROLE_FIELD);

    public SearchExpressionContributorRelatedFieldCriteria(String field, String expression) {
        super(field, expression);
    }

    @Override
    protected String getQueryFieldSpecifier() {
        return KEY_TO_SOLR_FIELD_MAPPING.get(field);
    }

}
