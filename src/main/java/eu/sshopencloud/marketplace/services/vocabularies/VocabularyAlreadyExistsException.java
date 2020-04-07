package eu.sshopencloud.marketplace.services.vocabularies;

public class VocabularyAlreadyExistsException extends Exception {

    public VocabularyAlreadyExistsException(String vocabularyCode) {
        super("Vocabulary with code '" + vocabularyCode + "' already exists!");
    }

}
