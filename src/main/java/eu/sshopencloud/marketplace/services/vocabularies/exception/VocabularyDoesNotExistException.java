package eu.sshopencloud.marketplace.services.vocabularies.exception;

public class VocabularyDoesNotExistException extends Exception {

    public VocabularyDoesNotExistException(String vocabularyCode) {
        super("Vocabulary with code '" + vocabularyCode + "' does not exist!");
    }

}
