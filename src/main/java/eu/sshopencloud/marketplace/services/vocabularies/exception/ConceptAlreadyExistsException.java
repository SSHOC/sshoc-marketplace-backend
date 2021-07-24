package eu.sshopencloud.marketplace.services.vocabularies.exception;

public class ConceptAlreadyExistsException  extends Exception {

    public ConceptAlreadyExistsException(String code, String vocabularyCode) {
        super(String.format("Concept with code = '%s' already exists in vocabulary with code = '%s'", code, vocabularyCode));
    }
}
