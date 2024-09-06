package eu.sshopencloud.marketplace.controllers.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
public class ItemControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String CONTRIBUTOR_JWT;
    private String ADMINISTRATOR_JWT;
    private String MODERATOR_JWT;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
    }

    @Test
    public void shouldGetDraftsInTimeDescendingOrder() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(1L);
        tool.setSource(source);
        tool.setSourceItemId("000000");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services?draft=true")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to test search by source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));

        ToolCore tool2 = new ToolCore();
        tool2.setLabel("Tool second");
        tool2.setVersion("10.0");
        tool2.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(2L);
        tool2.setSource(source2);
        tool2.setSourceItemId("000000");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool2);
        log.debug("JSON: " + payload2);


        mvc.perform(post("/api/tools-services?draft=true")
                        .content(payload2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(tool2.getLabel())))
                .andExpect(jsonPath("description", is(tool2.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")));

        mvc.perform(get("/api/draft-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].label", is(tool2.getLabel())))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("items[1].label", is(tool.getLabel())))
                .andExpect(jsonPath("items[1].lastInfoUpdate", notNullValue()));
    }

    @Test
    public void shouldGetContributedItems() throws Exception {

        mvc.perform(get("/api/contributed-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("order", "label")
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("items[0].id", is(9)))
                .andExpect(jsonPath("items[1].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("items[1].id", is(12)))
                .andExpect(jsonPath("items[2].persistentId", is("dU0BZc")))
                .andExpect(jsonPath("items[2].id", is(11)));

    }

    @Test
    public void shouldCreateApproveAndGetContributedItems() throws Exception {

        mvc.perform(get("/api/contributed-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("order", "label")
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("items[0].id", is(9)))
                .andExpect(jsonPath("items[1].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("items[1].id", is(12)))
                .andExpect(jsonPath("items[2].persistentId", is("dU0BZc")))
                .andExpect(jsonPath("items[2].id", is(11)));

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A first");
        dataset.setDescription("Lorem ipsum");

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        String response = mvc.perform(post("/api/datasets")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("A first")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        String datasetPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(response, DatasetDto.class).getPersistentId();

        long datasetId = TestJsonMapper.serializingObjectMapper()
                .readValue(response, DatasetDto.class).getId();

        mvc.perform(get("/api/contributed-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("order", "label")
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(4)))
                .andExpect(jsonPath("items[0].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("items[0].id", is( (int) datasetId)))
                .andExpect(jsonPath("items[1].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("items[1].id", is(9)))
                .andExpect(jsonPath("items[2].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("items[2].id", is(12)))
                .andExpect(jsonPath("items[3].persistentId", is("dU0BZc")))
                .andExpect(jsonPath("items[3].id", is(11)));


        dataset.setLabel("A first ");
        String payloadUpdate = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payloadUpdate);

        String responseUpdated = mvc.perform(put("/api/datasets/{id}",datasetPersistentId)
                        .content(payloadUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("A first ")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        long datasetIdUpdated = TestJsonMapper.serializingObjectMapper()
                .readValue(responseUpdated, DatasetDto.class).getId();

        mvc.perform(get("/api/contributed-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("order", "label")
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(4)))
                .andExpect(jsonPath("items[0].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("items[0].id", is( (int) datasetIdUpdated)))
                .andExpect(jsonPath("items[1].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("items[1].id", is(9)))
                .andExpect(jsonPath("items[2].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("items[2].id", is(12)))
                .andExpect(jsonPath("items[3].persistentId", is("dU0BZc")))
                .andExpect(jsonPath("items[3].id", is(11)));

    }
}
