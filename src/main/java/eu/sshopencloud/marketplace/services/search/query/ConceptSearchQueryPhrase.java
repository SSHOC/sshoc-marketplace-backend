package eu.sshopencloud.marketplace.services.search.query;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import org.springframework.data.solr.core.query.AnyCriteria;
import org.springframework.data.solr.core.query.Criteria;

import java.util.List;

public class ConceptSearchQueryPhrase extends SearchQueryPhrase {

    public ConceptSearchQueryPhrase(String phrase, boolean advanced) {
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
                Criteria codeCriteria = Criteria.where(IndexConcept.CODE_FIELD).boost(10f).is(queryPart.getExpression());
                Criteria uriCriteria = Criteria.where(IndexConcept.URI_FIELD).boost(10f).is(queryPart.getExpression());
                Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).is(queryPart.getExpression());
                Criteria labelTextEnCriteria = Criteria.where(IndexConcept.LABEL_TEXT_EN_FIELD).boost(4f).expression(queryPart.getExpression());
                Criteria definitionTextEnCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_EN_FIELD).boost(2f).expression(queryPart.getExpression());
                Criteria orCriteria;
                if (!queryPart.isQuotedPhrase()) {
                    Criteria labelTextCriteria = Criteria.where(IndexConcept.LABEL_TEXT_FIELD).boost(2f).contains(queryPart.getExpression());
                    Criteria definitionTextCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_FIELD).boost(1f).contains(queryPart.getExpression());
                    orCriteria = codeCriteria.or(uriCriteria).or(notationCriteria).or(labelTextCriteria)
                            .or(definitionTextCriteria).or(labelTextEnCriteria).or(definitionTextEnCriteria);
                } else {
                    orCriteria = codeCriteria.or(uriCriteria).or(notationCriteria).or(labelTextEnCriteria).
                            or(definitionTextEnCriteria);
                }
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
            Criteria codeCriteria = Criteria.where(IndexConcept.CODE_FIELD).boost(10f).expression(phrase);
            Criteria uriCriteria = Criteria.where(IndexConcept.URI_FIELD).boost(10f).expression(phrase);
            Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).expression(phrase);
            Criteria labelCriteria = Criteria.where(IndexConcept.LABEL_TEXT_FIELD).boost(4f).expression(phrase);
            Criteria definitionTextCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_FIELD).boost(2f).expression(phrase);
            Criteria labelTextEnCriteria = Criteria.where(IndexConcept.LABEL_TEXT_EN_FIELD).boost(4f).expression(phrase);
            Criteria definitionTextEnCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_EN_FIELD).boost(2f).expression(phrase);
            return codeCriteria.or(uriCriteria).or(notationCriteria).or(labelCriteria)
                    .or(definitionTextCriteria).or(labelTextEnCriteria).or(definitionTextEnCriteria);
        }
    }

}
