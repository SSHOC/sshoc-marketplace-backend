package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;

public class ConceptDisallowedException extends Exception {

    public ConceptDisallowedException(PropertyType propertyType, String vocabularyCode) {
        super("Disallowed vocabulary '" + vocabularyCode + "' for property type '" + propertyType.getCode() + "'!");
    }

}
