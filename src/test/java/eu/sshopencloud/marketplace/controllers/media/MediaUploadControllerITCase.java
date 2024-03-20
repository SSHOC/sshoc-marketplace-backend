package eu.sshopencloud.marketplace.controllers.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
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
@DirtiesContext
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

    //With content-type in heads
    @Test
    public void shouldUploadBmpImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "bmp_example.bmp", "image/bmp", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("bmp_example.bmp")))
                .andExpect(jsonPath("mimeType", is("image/bmp")))
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
    public void shouldUploadGiffImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "gif_example.gif", "image/gif", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("gif_example.gif")))
                .andExpect(jsonPath("mimeType", is("image/gif")))
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
    public void shouldUploadJpegImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "jpeg_example.jpeg", "image/jpeg", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("jpeg_example.jpeg")))
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
    public void shouldUploadPngImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "png_example.png", "image/png", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("png_example.png")))
                .andExpect(jsonPath("mimeType", is("image/png")))
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
    public void shouldUploadPngImageMediaFileWithTransparentBackground() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "png_with_transparent_bg", "image/png", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("png_with_transparent_bg")))
                .andExpect(jsonPath("mimeType", is("image/png")))
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
    public void shouldUploadSvgImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "svg_example.svg", "image/svg+xml", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("svg_example.svg")))
                .andExpect(jsonPath("mimeType", is("image/svg+xml")))
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
    public void shouldUploadTiffImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "tiff_example.tiff", "image/tiff", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("tiff_example.tiff")))
                .andExpect(jsonPath("mimeType", is("image/tiff")))
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
    public void shouldUploadWebpImageMediaFile() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "webp_example.webp", "image/webp", mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("webp_example.webp")))
                .andExpect(jsonPath("mimeType", is("image/webp")))
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
    public void shouldUploadBmpImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "bmp_example.bmp", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("bmp_example.bmp")))
                .andExpect(jsonPath("mimeType", is("image/bmp")))
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
    public void shouldUploadGiffImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "gif_example.gif", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("gif_example.gif")))
                .andExpect(jsonPath("mimeType", is("image/gif")))
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
    public void shouldUploadJpegImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "jpeg_example.jpeg", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("jpeg_example.jpeg")))
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
    public void shouldUploadPngImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "png_example.png", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("png_example.png")))
                .andExpect(jsonPath("mimeType", is("image/png")))
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
    public void shouldUploadSvgImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "svg_example.svg", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("svg_example.svg")))
                .andExpect(jsonPath("mimeType", is("image/svg+xml")))
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
    public void shouldUploadTiffImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "tiff_example.tiff", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("tiff_example.tiff")))
                .andExpect(jsonPath("mimeType", is("image/tiff")))
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
    public void shouldUploadWebpImageMediaFileWithoutContentType() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        MockMultipartFile mediaFile = new MockMultipartFile(
                "file", "webp_example.webp", null, mediaStream
        );

        String response = mvc.perform(
                multipart("/api/media/upload/full")
                        .file(mediaFile)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", notNullValue()))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("webp_example.webp")))
                .andExpect(jsonPath("mimeType", is("image/webp")))
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
        String localYoutubeUrl = String.format("http://localhost:%d", wireMockRule.port());
        String youtubeVideoUrl = "/youtube/watch?v=r8mtXJh3hzM&ab_channel=VoxxedDaysVienna";
        URL videoUrl = new URL(localYoutubeUrl + youtubeVideoUrl);

        MediaSourceCore mediaSource = new MediaSourceCore("local-youtube", localYoutubeUrl, MediaCategory.VIDEO, 3);
        String mediaSourcePayload = mapper.writeValueAsString(mediaSource);

        mvc.perform(
                post("/api/media-sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mediaSourcePayload)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());

        stubFor(
                WireMock.head(urlEqualTo(youtubeVideoUrl))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "text/html")
                )
        );

        MediaLocation mediaLocation = MediaLocation.builder().sourceUrl(videoUrl).build();
        String mediaPayload = mapper.writeValueAsString(mediaLocation);

        String response = mvc.perform(
                post("/api/media/upload/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mediaPayload)
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
    public void shouldImportBmpImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/bmp_example.bmp");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/bmp"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/bmp"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/bmp");
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
                .andExpect(jsonPath("mimeType", is("image/bmp")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/bmp")))
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
    public void shouldImportGifImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/gif_example.gif");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/gif"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/gif"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/gif");
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
                .andExpect(jsonPath("mimeType", is("image/gif")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/gif")))
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
    public void shouldImportJpegImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/jpeg_example.jpeg");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/jpeg"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/jpeg"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/jpeg");
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
                .andExpect(jsonPath("mimeType", is("image/jpeg")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/jpeg")))
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
    public void shouldImportPngImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/png_example.png");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/png"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/png"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/png");
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
    public void shouldImportSvgImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/svg_example.svg");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/svg"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/svg"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/svg");
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
                .andExpect(jsonPath("mimeType", is("image/svg+xml")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/svg+xml")))
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
    public void shouldImportTiffImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/tiff_example.tiff");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/tiff"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/tiff"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/tiff");
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
                .andExpect(jsonPath("mimeType", is("image/tiff")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/tiff")))
                .andExpect(jsonPath("hasThumbnail", is(true)));

        //mvc.perform(get("/api/media/download/{mediaId}", mediaId)).andExpect(status().isBadRequest());

        byte[] thumbnail = mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andReturn().getResponse().getContentAsByteArray();

        ImageIO.read(new ByteArrayInputStream(thumbnail));
    }

    @Test
    public void shouldImportWebpImageMediaWithoutContentType() throws Exception {
        InputStream imageStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/webp_example.webp");
        byte[] imageContent = FileCopyUtils.copyToByteArray(imageStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "")
                .withHeader("Content-Length", String.valueOf(imageContent.length));

        stubFor(
                WireMock.head(urlEqualTo("/webp"))
                        .willReturn(grumpyResponse)
        );

        stubFor(
                WireMock.get(urlEqualTo("/webp"))
                        .willReturn(grumpyResponse.withBody(imageContent))
        );

        URL imageUrl = new URL("http", "localhost", wireMockRule.port(), "/webp");
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
                .andExpect(jsonPath("mimeType", is("image/webp")))
                .andExpect(jsonPath("hasThumbnail", is(true)))
                .andReturn().getResponse().getContentAsString();

        MediaDetails details = mapper.readValue(response, MediaDetails.class);
        UUID mediaId = details.getMediaId();

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("location.sourceUrl", is(imageUrl.toString())))
                .andExpect(jsonPath("mimeType", is("image/webp")))
                .andExpect(jsonPath("hasThumbnail", is(true)));

        mvc.perform(get("/api/media/download/{mediaId}", mediaId)).andExpect(status().isBadRequest());

        byte[] thumbnail = mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andReturn().getResponse().getContentAsByteArray();

        ImageIO.read(new ByteArrayInputStream(thumbnail));
    }


    @Test
    public void shouldImportImageMediaWithoutContentType() throws Exception {
        InputStream grumpyStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/grumpycat.png");
        byte[] grumpyContent = FileCopyUtils.copyToByteArray(grumpyStream);

        ResponseDefinitionBuilder grumpyResponse = aResponse()
                .withStatus(200)
                //.withHeader("Content-Type", "image/png")
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
    public void shouldUploadImageInChunks() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/seriouscat.jpg");
        byte[] mediaContent = FileCopyUtils.copyToByteArray(mediaStream);

        UUID mediaId = uploadMediaFileInChunks(
                mediaContent, "seriouscat.jpg", Optional.of(MediaType.IMAGE_JPEG), 5, MODERATOR_JWT
        );

        byte[] downloadedContent = mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertArrayEquals(mediaContent, downloadedContent);

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("image")))
                .andExpect(jsonPath("filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("mimeType", is("image/jpeg")))
                .andExpect(jsonPath("hasThumbnail", is(true)));

        byte[] thumbnail = mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        ImageIO.read(new ByteArrayInputStream(thumbnail));
    }

    @Test
    public void shouldUploadPdfInChunks() throws Exception {
        InputStream mediaStream = MediaUploadControllerITCase.class.getResourceAsStream("/initial-data/media/jwt-handbook.pdf");
        byte[] mediaContent = FileCopyUtils.copyToByteArray(mediaStream);

        UUID mediaId = uploadMediaFileInChunks(
                mediaContent, "jwt-handbook.pdf", Optional.of(MediaType.APPLICATION_PDF), 10, CONTRIBUTOR_JWT
        );

        byte[] downloadedContent = mvc.perform(get("/api/media/download/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertArrayEquals(mediaContent, downloadedContent);

        mvc.perform(get("/api/media/info/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("category", is("object")))
                .andExpect(jsonPath("filename", is("jwt-handbook.pdf")))
                .andExpect(jsonPath("mimeType", is("application/pdf")))
                .andExpect(jsonPath("hasThumbnail", is(false)));

        mvc.perform(get("/api/media/thumbnail/{mediaId}", mediaId))
                .andExpect(status().isNotFound());
    }

    private UUID uploadMediaFileInChunks(byte[] content, String filename, Optional<MediaType> mimeType, int chunks, String user)
            throws Exception {

        int chunkSize = (content.length + chunks - 1) / chunks;
        String contentType = mimeType.map(MediaType::toString).orElse(null);
        UUID mediaId = null;

        for (int i = 0; i * chunkSize < content.length; ++i) {
            int offset = i * chunkSize;
            byte[] range = Arrays.copyOfRange(content, offset, Math.min(offset + chunkSize, content.length));

            MockMultipartFile chunkFile = new MockMultipartFile("chunk", filename, contentType, range);
            MockHttpServletRequestBuilder uploadRequest = multipart("/api/media/upload/chunk")
                    .file(chunkFile)
                    .header("Authorization", user)
                    .param("no", Integer.toString(i));

            if (mediaId != null)
                uploadRequest = uploadRequest.param("mediaId", mediaId.toString());

            String response = mvc.perform(uploadRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("mediaId", notNullValue()))
                    .andExpect(jsonPath("filename", is(filename)))
                    .andExpect(jsonPath("nextChunkNo", is(i + 1)))
                    .andReturn().getResponse().getContentAsString();

            MediaUploadInfo uploadInfo = mapper.readValue(response, MediaUploadInfo.class);
            mediaId = uploadInfo.getMediaId();
        }

        mvc.perform(
                post("/api/media/upload/complete/{mediaId}", mediaId)
                        .param("filename", filename)
                        .header("Authorization", user)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("mediaId", is(mediaId.toString())))
                .andExpect(jsonPath("filename", is(filename)));

        return mediaId;
    }
}
