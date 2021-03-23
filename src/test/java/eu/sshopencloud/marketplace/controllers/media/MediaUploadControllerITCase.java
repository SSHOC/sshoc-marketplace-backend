package eu.sshopencloud.marketplace.controllers.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MediaUploadControllerITCase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;


    @Before
    public void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }


    @Test
    public void shouldUploadImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "seriouscat.jpg", "image/jpeg", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("mimeType", is("image/jpeg")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();
        
        byte[] mediaContent = mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertArrayEquals(mediaFile.getBytes(), mediaContent);

        mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    public void shouldUploadImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "seriouscat.jpg", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("mimeType", is("image/jpeg")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        byte[] mediaContent = mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertArrayEquals(mediaFile.getBytes(), mediaContent);

        byte[] thumbnail = mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andReturn().getResponse().getContentAsByteArray();

        ImageIO.read(new ByteArrayInputStream(thumbnail));
    }

    @Test
    public void shouldUploadTextMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream(
                "/initial-data/media/sample-2mb-text-file.txt"
        );

        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "sample-2mb-text-file.txt", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("object")))
                .andExpect(jsonPath("filename", is("sample-2mb-text-file.txt")))
                .andExpect(jsonPath("hasThumbnail", is(false)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        byte[] mediaContent = mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertArrayEquals(mediaFile.getBytes(), mediaContent);

        mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldImportVideoMedia() throws Exception {
        URL videoUrl = new URL("https://www.youtube.com/watch?v=r8mtXJh3hzM&ab_channel=VoxxedDaysVienna");
        MediaLocation mediaLocation = MediaLocation.builder().sourceUrl(videoUrl).build();

        String payload = mapper.writeValueAsString(mediaLocation);

        String response = mvc.perform(
                post("/api/media/upload/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("video")))
                .andExpect(jsonPath("location.sourceUrl", is(videoUrl.toString())))
                .andExpect(jsonPath("hasThumbnail", is(false))) // Possibly the streaming services provide thumbnails
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("video")))
                .andExpect(jsonPath("location.sourceUrl", is(videoUrl.toString())))
                .andExpect(jsonPath("hasThumbnail", is(false)));

        mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldImportImageMedia() throws Exception {
        InputStream grumpyStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/grumpycat.png");
        byte[] grumpyContent = FileCopyUtils.copyToByteArray(grumpyStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withHeader("Content-Length", String.valueOf(grumpyContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/grumpy"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/grumpy"))
                        .willReturn(grumpyResponse.withBody(grumpyContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/grumpy");
        MediaLocation mediaLocation = MediaLocation.builder().sourceUrl(imageUrl).build();

        String payload = mapper.writeValueAsString(mediaLocation);

        String response = mvc.perform(
                post("/api/media/upload/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/png")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/png")))
                .andExpect(jsonPath("hasThumbnail", is(true)));

        mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isBadRequest());

        byte[] thumbnail = mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andReturn().getResponse().getContentAsByteArray();

        ImageIO.read(new ByteArrayInputStream(thumbnail));
    }

    @Test
    public void shouldUploadImageInChunks() {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Test
    public void shouldUploadPdfInChunks() {
        throw new RuntimeException("Not implemented");
        // TODO
    }

    private void uploadMediFileInChunks(Resource mediaResource) {
        // TODO
    }
}
