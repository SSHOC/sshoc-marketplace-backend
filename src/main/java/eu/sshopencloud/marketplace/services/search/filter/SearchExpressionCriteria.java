package eu.sshopencloud.marketplace.services.search.filter;

import org.springframework.data.solr.core.query.Criteria;

public class SearchExpressionCriteria {

    protected String field;

    protected String expression;

    public SearchExpressionCriteria(String field, String expression) {
        this.field = field;
        this.expression = expression;
    }

    public Criteria getFilterCriteria() {
        Criteria filterField = new Criteria(getQueryFieldSpecifier());
        return filterField.expression(expression);
    }

    protected String getQueryFieldSpecifier() {
        return field.replace("-", "_");
    }

}
