package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import org.springframework.data.solr.core.query.AnyCriteria;
import org.springframework.data.solr.core.query.Criteria;

import java.util.List;

public class IndexSearchQueryPhrase extends SearchQueryPhrase {

    public IndexSearchQueryPhrase(String phrase, boolean advanced) {
        super(phrase, advanced);
    }

    @Override
    public Criteria getQueryCriteria() {
        List<QueryPart> queryParts = QueryParser.parseQuery(phrase, advanced);
        if (queryParts.isEmpty()) {
            return Criteria.where(IndexItem.LABEL_TEXT_FIELD).boost(4f).contains("");
        } else {
            Criteria andCriteria = AnyCriteria.any();
            for (QueryPart queryPart : queryParts) {
                Criteria orCriteria = null;
                if (!queryPart.isComplexPhrase()) {
                    Criteria nameTextCriteria = Criteria.where(IndexItem.LABEL_TEXT_FIELD).boost(4f).contains(queryPart.getExpression());
                    Criteria descTextCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_FIELD).boost(3f).contains(queryPart.getExpression());
                    orCriteria = nameTextCriteria.or(descTextCriteria);
                }
                Criteria nameTextEnCriteria = Criteria.where(IndexItem.LABEL_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria descTextEnCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_EN_FIELD).boost(1f).is(queryPart.getExpression());
                Criteria keywordTextCriteria = Criteria.where(IndexItem.KEYWORD_TEXT_FIELD).boost(3f).is(queryPart.getExpression());
                if (orCriteria == null) {
                    orCriteria = nameTextEnCriteria.or(descTextEnCriteria).or(keywordTextCriteria);
                } else {
                    orCriteria = orCriteria.or(nameTextEnCriteria).or(descTextEnCriteria).or(keywordTextCriteria);
                }
                andCriteria = andCriteria.and(orCriteria);
            }
            return andCriteria;
        }
    }

}
