package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
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
        String subjectPersistentId = "n21Kfc";
        String objectPersistentId = "DstBL5";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("subject.persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("subject.category", is("tool-or-service")))
                .andExpect(jsonPath("object.persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("object.category", is("tool-or-service")))
                .andExpect(jsonPath("relation.code", is("mentions")))
                .andExpect(jsonPath("relation.label", is("Mentions")));

        mvc.perform(get("/api/tools-services/{id}", subjectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("relatedItems", hasSize(3)))
                .andExpect(jsonPath("relatedItems[0].id", is(3)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")))
                .andExpect(jsonPath("relatedItems[2].persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("relatedItems[2].relation.code", is("mentions")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(1)))
                .andExpect(jsonPath("olderVersions[0].label", is("Gephi")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        mvc.perform(get("/api/tools-services/{id}", objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Stata")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(2)))
                .andExpect(jsonPath("olderVersions[0].label", is("Stata")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldCreateItemsRelationsAsDraft() throws Exception {
        String subjectPersistentId = "n21Kfc";

        ToolCore tool = new ToolCore();
        tool.setLabel("Draft Gephi");
        tool.setDescription("Draft Gephi ...");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}?draft=true", subjectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Draft Gephi")))
                .andExpect(jsonPath("description", is("Draft Gephi ...")))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        String objectPersistentId = "DstBL5";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}?draft=true", subjectPersistentId, objectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("subject.persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("subject.category", is("tool-or-service")))
                .andExpect(jsonPath("object.persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("object.category", is("tool-or-service")))
                .andExpect(jsonPath("relation.code", is("mentions")))
                .andExpect(jsonPath("relation.label", is("Mentions")));

        mvc.perform(get("/api/tools-services/{id}?draft=true", subjectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Draft Gephi")))
                .andExpect(jsonPath("relatedItems", hasSize(3)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        mvc.perform(get("/api/tools-services/{id}", objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Stata")))
                .andExpect(jsonPath("relatedItems", hasSize(0)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldNotCreateItemsRelationsWhenRelationIsIncorrect() throws Exception {
        String subjectPersistentId = "n21Kfc";
        String objectPersistentId = "DstBL5";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("qwerty");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
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
        String subjectPersistentId = "n21Kfc";
        String objectPersistentId = "Xgufde";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", notNullValue()));
    }

    @Test
    public void shouldNotCreateItemsRelationsWhenItemNotExist() throws Exception {
        String subjectPersistentId = "n21Kfc";
        String objectPersistentId = "xxxxxx7";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateAndDeleteItemsRelations() throws Exception {
        String subjectPersistentId = "DstBL5";
        String objectPersistentId = "dU0BZc";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("subject.persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("subject.category", is("tool-or-service")))
                .andExpect(jsonPath("object.persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("object.category", is("dataset")))
                .andExpect(jsonPath("relation.code", is("mentions")))
                .andExpect(jsonPath("relation.label", is("Mentions")));

        mvc.perform(get("/api/tools-services/{id}", subjectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Stata")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(2)))
                .andExpect(jsonPath("olderVersions[0].label", is("Stata")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        mvc.perform(get("/api/datasets/{id}", objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(11)))
                .andExpect(jsonPath("olderVersions[0].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/tools-services/{id}", subjectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Stata")))
                .andExpect(jsonPath("relatedItems", hasSize(0)))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[1].id", is(2)))
                .andExpect(jsonPath("olderVersions[1].label", is("Stata")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        mvc.perform(get("/api/datasets/{id}", objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("relatedItems", hasSize(0)))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[1].id", is(11)))
                .andExpect(jsonPath("olderVersions[1].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldCreateAndDeleteItemsRelationsAsDraft() throws Exception {
        String subjectPersistentId = "DstBL5";

        ToolCore tool = new ToolCore();
        tool.setLabel("Draft Stata");
        tool.setDescription("Draft Stata ...");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}?draft=true", subjectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Draft Stata")))
                .andExpect(jsonPath("description", is("Draft Stata ...")))
                .andExpect(jsonPath("relatedItems", hasSize(0)))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        String objectPersistentId = "dU0BZc";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}?draft=true", subjectPersistentId, objectPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/tools-services/{id}?draft=true", subjectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Draft Stata")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}?draft=true", subjectPersistentId, objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/tools-services/{id}?draft=true", subjectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Draft Stata")))
                .andExpect(jsonPath("relatedItems", hasSize(0)))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));

    }

    @Test
    public void shouldNotDeleteItemsRelationsWhenItemNotExist() throws Exception {
        String subjectPersistentId = "DstBL5";
        String objectPersistentId = "xxxxxx7";

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

}
