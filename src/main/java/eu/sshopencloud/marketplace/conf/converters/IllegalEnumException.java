package eu.sshopencloud.marketplace.conf.converters;

public class IllegalEnumException extends RuntimeException {

    public IllegalEnumException(String name, String value) {
        super("Incorrect value '" + value + "' for enum '" + name + "'!");
    }
}
