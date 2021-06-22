package eu.sshopencloud.marketplace.model.items;

public enum ItemMediaType {

    MEDIA("Media"),
    THUMBNAIL("Thumbnail"),             //media that is also thumbnail
    THUMBNAIL_ONLY("Thumbnail only");   //thumbnail but not media

    private final String label;

    ItemMediaType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
