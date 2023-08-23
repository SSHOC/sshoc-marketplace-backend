package eu.sshopencloud.marketplace.services.oaipmh;

import io.gdcc.xoai.model.oaipmh.results.Description;
import io.gdcc.xoai.model.oaipmh.verbs.Identify;
import io.gdcc.xoai.model.oaipmh.verbs.Verb;
import io.gdcc.xoai.xml.XmlWriter;
import io.gdcc.xoai.xmlio.exceptions.XmlWriteException;

import java.util.Iterator;

/**
 * This class is a wrapper to fix Identify response - so that granularity of the earliest datestamp is the same
 * as declared by the repository itself.
 */
public class IdentifyWrapper implements Verb {
    private final Identify verb;

    public IdentifyWrapper(Identify verb) {
        this.verb = verb;
    }

    @Override
    public Type getType() {
        return verb.getType();
    }

    @Override
    public void write(XmlWriter writer) throws XmlWriteException {
        if (verb.getRepositoryName() == null) {
            throw new XmlWriteException("Repository Name cannot be null");
        } else if (verb.getBaseURL() == null) {
            throw new XmlWriteException("Base URL cannot be null");
        } else if (verb.getProtocolVersion() == null) {
            throw new XmlWriteException("Protocol version cannot be null");
        } else if (verb.getEarliestDatestamp() == null) {
            throw new XmlWriteException("Eerliest datestamp cannot be null");
        } else if (verb.getDeletedRecord() == null) {
            throw new XmlWriteException("Deleted record persistency cannot be null");
        } else if (verb.getGranularity() == null) {
            throw new XmlWriteException("Granularity cannot be null");
        } else if (verb.getAdminEmails() != null && !verb.getAdminEmails().isEmpty()) {
            writer.writeElement("repositoryName", verb.getRepositoryName());
            writer.writeElement("baseURL", verb.getBaseURL());
            writer.writeElement("protocolVersion", verb.getProtocolVersion());
            Iterator var2 = verb.getAdminEmails().iterator();

            String compression;
            while(var2.hasNext()) {
                compression = (String)var2.next();
                writer.writeElement("adminEmail", compression);
            }

            //THIS IS THE FIX OF THE ORIGINAL LIBRARY
            writer.writeElement("earliestDatestamp", verb.getEarliestDatestamp(), verb.getGranularity());
            writer.writeElement("deletedRecord", verb.getDeletedRecord().value());
            writer.writeElement("granularity", verb.getGranularity().toString());
            if (!verb.getCompressions().isEmpty()) {
                var2 = verb.getCompressions().iterator();

                while(var2.hasNext()) {
                    compression = (String)var2.next();
                    writer.writeElement("compression", compression);
                }
            }

            if (!verb.getDescriptions().isEmpty()) {
                var2 = verb.getDescriptions().iterator();

                while(var2.hasNext()) {
                    Description description = (Description)var2.next();
                    writer.writeElement("description", description);
                }
            }

        } else {
            throw new XmlWriteException("List of admin emails cannot be null or empty");
        }
    }
}
