package eu.sshopencloud.marketplace.repositories.oaipmh.metadata;

import eu.sshopencloud.marketplace.repositories.oaipmh.OaiItem;
import eu.sshopencloud.marketplace.repositories.oaipmh.metadata.extractors.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

@RequiredArgsConstructor
public enum DublinCoreMetadataExtractors {
    TITLE("title", (oaiItem, dcLocalName) -> Strings.isBlank(oaiItem.getItem().getLabel()) ? List.of() : List.of(
            new DcValue(oaiItem.getItem().getLabel()))),
    CREATOR("creator", new ActorRoleBasedExtractor()),
    SUBJECT("subject", new CodeBasedPropertyExtractor()),
    DESCRIPTION("description",
            (oaiItem, dcLocalName) -> Strings.isBlank(oaiItem.getItem().getDescription()) ? List.of() : List.of(
                    new DcValue(oaiItem.getItem().getDescription()))),
    PUBLISHER("publisher", new CodeBasedPropertyExtractor()),
    CONTRIBUTOR("contributor", new ActorRoleBasedExtractor()),
    DATE("date", new DateExtractor()),
    TYPE("type", new TypeExtractor()),
    FORMAT("format", new CodeBasedPropertyExtractor()),
    IDENTIFIER("identifier", new IdentifierExtractor()),
    SOURCE("source", (oaiItem, dcLocalName) -> (oaiItem.getItem().getSource() == null ||
            Strings.isBlank(oaiItem.getItem().getSource().getLabel())) ? List.of() : List.of(
            new DcValue(oaiItem.getItem().getSource().getLabel()))),
    LANGUAGE("language", new CodeBasedPropertyExtractor()),
    RELATION("relation", new RelationExtractor()),
    COVERAGE("coverage", new CodeBasedPropertyExtractor()),
    RIGHTS("rights", new CodeBasedPropertyExtractor());

    private final String localName;
    private final ValuesExtractor valuesExtractor;

    public void write(XMLStreamWriter writer, OaiItem oaiItem) throws XMLStreamException {
        List<DcValue> dcValues = valuesExtractor.extractValues(oaiItem, localName);
        for (DcValue dcValue : dcValues) {
            if (Strings.isNotBlank(dcValue.getValue())) {
                writer.writeStartElement("http://purl.org/dc/elements/1.1/", localName);
                writer.writeAttribute("xml:lang", "en");
                writer.writeCData(dcValue.getValue());
                writer.writeEndElement();
            }
        }
    }
}
