package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;

public class SearchExpressionDynamicFieldCriteria extends SearchExpressionCriteria {

    private static final String DYNAMIC_FIELD_PREFIX = "dynamic_property_";

    private PropertyTypeClass propertyTypeClass;

    public SearchExpressionDynamicFieldCriteria(String field, String expression, PropertyTypeClass propertyTypeClass) {
        super(field, expression);
        this.propertyTypeClass = propertyTypeClass;
    }

    @Override
    protected String getQueryFieldSpecifier() {
        return DYNAMIC_FIELD_PREFIX + field + propertyTypeClass.getDynamicFieldIndexTypeSuffix();
    }

}
