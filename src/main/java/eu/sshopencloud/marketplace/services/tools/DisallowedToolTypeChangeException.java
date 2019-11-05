package eu.sshopencloud.marketplace.services.tools;

public class DisallowedToolTypeChangeException extends Exception {

    public DisallowedToolTypeChangeException(String currentTypeCode, String newTypeCode) {
        super("The tool type cannot be changed from '" + currentTypeCode + "' to '" + newTypeCode + "'!");
    }

}
