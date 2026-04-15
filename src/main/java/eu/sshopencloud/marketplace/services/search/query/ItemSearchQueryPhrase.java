package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.SimpleParams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ItemSearchQueryPhrase extends SearchQueryPhrase {
    public enum ItemCriteriaParams {
        PERSISTENT_ID (IndexItem.PERSISTENT_ID_FIELD, 3f),
        EXTERNAL_ID (IndexItem.EXTERNAL_IDENTIFIER_FIELD, 3f),
        LABEL_EN (IndexItem.LABEL_TEXT_EN_FIELD, 2f),
        DESCRIPTION_EN (IndexItem.DESCRIPTION_TEXT_EN_FIELD, 1f),
        CATEGORY (IndexItem.KEYWORD_TEXT_FIELD, 2f),
        CONTRIBUTOR (IndexItem.CONTRIBUTOR_TEXT_FIELD, 1f),
        LABEL(IndexItem.LABEL_TEXT_FIELD, 2f, true),
        DESCRIPTION(IndexItem.DESCRIPTION_TEXT_FIELD, 1f, true);

        private final String fieldName;
        private final float boost;
        private final boolean noQuoteOnly;

        ItemCriteriaParams(String fieldName, float boost) {
            this(fieldName, boost, false);
        }

        ItemCriteriaParams(String fieldName, float boost, boolean noQuoteOnly) {
            this.fieldName = fieldName;
            this.boost = boost;
            this.noQuoteOnly = noQuoteOnly;
        }

        public boolean isQuoteParam() {
            return !noQuoteOnly;
        }

        public String getCondition(String expression, boolean advanced) {
            if (noQuoteOnly && !advanced) {
                return fieldName + COLON + WILDCARD + expression + WILDCARD + CIRCUMFLEX + boost;
            }
            return fieldName + COLON + expression + CIRCUMFLEX + boost;
        }
    }

    private static final String INITIAL_QUERY_CRITERIA = "q={!boost b=scale(related_items,0,1)}*";

    public ItemSearchQueryPhrase(String phrase, boolean advanced) {
        super(phrase, advanced);
    }

    @Override
    protected String getPhraseQueryCriteria() {
        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);
        if (queryParts.isEmpty()) {
            return INITIAL_QUERY_CRITERIA;
        } else {
            StringBuilder queryCriteria = new StringBuilder(INITIAL_QUERY_CRITERIA);

            for (QueryPart queryPart : queryParts) {
                List<String> queryPartsCriteria = Arrays.stream(ItemCriteriaParams.values())
                        .filter(c -> queryPart.isQuotedPhrase() ? c.isQuoteParam() : true)
                        .map(c -> c.getCondition(queryPart.getExpression(), false)).collect(Collectors.toList());

                queryCriteria.append(StringUtils.SPACE + SimpleParams.AND_OPERATOR + StringUtils.SPACE)
                        .append("(").append(StringUtils.join(queryPartsCriteria, StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE)).append(")");
            }
            return queryCriteria.toString();
        }
    }

    @Override
    protected String getAdvancedQueryCriteria() {
        if (phrase.isEmpty()) {
            return INITIAL_QUERY_CRITERIA;
        } else {
            StringBuilder queryCriteria = new StringBuilder(INITIAL_QUERY_CRITERIA);
            List<String> queryPartsCriteria = Arrays.stream(ItemCriteriaParams.values())
                    .map(c -> c.getCondition(phrase, true))
                    .collect(Collectors.toList());

            queryCriteria.append(StringUtils.SPACE + SimpleParams.AND_OPERATOR + StringUtils.SPACE).append("(")
                    .append(StringUtils.join(queryPartsCriteria,
                            StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE)).append(")");
            return queryCriteria.toString();
        }
    }
}
