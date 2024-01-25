package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.SimpleParams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConceptSearchQueryPhrase extends SearchQueryPhrase {

    public enum ConceptCriteriaParams {
        CODE (IndexConcept.CODE_FIELD, 10f),
        URI (IndexConcept.URI_FIELD, 10f),
        NOTATION (IndexConcept.NOTATION_FIELD, 4f),
        LABEL_EN (IndexConcept.LABEL_TEXT_EN_FIELD, 4f),
        DEFINITION_EN (IndexConcept.DEFINITION_TEXT_EN_FIELD, 2f),
        LABEL(IndexConcept.LABEL_TEXT_FIELD, 2f, true),
        DEFINITION(IndexConcept.DEFINITION_TEXT_FIELD, 1f, true);

        private final String fieldName;
        private final float boost;
        private final boolean noQuoteOnly;

        ConceptCriteriaParams(String fieldName, float boost) {
            this(fieldName, boost, false);
        }

        ConceptCriteriaParams(String fieldName, float boost, boolean noQuoteOnly) {
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

    public ConceptSearchQueryPhrase(String phrase, boolean advanced) {
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
                List<String> queryPartsCriteria = Arrays.stream(ConceptCriteriaParams.values())
                        .filter(c -> !queryPart.isQuotedPhrase() || c.isQuoteParam())
                        .map(c -> c.getCondition(queryPart.getExpression(), false)).collect(Collectors.toList());

                queryCriteria.append(StringUtils.SPACE + SimpleParams.AND_OPERATOR + StringUtils.SPACE).append(
                                LEFT_BRACKET)
                        .append(StringUtils.join(queryPartsCriteria,
                                StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE)).append(RIGHT_BRACKET);
            }

            return queryCriteria.toString();
        }
    }

    @Override
    protected String getAdvancedQueryCriteria() {
        if (phrase.isEmpty()) {
            return QUERY_ALL;
        } else {
            List<String> queryPartsCriteria = Arrays.stream(ConceptCriteriaParams.values())
                    .map(c -> c.getCondition(phrase, true)).collect(Collectors.toList());

            return StringUtils.join(queryPartsCriteria,
                    StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE);
        }
    }

}
