package eu.sshopencloud.marketplace.controllers.sources;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class SourceControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;

    @Before
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    public void shouldReturnSources() throws Exception {

        mvc.perform(get("/api/sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnSourcesBySortedByLabel() throws Exception {

        SourceCore source = new SourceCore();
        source.setLabel("Source Test");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/sources?order=NAME")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("sources[0].label", notNullValue()))
                .andExpect(jsonPath("sources[1].label", is(source.getLabel())))
                .andExpect(jsonPath("sources[1].url", is(source.getUrl())))
                .andExpect(jsonPath("sources[2].label", is("TAPoR")))
                .andExpect(jsonPath("sources[2].url", is("http://tapor.ca")))
                .andExpect(jsonPath("sources[2].urlTemplate", is("http://tapor.ca/tools/{source-item-id}")));
    }

    //Eliza
    @Test
    public void shouldReturnSourcesBySortedByDate() throws Exception {

        SourceCore source = new SourceCore();
        source.setLabel("Test");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/sources?order=DATE")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)));
               // .andExpect(jsonPath("sources[0].label", is(source.getLabel())))
                //.andExpect(jsonPath("sources[0].url", is(source.getUrl())));
    }

    @Test
    public void shouldReturnSourcesByLabel() throws Exception {

        mvc.perform(get("/api/sources?q=tapor")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("sources[0].label", is("TAPoR")))
                .andExpect(jsonPath("sources[0].url", is("http://tapor.ca")))
                .andExpect(jsonPath("sources[0].urlTemplate", is("http://tapor.ca/tools/{source-item-id}")));
    }

    @Test
    public void shouldReturnSourcesByPartOfUrl() throws Exception {

        mvc.perform(get("/api/sources?q=historian")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("sources[0].label", is("Programming Historian")))
                .andExpect(jsonPath("sources[0].url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sources[0].urlTemplate", is("https://programminghistorian.org/en/lessons/{source-item-id}")));
    }

    @Test
    public void shouldReturnSource() throws Exception {
        Integer sourceId = 1;

        mvc.perform(get("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(sourceId)))
                .andExpect(jsonPath("label", is("TAPoR")))
                .andExpect(jsonPath("url", is("http://tapor.ca")))
                .andExpect(jsonPath("urlTemplate", is("http://tapor.ca/tools/{source-item-id}")));
    }

    @Test
    public void shouldNotReturnSourceWhenNotExist() throws Exception {
        Integer sourceId = -1;

        mvc.perform(get("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotCreateSourceWhenUrlIsMalformed() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("url")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateSourceWhenUrlTemplateHasNoSourceItemId() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/xxx/yyy/zzz");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("urlTemplate")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldCreateUpdateAndDeleteSource() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long sourceId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, SourceDto.class).getId();

        source = new SourceCore();
        source.setLabel("Test another source");
        source.setUrl("http://other.example.com");
        source.setUrlTemplate("http://other.example.com/{source-item-id}");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/sources/{id}", sourceId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(sourceId.intValue())))
                .andExpect(jsonPath("label", is("Test another source")))
                .andExpect(jsonPath("url", is("http://other.example.com")))
                .andExpect(jsonPath("urlTemplate", is("http://other.example.com/{source-item-id}")))
                .andExpect(jsonPath("lastHarvestedDate", nullValue()));

        mvc.perform(delete("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }

}
