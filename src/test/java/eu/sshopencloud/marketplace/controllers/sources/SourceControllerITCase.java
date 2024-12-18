package eu.sshopencloud.marketplace.controllers.sources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
public class SourceControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String SYSTEM_IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;

    @BeforeEach
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        SYSTEM_IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
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
    public void shouldReturnSourcesSortedByLabel() throws Exception {

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

        mvc.perform(get("/api/sources?order=name")
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

    @Test
    public void shouldReturnSourcesSortedByDate() throws Exception {

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

        mvc.perform(get("/api/sources?order=date")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)));
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
                .andExpect(jsonPath("urlTemplate", is("http://other.example.com/{source-item-id}")));

        mvc.perform(delete("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }


    @Test
    public void shouldGetItemsBySourceId() throws Exception {
        Long sourceId = 1L;

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("WebSty")));
    }


    @Test
    public void shouldGetItemsBySourceIdAndSourceItemId() throws Exception {
        Long sourceId = 2L;
        String sourceItemId = "rT8gg";

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].persistentId", is("JmBgWa")))
                .andExpect(jsonPath("items[0].category", is("training-material")))
                .andExpect(jsonPath("items[0].label", is("Webinar on DH")));
    }

    @Test
    public void shouldUpdateActorRelatedToItemAngGetThisItem() throws Exception {
        Long sourceId = 2L;
        String sourceItemId = "rT8gg";
        Long actorIdToUpdate = 4L;

        // create datasets that relate to the actor we are going to update
        ItemContributorId contributor = new ItemContributorId(new ActorId(4L), new ActorRoleId("contributor"));

        List<String> datasetsPIDs = new ArrayList<>();

        IntStream.range(1, 11).forEach(i -> {
            DatasetCore dataset = new DatasetCore();
            dataset.setLabel("Test dataset with source and actor " + i);
            dataset.setDescription("Lorem ipsum");
            SourceId source = new SourceId();
            source.setId(sourceId);
            dataset.setSource(source);
            dataset.setSourceItemId(sourceItemId);
            dataset.setContributors(List.of(contributor));

            String payload = null;
            try {
                payload = mapper.writeValueAsString(dataset);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.debug("JSON: " + payload);

            try {
                mvc.perform(post("/api/datasets")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("status", is("approved")))
                        .andExpect(jsonPath("category", is("dataset")))
                        .andExpect(jsonPath("label", is("Test dataset with source and actor " + i)))
                        .andExpect(jsonPath("description", is("Lorem ipsum")))
                        .andExpect(jsonPath("properties", hasSize(0)))
                        .andExpect(jsonPath("source.id", is(2)))
                        .andExpect(jsonPath("source.label", is("Programming Historian")))
                        .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                        .andExpect(jsonPath("sourceItemId", is("rT8gg"))).andDo(h -> {
                            datasetsPIDs.add(mapper.readValue(h.getResponse().getContentAsString(), DatasetDto.class).getPersistentId());
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(1L);
        affiliations.add(affiliation1);
        actor.setAffiliations(affiliations);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/actors/{id}", actorIdToUpdate)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(jsonPath("items", hasSize(11)))
                .andExpect(jsonPath("items[0].persistentId", is(datasetsPIDs.getFirst())))
                .andExpect(jsonPath("items[0].category", is("dataset")))
                .andExpect(jsonPath("items[0].label", is("Test dataset with source and actor 1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(jsonPath("items", hasSize(11)))
                .andExpect(jsonPath("items[0].persistentId", is(datasetsPIDs.getFirst())))
                .andExpect(jsonPath("items[0].category", is("dataset")))
                .andExpect(jsonPath("items[0].label", is("Test dataset with source and actor 1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("items", hasSize(11)))
                .andExpect(jsonPath("items[0].persistentId", is(datasetsPIDs.getFirst())))
                .andExpect(jsonPath("items[0].category", is("dataset")))
                .andExpect(jsonPath("items[0].label", is("Test dataset with source and actor 1")));
    }

}
