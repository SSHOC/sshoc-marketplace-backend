package eu.sshopencloud.marketplace.services.search.filter;

public class SearchExpressionCriteria {

    protected String field;

    protected String expression;

    public SearchExpressionCriteria(String field, String expression) {
        this.field = field;
        this.expression = expression;
    }

    public String getFilterCriteria() {
        return getQueryFieldSpecifier() + ":" + expression;
    }

    protected String getQueryFieldSpecifier() {
        return field.replace("-", "_");
    }

}
