package eu.sshopencloud.marketplace.controllers.oaipmh.metadata.extractors;

import eu.sshopencloud.marketplace.controllers.oaipmh.metadata.DcValue;
import eu.sshopencloud.marketplace.model.items.DigitalObject;
import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateExtractor extends CodeBasedPropertyExtractor {
    @Override
    public List<DcValue> extractValues(@NotNull OaiItem item, String dcLocalName) {
        List<DcValue> values = new ArrayList<>(super.extractValues(item, dcLocalName));
        if (item.getItem() instanceof DigitalObject && ((DigitalObject) item.getItem()).getDateCreated() != null) {
            values.add(new DcValue(DateTimeFormatter.ISO_DATE_TIME.format(((DigitalObject)item.getItem()).getDateCreated())));
        }
        if (item.getItem() instanceof DigitalObject && ((DigitalObject) item.getItem()).getDateLastUpdated() != null) {
            values.add(new DcValue(DateTimeFormatter.ISO_DATE_TIME.format(((DigitalObject)item.getItem()).getDateLastUpdated())));
        }
        return values;
    }
}
