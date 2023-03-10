package eu.sshopencloud.marketplace.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@UtilityClass
public class VocabularyTestUploadUtils {

    public MockHttpServletRequestBuilder vocabularyUpload(HttpMethod method, MockMultipartFile vocabularyFile,
                                                           String urlTemplate, Object... urlVars) {

        return multipart(urlTemplate, urlVars)
                .file(vocabularyFile)
                .with(request -> {
                    request.setMethod(method.toString());
                    return request;
                });
    }

}
