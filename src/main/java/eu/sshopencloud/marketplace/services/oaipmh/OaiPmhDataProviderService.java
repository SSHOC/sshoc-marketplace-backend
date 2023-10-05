package eu.sshopencloud.marketplace.services.oaipmh;

import eu.sshopencloud.marketplace.repositories.oaipmh.OaiPmhItemRepository;
import io.gdcc.xoai.dataprovider.DataProvider;
import io.gdcc.xoai.dataprovider.exceptions.InternalOAIException;
import io.gdcc.xoai.dataprovider.model.Context;
import io.gdcc.xoai.dataprovider.repository.Repository;
import io.gdcc.xoai.dataprovider.repository.RepositoryConfiguration;
import io.gdcc.xoai.dataprovider.request.RequestBuilder;
import io.gdcc.xoai.model.oaipmh.DeletedRecord;
import io.gdcc.xoai.model.oaipmh.Error;
import io.gdcc.xoai.model.oaipmh.Granularity;
import io.gdcc.xoai.model.oaipmh.OAIPMH;
import io.gdcc.xoai.model.oaipmh.verbs.Identify;
import io.gdcc.xoai.model.oaipmh.verbs.Verb;
import io.gdcc.xoai.services.impl.SimpleResumptionTokenFormat;
import io.gdcc.xoai.xml.XmlWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class OaiPmhDataProviderService {
    protected static final String FORMAT = "oai_dc";
    private final OaiPmhItemRepository oaiItemRepository;
    private final String repositoryName;
    private final String[] adminEmails;
    private final String repositoryBaseUrl;
    private final int repositoryMaxListIdentifiers;
    private final int repositoryMaxListRecords;
    private final String description;

    protected DataProvider dataProvider;


    @Autowired
    public OaiPmhDataProviderService(OaiPmhItemRepository oaiItemRepository,
            @Value("${marketplace.oai-pmh-data-provider.name}") String oaiRepositoryName,
            @Value("${marketplace.oai-pmh-data-provider.adminEmails}") String[] oaiRepositoryAdminEmails,
            @Value("${marketplace.oai-pmh-data-provider.baseUrl}") String oaiRepositoryBaseUrl,
            @Value("${marketplace.oai-pmh-data-provider.max.list.identifiers}") int oaiRepositoryMaxListIdentifiers,
            @Value("${marketplace.oai-pmh-data-provider.max.list.records}") int oaiRepositoryMaxListRecords,
            @Value("${marketplace.oai-pmh-data-provider.description}") String description) {
        this.oaiItemRepository = oaiItemRepository;
        this.repositoryName = oaiRepositoryName;
        this.adminEmails = oaiRepositoryAdminEmails;
        this.repositoryBaseUrl = oaiRepositoryBaseUrl;
        this.repositoryMaxListIdentifiers = oaiRepositoryMaxListIdentifiers;
        this.repositoryMaxListRecords = oaiRepositoryMaxListRecords;
        this.description = description;
    }

    @PostConstruct
    public void setupOaiProvider() {
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration.RepositoryConfigurationBuilder().withRepositoryName(
                        repositoryName).withAdminEmails(adminEmails).withBaseUrl(repositoryBaseUrl)
                .withMaxListIdentifiers(repositoryMaxListIdentifiers).withMaxListRecords(repositoryMaxListRecords)
                // we do not support sets, but needs to be set because otherwise XOAI raises error
                .withMaxListSets(100).withEarliestDate(setupProviderMinDate()).withGranularity(Granularity.Day)
                .withDeleteMethod(DeletedRecord.NO).withDescription(description)
                .withResumptionTokenFormat(new SimpleResumptionTokenFormat()).build();
        Context context = new Context().withMetadataFormat(FORMAT, createIdentity()).withTransformer(createIdentity());
        // no support for sets, only item repository is supported in a limited way
        Repository repository = new Repository(repositoryConfiguration).withSetRepository(List::of)
                .withItemRepository(oaiItemRepository);
        this.dataProvider = new DataProvider(context, repository);
    }

    private Transformer createIdentity() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance(
                    "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            return factory.newTransformer();
        } catch (TransformerConfigurationException var1) {
            throw new InternalOAIException("Could not setup the identity transformer", var1);
        }
    }

    public String handle(RequestBuilder.RawRequest oaiRequest) throws XMLStreamException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        XmlWriter xmlWriter = new XmlWriter(byteOutputStream);
        OAIPMH oaiPmhResponse;
        try {
            oaiPmhResponse = dataProvider.handle(oaiRequest);
            // this is to fix error in identify verb of XOAI library - we use here granularity declared by the repository
            // while default XOAI library uses hard-coded seconds granularity
            if (Verb.Type.Identify.equals(oaiRequest.getVerb()) && oaiPmhResponse.getVerb() instanceof Identify) {
                oaiPmhResponse.withVerb(new IdentifyWrapper((Identify) oaiPmhResponse.getVerb()));
            }
        } catch (IllegalArgumentException e) {
            Arrays.stream(e.getStackTrace())
                    .filter(element -> element.getClassName().equals(SimpleResumptionTokenFormat.class.getName()))
                    .findFirst().orElseThrow(() -> e);
            oaiPmhResponse = new OAIPMH().withError(
                    new Error("Invalid resumption token provided!").withCode(Error.Code.BAD_RESUMPTION_TOKEN));
        }
        oaiPmhResponse.write(xmlWriter);
        xmlWriter.flush();
        return byteOutputStream.toString();
    }

    private Instant setupProviderMinDate() {
        return oaiItemRepository.getMinDate().orElse(Instant.now()).truncatedTo(ChronoUnit.DAYS);
    }
}