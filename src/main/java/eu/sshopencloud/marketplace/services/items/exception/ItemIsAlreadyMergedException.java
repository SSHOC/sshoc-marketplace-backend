package eu.sshopencloud.marketplace.services.items.exception;


public class ItemIsAlreadyMergedException extends Exception {

    public ItemIsAlreadyMergedException(String persistentId, String mergedPersistentId) {
        super("Item " + persistentId + " is already merged into " + mergedPersistentId + ".");
    }

}
