package eu.sshopencloud.marketplace.controllers.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.items.RelatedItemCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class ItemRelationControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

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
        tool.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().objectId("Xgufde").relation(new ItemRelationId("relates-to")).build(),
                        RelatedItemCore.builder().objectId("heBAGQ").relation(new ItemRelationId("documents")).build()
                )
        );

        String payload = mapper.writeValueAsString(tool);
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

        payload = mapper.writeValueAsString(itemRelation);
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

    @Test
    public void shouldCreateNewItemWithRelations() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Problems Dataset");
        dataset.setDescription("A dataset of algorithmic problems ...");
        dataset.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().objectId("prblMo").relation(new ItemRelationId("is-mentioned-in")).build(),
                        RelatedItemCore.builder().objectId("OdKfPc").relation(new ItemRelationId("relates-to")).build()
                )
        );

        String payload = mapper.writeValueAsString(dataset);

        String datasetJson = mvc.perform(
                post("/api/datasets")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        DatasetDto newDataset = mapper.readValue(datasetJson, DatasetDto.class);

        newDataset.getRelatedItems().forEach(rel -> {
            assertThat(List.of("prblMo", "OdKfPc"), hasItem(rel.getPersistentId()));
            Map<String, Long> versionIds = Map.of("prblMo", 13L, "OdKfPc", 10L);
            Map<String, String> itemLabels = Map.of(
                    "prblMo", "Build the model of the dictionary",
                    "OdKfPc", "Consortium of European Social Science Data Archives"
            );

            assertNotEquals(rel.getId(), versionIds.get(rel.getPersistentId()));
            assertEquals(rel.getLabel(), itemLabels.get(rel.getPersistentId()));
        });

        mvc.perform(
                get("/api/workflows/{workflowId}/steps/{stepId}", "tqmbGY", "prblMo")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("prblMo")))
                .andExpect(jsonPath("id", not(is(13))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(newDataset.getPersistentId())))
                .andExpect(jsonPath("relatedItems[0].id", is(newDataset.getId().intValue())))
                .andExpect(jsonPath("relatedItems[0].label", is(newDataset.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")));

        mvc.perform(
                get("/api/workflows/{workflowId}", "tqmbGY")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("tqmbGY")))
                .andExpect(jsonPath("id", not(is(12))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")));

        mvc.perform(
                get("/api/datasets/{datasetId}", "OdKfPc")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("OdKfPc")))
                .andExpect(jsonPath("id", not(is(10))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("relatedItems[0].id", is(9)))
                .andExpect(jsonPath("relatedItems[0].label", is("Austin Crime Data")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is(newDataset.getPersistentId())))
                .andExpect(jsonPath("relatedItems[1].id", is(newDataset.getId().intValue())))
                .andExpect(jsonPath("relatedItems[1].label", is(newDataset.getLabel())))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-related-to")));

        mvc.perform(
                get("/api/datasets/{datasetId}", "dmbq4v")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("dmbq4v")))
                .andExpect(jsonPath("id", is(9)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("relatedItems[0].id", not(is(10))))
                .andExpect(jsonPath("relatedItems[0].label", is("Consortium of European Social Science Data Archives")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")));

        mvc.perform(
                get("/api/datasets/{datasetId}", newDataset.getPersistentId())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(newDataset.getPersistentId())))
                .andExpect(jsonPath("id", is(newDataset.getId().intValue())))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("prblMo")))
                .andExpect(jsonPath("relatedItems[0].id", not(is(13))))
                .andExpect(jsonPath("relatedItems[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("relatedItems[1].id", not(is(10))))
                .andExpect(jsonPath("relatedItems[1].label", is("Consortium of European Social Science Data Archives")))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("relates-to")));
    }

    @Test
    public void shouldUpdateItemRelations() throws Exception {
        String toolId = "n21Kfc";

        mvc.perform(
                get("/api/tools-services/{toolId}", toolId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].id", is(3)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi v2");
        tool.setDescription("Gephi v2...");
        tool.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().objectId("heBAGQ").relation(new ItemRelationId("is-mentioned-in")).build(),
                        RelatedItemCore.builder().objectId("tqmbGY").relation(new ItemRelationId("is-documented-by")).build()
                )
        );

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                put("/api/tools-services/{toolId}", toolId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", not(is(1))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[0].id", not(is(4))))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("relatedItems[1].id", not(is(12))))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        mvc.perform(
                get("/api/training-materials/{trainingMaterialId}", "heBAGQ")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("heBAGQ")))
                .andExpect(jsonPath("id", not(is(4))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", not(is(1))))
                .andExpect(jsonPath("relatedItems[0].label", is(tool.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")));

        mvc.perform(
                get("/api/workflows/{workflowId}", "tqmbGY")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("tqmbGY")))
                .andExpect(jsonPath("id", not(is(12))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", not(is(1))))
                .andExpect(jsonPath("relatedItems[0].label", is(tool.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("documents")));

        mvc.perform(
                get("/api/tools-services/{toolId}", "Xgufde")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("Xgufde")))
                .andExpect(jsonPath("id", not(is(3))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("relatedItems", hasSize(0)));
    }

    @Test
    public void shouldUpdateItemRelationsUnmodified() throws Exception {
        String toolId = "n21Kfc";

        mvc.perform(
                get("/api/tools-services/{toolId}", toolId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].id", is(3)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi v2");
        tool.setDescription("Gephi v2...");
        tool.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().objectId("Xgufde").relation(new ItemRelationId("is-related-to")).build(),
                        RelatedItemCore.builder().objectId("heBAGQ").relation(new ItemRelationId("is-documented-by")).build()
                )
        );

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                put("/api/tools-services/{toolId}", toolId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", not(is(1))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].id", is(3)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        mvc.perform(
                get("/api/tools-services/{toolId}", "Xgufde")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("Xgufde")))
                .andExpect(jsonPath("id", is(3)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", not(is(1))))
                .andExpect(jsonPath("relatedItems[0].label", is(tool.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("relates-to")));

        mvc.perform(
                get("/api/training-materials/{trainingMaterialId}", "heBAGQ")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("heBAGQ")))
                .andExpect(jsonPath("id", is(4)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", not(is(1))))
                .andExpect(jsonPath("relatedItems[0].label", is(tool.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("documents")));
    }

    @Test
    public void shouldCommitUpdatedDraftItemRelations() throws Exception {
        String toolId = "n21Kfc";

        mvc.perform(
                get("/api/tools-services/{toolId}", toolId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].id", is(3)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi v2");
        tool.setDescription("Gephi v2...");
        tool.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().objectId("heBAGQ").relation(new ItemRelationId("is-mentioned-in")).build(),
                        RelatedItemCore.builder().objectId("tqmbGY").relation(new ItemRelationId("is-documented-by")).build()
                )
        );

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                put("/api/tools-services/{toolId}?draft=1", toolId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", not(is(1))))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[0].id", is(4)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("relatedItems[1].id", is(12)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        mvc.perform(
                get("/api/training-materials/{trainingMaterialId}", "heBAGQ")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("heBAGQ")))
                .andExpect(jsonPath("id", is(4)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", is(1)))
                .andExpect(jsonPath("relatedItems[0].label", is("Gephi")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("documents")));

        mvc.perform(
                get("/api/workflows/{workflowId}", "tqmbGY")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("tqmbGY")))
                .andExpect(jsonPath("id", is(12)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("relatedItems", hasSize(0)));

        mvc.perform(
                get("/api/tools-services/{toolId}", "Xgufde")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("Xgufde")))
                .andExpect(jsonPath("id", is(3)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", is(1)))
                .andExpect(jsonPath("relatedItems[0].label", is("Gephi")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("relates-to")));

        mvc.perform(
                post("/api/tools-services/{toolId}/commit", toolId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", not(is(1))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(2)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[0].id", not(is(4))))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("tqmbGY")))
                .andExpect(jsonPath("relatedItems[1].id", not(is(3))))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        mvc.perform(
                get("/api/training-materials/{trainingMaterialId}", "heBAGQ")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("heBAGQ")))
                .andExpect(jsonPath("id", not(is(4))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", not(is(1))))
                .andExpect(jsonPath("relatedItems[0].label", is(tool.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")));

        mvc.perform(
                get("/api/workflows/{workflowId}", "tqmbGY")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("tqmbGY")))
                .andExpect(jsonPath("id", not(is(12))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(toolId)))
                .andExpect(jsonPath("relatedItems[0].id", not(is(1))))
                .andExpect(jsonPath("relatedItems[0].label", is(tool.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("documents")));

        mvc.perform(
                get("/api/tools-services/{toolId}", "Xgufde")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is("Xgufde")))
                .andExpect(jsonPath("id", not(is(3))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("relatedItems", hasSize(0)));
    }

    @Test
    public void shouldNotUpdateWhenDuplicateRelation() throws Exception {
        String toolId = "n21Kfc";

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi v2");
        tool.setDescription("Gephi v2...");
        tool.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().objectId("heBAGQ").relation(new ItemRelationId("is-related-to")).build(),
                        RelatedItemCore.builder().objectId("heBAGQ").relation(new ItemRelationId("is-documented-by")).build()
                )
        );

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                put("/api/tools-services/{toolId}", toolId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAddRelationToWorkflowStepVersion() throws Exception {
        String workflowId = "tqmbGY";

        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Creation of a dictionary v2");
        workflow.setDescription("Best practices for creating a born-digital dictionary, i.e. lorem ipsum.");

        String workflowPayload = mapper.writeValueAsString(workflow);

        mvc.perform(
                put("/api/workflows/{id}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workflowPayload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(workflow.getLabel())))
                .andExpect(jsonPath("description", is(workflow.getDescription())))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Creation of a corpora")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].label", is("Corpus composition")))
                .andExpect(jsonPath("composedOf[1].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[1].label", is("Linguistic annotation")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[2].label", is("Selection of a license")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Write a dictionary")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));

        String subjectPersistentId = "prblMo";
        String objectPersistentId = "n21Kfc";

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        String relationPayload = mapper.writeValueAsString(itemRelation);

        mvc.perform(
                post("/api/items-relations/{subjectId}/{objectId}", subjectPersistentId, objectPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relationPayload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("subject.persistentId", is(subjectPersistentId)))
                .andExpect(jsonPath("subject.category", is("step")))
                .andExpect(jsonPath("object.persistentId", is(objectPersistentId)))
                .andExpect(jsonPath("object.category", is("tool-or-service")))
                .andExpect(jsonPath("relation.code", is("mentions")))
                .andExpect(jsonPath("relation.label", is("Mentions")));
    }

    @Test
    public void shouldNotModifyReferencedItemStatus() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Another new proposed publication");
        publication.setDescription("One of the many of proposed publications");

        String publicationPayload = mapper.writeValueAsString(publication);

        String publicationJson = mvc.perform(
                post("/api/publications")
                        .content(publicationPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is(publication.getLabel())))
                .andExpect(jsonPath("description", is(publication.getDescription())))
                .andReturn().getResponse().getContentAsString();

        PublicationDto publicationDto = mapper.readValue(publicationJson, PublicationDto.class);

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Brand-new tutorial");
        trainingMaterial.setDescription("In this tutorial you have access to the latest research");

        String trainingMaterialPayload = mapper.writeValueAsString(trainingMaterial);

        String trainingMaterialJson = mvc.perform(
                post("/api/training-materials")
                        .content(trainingMaterialPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto trainingMaterialDto = mapper.readValue(trainingMaterialJson, TrainingMaterialDto.class);

        TrainingMaterialCore acceptedTrainingMaterial = new TrainingMaterialCore();
        acceptedTrainingMaterial.setLabel("Accepted brand-new tutorial");
        acceptedTrainingMaterial.setDescription("In this approved tutorial you have access to the latest research");

        acceptedTrainingMaterial.setRelatedItems(
                List.of(new RelatedItemCore(publicationDto.getPersistentId(), new ItemRelationId("documents")))
        );

        String acceptedTrainingMaterialPayload = mapper.writeValueAsString(acceptedTrainingMaterial);

//        trainingMaterialJson = mvc.perform(
        mvc.perform(
                put("/api/training-materials/{trainingMaterialId}", trainingMaterialDto.getPersistentId())
                        .content(acceptedTrainingMaterialPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(trainingMaterialDto.getId()))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is(acceptedTrainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(acceptedTrainingMaterial.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(0)));
//                suggested items are not included in the related items
//                .andExpect(jsonPath("relatedItems[0].persistentId", is(publicationDto.getPersistentId())))
//                .andExpect(jsonPath("relatedItems[0].id", not(is(publicationDto.getId()))))
//                .andExpect(jsonPath("relatedItems[0].label", is(publication.getLabel())))
//                .andExpect(jsonPath("relatedItems[0].relation.code", is("documents")))
//                .andReturn().getResponse().getContentAsString();

        // TODO: uncomment as soon as retrieving suggested items will be possible
        //       the test scenario below requires loading a suggested item

        /*
        trainingMaterialDto = mapper.readValue(trainingMaterialJson, TrainingMaterialDto.class);

        String latestPublicationJson = mvc.perform(
                get("/api/publications/{publicationId}", publicationDto.getPersistentId())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("newerVersions", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        PublicationDto latestPublication = mapper.readValue(latestPublicationJson, PublicationDto.class);
        long publicationVersionId = latestPublication.getNewerVersions().get(0).getId();

        mvc.perform(
                get(
                        "/api/publications/{publicationId}/versions/{versionId}",
                        publicationDto.getPersistentId(), publicationVersionId
                )
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(publicationDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(publicationDto.getId()))))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is(publication.getLabel())))
                .andExpect(jsonPath("description", is(publication.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(trainingMaterialDto.getPersistentId())))
                .andExpect(jsonPath("relatedItems[0].id", is(trainingMaterialDto.getId())))
                .andExpect(jsonPath("relatedItems[0].label", is(acceptedTrainingMaterial.getLabel())))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-documented-by")));
         */
    }
}
