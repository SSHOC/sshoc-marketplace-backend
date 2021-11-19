package eu.sshopencloud.marketplace.services.vocabularies.exception;

public class VocabularyIsClosedException extends Exception {

    public VocabularyIsClosedException(String vocabularyCode) {
        super(String.format("Concept can not be added, because Vocabulary with code = '%s' is closed to changes (Vocabulary openness to false).", vocabularyCode));
    }
}
