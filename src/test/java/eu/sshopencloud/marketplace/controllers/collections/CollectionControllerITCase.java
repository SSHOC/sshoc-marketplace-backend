package eu.sshopencloud.marketplace.controllers.collections;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
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
class CollectionControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String CONTRIBUTOR_JWT;
    private String IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;


    @BeforeEach
    void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    void shouldReturnCollections() throws Exception {

        mvc.perform(get("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateSimpleCollection() throws Exception {
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));
    }

    @Test
    void shouldReadCreatedCollection() throws Exception {
        //create collection
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        String jsonResponse = mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        long createdCollectionId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, CollectionDto.class).getId();

        //read collection
        mvc.perform(get("/api/collections/{id}", createdCollectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

    }

    @Test
    void shouldModifyExistingCollection() throws Exception {
        //create collection
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        String jsonResponse = mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        long createdCollectionId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, CollectionDto.class).getId();

        //modify collection
        collection.setTitle("Modified collection");
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}",createdCollectionId)
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Modified collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        //read collection
        mvc.perform(get("/api/collections/{id}", createdCollectionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Modified collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));
    }
}