package eu.sshopencloud.marketplace.repositories.oaipmh;

import eu.sshopencloud.marketplace.conf.oaipmh.OaiPmhMetadataConfiguration;
import eu.sshopencloud.marketplace.repositories.oaipmh.metadata.DublinCoreMetadataExtractors;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLOutputFactory2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OaiItemUtils {
    private static final XMLOutputFactory factory = XMLOutputFactory2.newFactory();
    private static final String OAI_ID_FORMAT = "oai:%s:%s";
    private static String OAI_DOMAIN;

    private static OaiPmhMetadataConfiguration configuration;

    public static String buildOaiId(String persistentId) {
        return String.format(OAI_ID_FORMAT, OAI_DOMAIN, persistentId);
    }

    public static String convertToOaiDc(OaiItem oaiItem) {
        StringWriter out = new StringWriter();
        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writer.writeStartElement("oai_dc:dc");
            writer.writeNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            writer.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
            writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                    "http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
            for (DublinCoreMetadataExtractors extractor : DublinCoreMetadataExtractors.values()) {
                extractor.write(writer, oaiItem);
            }
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Could not create OaiDc metadata!", e);
        }
        return out.toString();
    }

    public static List<String> getPropertyCodeForDcName(String dcElementName) {
        return configuration.getPropertyCode(dcElementName);
    }

    public static List<String> getActorRoleForDcName(String dcElementName) {
        return configuration.getActorRole(dcElementName);
    }

    @Value("${marketplace.oai-pmh-data-provider.identifier.domain}")
    public void setOaiDomain(String oaiDomain) {
        OaiItemUtils.OAI_DOMAIN = oaiDomain;
    }

    @Autowired
    public void setConfiguration(OaiPmhMetadataConfiguration conf) {
        configuration = conf;
    }
}
