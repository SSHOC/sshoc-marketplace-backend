package eu.sshopencloud.marketplace.repositories.vocabularies.projection;


public interface VocabularyBasicView {
    String getCode();
    String getScheme();
    String getNamespace();
    String getLabel();
    String getAccessibleAt();
    boolean isClosed();
}
