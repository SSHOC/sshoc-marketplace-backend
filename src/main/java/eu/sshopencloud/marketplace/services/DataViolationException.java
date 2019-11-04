package eu.sshopencloud.marketplace.services;

public class DataViolationException extends Exception {

    public DataViolationException(String path, String value) {
        super("Incorrect value '" + value + "' for '" + path + "'!");
    }

    public DataViolationException(String path, Long value) {
        super("Incorrect value '" + value + "' for '" + path + "'!");
    }

}
