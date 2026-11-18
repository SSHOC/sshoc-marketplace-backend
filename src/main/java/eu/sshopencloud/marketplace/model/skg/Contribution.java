package eu.sshopencloud.marketplace.model.skg;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Contribution {

    private String by;
    private List<String> declaredAffiliations;
    private String role;

}
