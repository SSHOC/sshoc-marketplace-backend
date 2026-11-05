package eu.sshopencloud.marketplace.model.skg;

public enum EntityType {

    PRODUCT("product"),
    SEARCH_RESULT("search_result"),
    SEARCH_RESULT_PAGE("search_result_page"),;

    private String type;

    EntityType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
