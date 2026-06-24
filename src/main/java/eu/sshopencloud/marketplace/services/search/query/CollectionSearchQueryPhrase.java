package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexCollection;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.SimpleParams;

import java.util.Arrays;
import java.util.List;

public class CollectionSearchQueryPhrase extends SearchQueryPhrase {
    public enum CollectionCriteriaParams {
        ID(IndexCollection.ID_FIELD, 10f, false),
        DESCRIPTION(IndexCollection.DESCRIPTION_FIELD, 10f, false),
        TITLE(IndexCollection.TITLE_FIELD, 4f, true),
        OWNER_NAME(IndexCollection.OWNER_NAME_FIELD, 4f, true);

        private final String fieldName;
        private final float boost;
        private final boolean containsCondition;

        CollectionCriteriaParams(String fieldName, float boost, boolean containsCondition) {
            this.fieldName = fieldName;
            this.boost = boost;
            this.containsCondition = containsCondition;
        }

        public String getCondition(String expression) {
            if (containsCondition) {
                return fieldName + COLON + WILDCARD + expression + WILDCARD + CIRCUMFLEX + boost;
            }
            return fieldName + COLON + expression + CIRCUMFLEX + boost;
        }
    }


    public CollectionSearchQueryPhrase(String phrase, boolean advanced) {
        super(phrase, advanced);
    }

    @Override
    protected String getPhraseQueryCriteria() {
        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        if (queryParts.isEmpty()) {
            return QUERY_ALL;
        } else {
            StringBuilder queryCriteria = new StringBuilder(QUERY_ALL);

            for (QueryPart queryPart : queryParts) {
                List<String> queryPartsCriteria = Arrays.stream(CollectionSearchQueryPhrase.CollectionCriteriaParams.values())
                        .map(c -> c.getCondition(queryPart.getExpression())).toList();

                queryCriteria.append(StringUtils.SPACE).append(SimpleParams.AND_OPERATOR).append(StringUtils.SPACE).append("(")
                        .append(StringUtils.join(queryPartsCriteria,
                                StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE)).append(")");
            }
            return queryCriteria.toString();
        }
    }

    @Override
    protected String getAdvancedQueryCriteria() {
        if (phrase.isEmpty()) {
            return QUERY_ALL;
        } else {
            List<String> queryPartsCriteria = Arrays.stream(ActorSearchQueryPhrase.ActorCriteriaParams.values())
                    .map(c -> c.getCondition(phrase)).toList();

            return StringUtils.join(queryPartsCriteria,
                    StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE);
        }
    }
}
