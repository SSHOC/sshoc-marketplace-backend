package eu.sshopencloud.marketplace.services.items.exception;

public class VersionNotChangedException extends Exception {

    public VersionNotChangedException() {
        super("Provided version has not changed.");
    }

}
