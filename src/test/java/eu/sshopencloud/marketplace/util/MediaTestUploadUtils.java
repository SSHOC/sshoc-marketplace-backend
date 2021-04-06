package eu.sshopencloud.marketplace.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.sshopencloud.marketplace.controllers.media.MediaUploadControllerITCase;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@UtilityClass
public class MediaTestUploadUtils {

    public UUID uploadMedia(MockMvc mvc, ObjectMapper mapper, String mediaFilename, String userJwt) throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream(
                String.format("/initial-data/media/%s", mediaFilename)
        );

        MockMultipartFile mediaFile = new MockMultipartFile("file", mediaFilename, null, mediaStream);

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", userJwt)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        MediaDetails mediaDetails = mapper.readValue(response, MediaDetails.class);

        return mediaDetails.getMediaId();
    }

    public UUID importMedia(MockMvc mvc, ObjectMapper mapper, WireMockRule wireMockRule,
                            String mediaFilename, String contentType, String userJwt) throws Exception {

        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream(
                String.format("/initial-data/media/%s", mediaFilename)
        );

        byte[] mediaContent = FileCopyUtils.copyToByteArray(mediaStream);

        String mediaPath = "/" + mediaFilename;
        ResponseDefinitionBuilder mediaResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", contentType)
                .withHeader("Content-Length", String.valueOf(mediaContent.length))
                .withHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", mediaFilename));

        stubFor(
                WireMock.head(urlEqualTo(mediaPath))
                        .willReturn(mediaResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo(mediaPath))
                        .willReturn(mediaResponse.withBody(mediaContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), mediaPath);
        MediaLocation mediaLocation = MediaLocation.builder().sourceUrl(imageUrl).build();

        String payload = mapper.writeValueAsString(mediaLocation);

        String response = mvc.perform(
                post("/api/media/upload/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", userJwt)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        MediaDetails mediaDetails = mapper.readValue(response, MediaDetails.class);

        return mediaDetails.getMediaId();
    }
}
