package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexActor;
import org.springframework.data.solr.core.query.AnyCriteria;
import org.springframework.data.solr.core.query.Criteria;

import java.util.List;

public class ActorSearchQueryPhrase extends SearchQueryPhrase {

    public ActorSearchQueryPhrase(String phrase, boolean advanced) {
        super(phrase, advanced);
    }

    @Override
    protected Criteria getPhraseQueryCriteria() {

        List<QueryPart> queryParts = QueryParser.parsePhrase(phrase);

        if (queryParts.isEmpty()) {
            return AnyCriteria.any();
        } else {
            Criteria andCriteria = AnyCriteria.any();

            for (QueryPart queryPart : queryParts) {

                Criteria idCriteria = Criteria.where(IndexActor.ID_FIELD).boost(10f).is(queryPart.getExpression());
                Criteria externalIdentifierCriteria = Criteria.where(IndexActor.EXTERNAL_IDENTIFIER_FIELD).boost(10f).is(queryPart.getExpression());
                Criteria nameCriteria = Criteria.where(IndexActor.NAME_FIELD).boost(4f).contains(queryPart.getExpression());
                Criteria emailCriteria = Criteria.where(IndexActor.EMAIL_FIELD).boost(4f).contains(queryPart.getExpression());
                Criteria websiteCriteria = Criteria.where(IndexActor.WEBSITE_FIELD).boost(4f).contains(queryPart.getExpression());
                Criteria orCriteria = idCriteria.or(externalIdentifierCriteria).or(nameCriteria).or(emailCriteria).
                        or(websiteCriteria);
                andCriteria = andCriteria.and(orCriteria);
            }
            return andCriteria;
        }
    }

    @Override
    protected Criteria getAdvancedQueryCriteria() {
        if (phrase.isEmpty()) {
            return AnyCriteria.any();
        } else {

            Criteria idCriteria = Criteria.where(IndexActor.ID_FIELD).boost(10f).expression(phrase);
            Criteria externalIdentifierCriteria = Criteria.where(IndexActor.EXTERNAL_IDENTIFIER_FIELD).boost(10f).expression(phrase);
            Criteria nameCriteria = Criteria.where(IndexActor.NAME_FIELD).boost(4f).expression(phrase);
            Criteria emailCriteria = Criteria.where(IndexActor.EMAIL_FIELD).boost(4f).expression(phrase);
            Criteria websiteCriteria = Criteria.where(IndexActor.WEBSITE_FIELD).boost(2f).expression(phrase);
            return idCriteria.or(externalIdentifierCriteria).or(nameCriteria).or(emailCriteria).or(websiteCriteria);
        }
    }
}
