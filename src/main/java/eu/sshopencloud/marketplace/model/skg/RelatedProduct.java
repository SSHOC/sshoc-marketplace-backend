package eu.sshopencloud.marketplace.model.skg;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelatedProduct {

    private List<String> cites;
    private List<String> isSupplementedBy;
    private List<String> isDocumentedBy;
    private List<String> isNewVersionOf;
    private List<String> isPartOf;

}
