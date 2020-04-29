package eu.sshopencloud.marketplace.controllers.sources;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import lombok.extern.slf4j.Slf4j;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SourceControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnSources() throws Exception {

        mvc.perform(get("/api/sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnSourcesByLabel() throws Exception {

        mvc.perform(get("/api/sources?q=tapor")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("sources[0].label", is("TAPoR")))
                .andExpect(jsonPath("sources[0].url", is("http://tapor.ca")));
    }

    @Test
    public void shouldReturnSourcesByPartOfUrl() throws Exception {

        mvc.perform(get("/api/sources?q=historian")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("sources[0].label", is("Programming Historian")))
                .andExpect(jsonPath("sources[0].url", is("https://programminghistorian.org")));
    }

    @Test
    public void shouldReturnSource() throws Exception {
        Integer sourceId = 1;

        mvc.perform(get("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(sourceId)))
                .andExpect(jsonPath("label", is("TAPoR")))
                .andExpect(jsonPath("url", is("http://tapor.ca")));
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

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("url")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }


    @Test
    public void shouldCreateUpdateAndDeleteSource() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("http://example.com");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long sourceId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, SourceDto.class).getId();

        source = new SourceCore();
        source.setLabel("Test another source");
        source.setUrl("http://other.example.com");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/sources/{id}", sourceId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(sourceId.intValue())))
                .andExpect(jsonPath("label", is("Test another source")))
                .andExpect(jsonPath("url", is("http://other.example.com")))
                .andExpect(jsonPath("lastHarvestedDate", nullValue()));

        mvc.perform(delete("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
