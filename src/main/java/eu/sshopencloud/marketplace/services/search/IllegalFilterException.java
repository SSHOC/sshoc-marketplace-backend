package eu.sshopencloud.marketplace.services.search;

public class IllegalFilterException extends Exception {

    public IllegalFilterException(String name) {
        super("Incorrect filter name '" + name + "'!");
    }

}
