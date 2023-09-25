package eu.sshopencloud.marketplace.controllers.oaipmh;

import eu.sshopencloud.marketplace.services.oaipmh.OaiPmhDataProviderService;
import io.gdcc.xoai.dataprovider.request.RequestBuilder;
import io.gdcc.xoai.exceptions.BadVerbException;
import io.gdcc.xoai.exceptions.OAIException;
import io.gdcc.xoai.model.oaipmh.Error;
import io.gdcc.xoai.model.oaipmh.OAIPMH;
import io.gdcc.xoai.xml.XmlWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/oai-pmh")
@RequiredArgsConstructor
public class OaiPmhDataProviderController {
    private final OaiPmhDataProviderService oaiService;

    @RequestMapping(value = "/oai-pmh-repository.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String handle(@RequestParam MultiValueMap<String, String> allRequestParams) throws XMLStreamException {
        Map<String, String[]> listedMap = new HashMap<>();
        allRequestParams.forEach((key, value) -> listedMap.put(key, value.toArray(new String[0])));
        RequestBuilder.RawRequest oaiRequest;
        try {
            oaiRequest = RequestBuilder.buildRawRequest(listedMap);
            if (oaiRequest.hasErrors()) {
                OAIPMH oaiPmhResponse = new OAIPMH();
                oaiRequest.getErrors().forEach(error -> oaiPmhResponse.withError(
                        new Error("Invalid request!").withCode(error.getErrorCode())));
                return getOaipmhResponse(oaiPmhResponse);
            }
        } catch (BadVerbException e) {
            OAIPMH oaiPmhResponse = new OAIPMH().withError(
                    new Error("Invalid verb provided!").withCode(Error.Code.BAD_VERB));
            return getOaipmhResponse(oaiPmhResponse);
        } catch (OAIException e) {
            throw new RuntimeException(e);
        }
        return oaiService.handle(oaiRequest);
    }

    private static String getOaipmhResponse(OAIPMH oaiPmhResponse) throws XMLStreamException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        XmlWriter xmlWriter = new XmlWriter(byteOutputStream);
        oaiPmhResponse.write(xmlWriter);
        xmlWriter.flush();
        return byteOutputStream.toString();
    }
}
