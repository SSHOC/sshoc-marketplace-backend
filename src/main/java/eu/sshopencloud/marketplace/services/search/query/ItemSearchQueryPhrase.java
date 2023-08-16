package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleStringCriteria;

import java.util.List;

@Slf4j
public class ItemSearchQueryPhrase extends SearchQueryPhrase {

    public ItemSearchQueryPhrase(String phrase, boolean advanced) {
        super(phrase, advanced);
    }

    @Override
    protected Criteria getPhraseQueryCriteria() {
        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);
        if (queryParts.isEmpty()) {
            return initialCriteria();
        } else {
            Criteria andCriteria = initialCriteria();
            for (QueryPart queryPart : queryParts) {
                Criteria persistentIdCriteria = Criteria.where(IndexItem.PERSISTENT_ID_FIELD).boost(3f).is(queryPart.getExpression());
                Criteria externalIdCriteria = Criteria.where(IndexItem.EXTERNAL_IDENTIFIER_FIELD).boost(3f).is(queryPart.getExpression());
                Criteria labelTextEnCriteria = Criteria.where(IndexItem.LABEL_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria descTextEnCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_EN_FIELD).boost(1f).is(queryPart.getExpression());
                Criteria keywordTextCriteria = Criteria.where(IndexItem.KEYWORD_TEXT_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria contributorTextCriteria = Criteria.where(IndexItem.CONTRIBUTOR_TEXT_FIELD).boost(1f).is(queryPart.getExpression());
                Criteria orCriteria;

                if (!queryPart.isQuotedPhrase()) {
                    Criteria labelTextCriteria = Criteria.where(IndexItem.LABEL_TEXT_FIELD).boost(2f).contains(queryPart.getExpression());
                    Criteria descTextCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_FIELD).boost(1f).contains(queryPart.getExpression());

                    orCriteria = persistentIdCriteria.or(externalIdCriteria).or(labelTextCriteria).or(descTextCriteria)
                            .or(labelTextEnCriteria).or(descTextEnCriteria).or(keywordTextCriteria).or(contributorTextCriteria);
                } else {
                    orCriteria = persistentIdCriteria.or(externalIdCriteria).or(labelTextEnCriteria).or(descTextEnCriteria)
                            .or(keywordTextCriteria).or(contributorTextCriteria);

                }

                andCriteria = andCriteria.and(orCriteria);

            }
            return andCriteria;
        }
    }

    @Override
    protected Criteria getAdvancedQueryCriteria() {
        if (phrase.isEmpty()) {
            return initialCriteria();
        } else {
            Criteria andCriteria = initialCriteria();
            Criteria persistentIdCriteria = Criteria.where(IndexItem.PERSISTENT_ID_FIELD).boost(3f).expression(phrase);
            Criteria externalIdCriteria = Criteria.where(IndexItem.EXTERNAL_IDENTIFIER_FIELD).boost(3f).expression(phrase);
            Criteria labelTextCriteria = Criteria.where(IndexItem.LABEL_TEXT_FIELD).boost(3f).expression(phrase);
            Criteria descTextCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_FIELD).boost(1f).expression(phrase);
            Criteria labelTextEnCriteria = Criteria.where(IndexItem.LABEL_TEXT_EN_FIELD).boost(2f).expression(phrase);
            Criteria descTextEnCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_EN_FIELD).boost(1f).expression(phrase);
            Criteria keywordTextCriteria = Criteria.where(IndexItem.KEYWORD_TEXT_FIELD).boost(2f).expression(phrase);
            Criteria orCriteria = persistentIdCriteria.or(externalIdCriteria).or(labelTextCriteria).or(descTextCriteria)
                    .or(labelTextEnCriteria).or(descTextEnCriteria).or(keywordTextCriteria);
            return andCriteria.and(orCriteria);
        }
    }


    private Criteria initialCriteria() {
        return new SimpleStringCriteria("q={!boost b=product(related_items,2)}*");
    }

}
