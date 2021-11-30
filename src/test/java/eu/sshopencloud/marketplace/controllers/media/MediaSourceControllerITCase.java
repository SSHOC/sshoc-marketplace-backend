package eu.sshopencloud.marketplace.controllers.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MediaSourceControllerITCase {

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
    public void shouldReturnAllMediaSources() throws Exception {
        mvc.perform(get("/api/media-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("youtube")))
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("vimeo")))
                .andExpect(jsonPath("$[1].ord", is(2)));
    }

    @Test
    public void shouldCreateMediaSource() throws Exception {
        MediaSourceCore itemSource = MediaSourceCore.builder()
                .code("google-images")
                .serviceUrl("https://www.google.com/imgres")
                .mediaCategory(MediaCategory.IMAGE)
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/media-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("google-images")))
                .andExpect(jsonPath("serviceUrl", is("https://www.google.com/imgres")))
                .andExpect(jsonPath("mediaCategory", is("image")));

        mvc.perform(get("/api/media-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].code", is("youtube")))
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("google-images")))
                .andExpect(jsonPath("$[1].serviceUrl", is("https://www.google.com/imgres")))
                .andExpect(jsonPath("$[1].mediaCategory", is("image")))
                .andExpect(jsonPath("$[1].ord", is(2)))
                .andExpect(jsonPath("$[2].code", is("vimeo")))
                .andExpect(jsonPath("$[2].ord", is(3)));
    }

    @Test
    public void shouldNotCreateMediaSourceAtWrongPosition() throws Exception {
        MediaSourceCore itemSource = MediaSourceCore.builder()
                .code("test")
                .serviceUrl("http://test.org")
                .mediaCategory(MediaCategory.OBJECT)
                .ord(50)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/media-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRetrieveMediaSource() throws Exception {
        mvc.perform(get("/api/media-sources/vimeo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("vimeo")))
                .andExpect(jsonPath("serviceUrl", is("https://vimeo.com/watch")))
                .andExpect(jsonPath("mediaCategory", is("embed")));
    }

    @Test
    public void shouldUpdateMediaSource() throws Exception {
        MediaSourceCore itemSource = MediaSourceCore.builder()
                .code("youtube")
                .mediaCategory(MediaCategory.VIDEO)
                .serviceUrl("http://youtube.com/watch")
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                put("/api/media-sources/{sourceId}", "youtube")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("youtube")))
                .andExpect(jsonPath("mediaCategory", is("video")))
                .andExpect(jsonPath("serviceUrl", is("http://youtube.com/watch")));

        mvc.perform(get("/api/media-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("vimeo")))
                .andExpect(jsonPath("$[1].code", is("youtube")))
                .andExpect(jsonPath("$[1].serviceUrl", is("http://youtube.com/watch")));
    }

    @Test
    public void shouldRemoveMediaSource() throws Exception {
        mvc.perform(
                delete("/api/media-sources/{sourceId}", "youtube")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(get("/api/media-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("vimeo")));
    }

    @Test
    public void shouldNotCreateMediaSourceUnauthorized() throws Exception {
        MediaSourceCore itemSource = MediaSourceCore.builder()
                .code("test")
                .mediaCategory(MediaCategory.OBJECT)
                .serviceUrl("https://tor-files.gov")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/media-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotUpdateMediaSourceUnauthorized() throws Exception {
        MediaSourceCore itemSource = MediaSourceCore.builder()
                .code("vimeo")
                .mediaCategory(MediaCategory.OBJECT)
                .serviceUrl("http://vimeo.com/watch")
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                put("/api/media-sources/{sourceId}", "vimeo")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotDeleteMediaSourceUnauthorized() throws Exception {
        mvc.perform(delete("/api/media-sources/{sourceId}", "vimeo"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateMediaSourceAsAdministrator() throws Exception {
        MediaSourceCore itemSource = MediaSourceCore.builder()
                .code("twitch")
                .serviceUrl("https://www.twitch.tv")
                .mediaCategory(MediaCategory.VIDEO)
                .ord(3)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/media-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(get("/api/media-sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].code", is("youtube")))
                .andExpect(jsonPath("$[1].code", is("vimeo")))
                .andExpect(jsonPath("$[2].code", is("twitch")));
    }
}
