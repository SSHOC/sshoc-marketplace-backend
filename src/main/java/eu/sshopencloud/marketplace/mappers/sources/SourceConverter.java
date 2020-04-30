package eu.sshopencloud.marketplace.mappers.sources;

import eu.sshopencloud.marketplace.model.sources.Source;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SourceConverter {

    public String convertSource(Source source) {
        if (source != null) {
            return source.getLabel();
        } else {
            return null;
        }
    }

}
