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
    public Criteria getQueryCriteria() {
        List<QueryPart> queryParts = QueryParser.parseQuery(phrase, advanced);
        if (queryParts.isEmpty()) {
            return Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).contains("");
        } else {
            Criteria andCriteria = AnyCriteria.any();
            for (QueryPart queryPart : queryParts) {
                Criteria orCriteria = null;
                if (!queryPart.isComplexPhrase()) {
                    Criteria definitionTextCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_FIELD).boost(1f).contains(queryPart.getExpression());
                    Criteria labelCriteria = Criteria.where(IndexConcept.LABEL_TEXT_FIELD).boost(4f).contains(queryPart.getExpression());
                    Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).contains(queryPart.getExpression());
                    orCriteria = definitionTextCriteria.or(labelCriteria).or(notationCriteria);
                }
                Criteria codeCriteria = Criteria.where(IndexConcept.CODE_FIELD).boost(10f).is(queryPart.getExpression());
                Criteria definitionTextEnCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria labelCriteria = Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).is(queryPart.getExpression());
                Criteria labelEngCriteria = Criteria.where(IndexConcept.LABEL_TEXT_EN_FIELD).boost(4f).is(queryPart.getExpression());
                Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).is(queryPart.getExpression());
                if (orCriteria == null) {
                    orCriteria = codeCriteria.or(definitionTextEnCriteria).or(labelCriteria).or(labelEngCriteria).or(notationCriteria);
                } else {
                    orCriteria = orCriteria.or(codeCriteria).or(definitionTextEnCriteria).or(labelCriteria).or(labelEngCriteria).or(notationCriteria);
                }
                andCriteria = andCriteria.and(orCriteria);
            }
            return andCriteria;
        }
    }

}
