package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexActor;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.SimpleParams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActorSearchQueryPhrase extends SearchQueryPhrase {
    public enum ActorCriteriaParams {
        ID(IndexActor.ID_FIELD, 10f),
        EXT_ID(IndexActor.EXTERNAL_IDENTIFIER_FIELD, 10f),
        NAME(IndexActor.NAME_FIELD, 4f),
        EMAIL(IndexActor.EMAIL_FIELD, 4f),
        WEBSITE(IndexActor.WEBSITE_FIELD, 4f);

        private final String fieldName;
        private final float boost;

        ActorCriteriaParams(String fieldName, float boost) {
            this.fieldName = fieldName;
            this.boost = boost;
        }

        public String getCondition(String expression) {
            return fieldName + COLON + expression + CIRCUMFLEX + boost;
        }
    }

    public ActorSearchQueryPhrase(String phrase, boolean advanced) {
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
                List<String> queryPartsCriteria = Arrays.stream(ActorCriteriaParams.values())
                        .map(c -> c.getCondition(queryPart.getExpression())).collect(Collectors.toList());

                queryCriteria.append(StringUtils.SPACE + SimpleParams.AND_OPERATOR + StringUtils.SPACE).append("(")
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
            List<String> queryPartsCriteria = Arrays.stream(ActorCriteriaParams.values())
                    .map(c -> c.getCondition(phrase)).collect(Collectors.toList());

            return StringUtils.join(queryPartsCriteria,
                    StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE);
        }
    }
}
