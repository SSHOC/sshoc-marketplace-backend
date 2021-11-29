package eu.sshopencloud.marketplace.services.vocabularies.exception;

public class VocabularyIsClosedException extends Exception {

    public VocabularyIsClosedException(String vocabularyCode) {
        super(String.format("Concept cannot be added, because Vocabulary with code = '%s' is closed to changes.", vocabularyCode));
    }
}
