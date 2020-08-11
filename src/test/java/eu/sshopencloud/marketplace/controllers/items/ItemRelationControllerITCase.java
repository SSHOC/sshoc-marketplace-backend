package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class ItemRelationControllerITCase {

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
    public void shouldReturnAllItemRelations() throws Exception {

        mvc.perform(get("/api/items-relations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].code", is("relates-to")))
                .andExpect(jsonPath("$[1].code", is("is-related-to")))
                .andExpect(jsonPath("$[2].code", is("documents")))
                .andExpect(jsonPath("$[3].code", is("is-documented-by")))
                .andExpect(jsonPath("$[4].code", is("mentions")))
                .andExpect(jsonPath("$[5].code", is("is-mentioned-in")))
                .andExpect(jsonPath("$[6].code", is("extends")))
                .andExpect(jsonPath("$[7].code", is("is-extended-by")));
    }

    @Test
    public void shouldCreateItemsRelations() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 2;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("subject.id", is(subjectId)))
                .andExpect(jsonPath("subject.category", is("tool")))
                .andExpect(jsonPath("object.id", is(objectId)))
                .andExpect(jsonPath("object.category", is("tool")))
                .andExpect(jsonPath("relation.code", is("mentions")))
                .andExpect(jsonPath("relation.label", is("Mentions")));
    }

    @Test
    public void shouldNotCreateItemsRelationsWhenRelationIsIncorrect() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 2;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("qwerty");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateItemsRelationsWhenExists() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 3;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", notNullValue()));
    }

    @Test
    public void shouldNotCreateItemsRelationsWhenItemNotExist() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 300;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteItemsRelations() throws Exception {
        Integer subjectId = 2;
        Integer objectId = 11;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotDeleteItemsRelationsWhenItemNotExist() throws Exception {
        Integer subjectId = 2;
        Integer objectId = 40;

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

}
