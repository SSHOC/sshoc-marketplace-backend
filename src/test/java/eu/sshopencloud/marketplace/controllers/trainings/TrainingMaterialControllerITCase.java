package eu.sshopencloud.marketplace.controllers.trainings;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.items.RelatedItemCore;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class TrainingMaterialControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private ObjectMapper testMapper;

    private String CONTRIBUTOR_JWT;
    private String IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;

    @Before
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");

        testMapper = TestJsonMapper.serializingObjectMapper();
    }

    @Test
    public void shouldReturnTrainingMaterials() throws Exception {

        mvc.perform(get("/api/training-materials")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnApprovedAndProposedTrainingMaterials() throws Exception {
        String trainingMaterialId = "JmBgWa";

        TrainingMaterialCore trainingMaterial1 = new TrainingMaterialCore();
        trainingMaterial1.setLabel("Abc: Test proposed training material");
        trainingMaterial1.setDescription("Lorem ipsum dolor");

        String payload1 = mapper.writeValueAsString(trainingMaterial1);

        String trainingMaterialJson1 = mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload1)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(trainingMaterial1.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial1.getDescription())))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto trainingMaterialDto1 = mapper.readValue(trainingMaterialJson1, TrainingMaterialDto.class);
        int trainingMaterialVersionId1 = trainingMaterialDto1.getId().intValue();

        TrainingMaterialCore trainingMaterial2 = new TrainingMaterialCore();
        trainingMaterial2.setLabel("Abc: Test ingested training material");
        trainingMaterial2.setDescription("Lorem ipsum dolor sit");

        String payload2 = mapper.writeValueAsString(trainingMaterial2);

        String trainingMaterialJson2 = mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload2)
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")))
                .andExpect(jsonPath("label", is(trainingMaterial2.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial2.getDescription())))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto trainingMaterialDto2 = mapper.readValue(trainingMaterialJson2, TrainingMaterialDto.class);
        int trainingMaterialVersionId2 = trainingMaterialDto2.getId().intValue();

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
                                .param("approved", "false")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId1)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(trainingMaterial1.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial1.getDescription())));

        mvc.perform(
                        get("/api/training-materials")
                                .param("approved", "false")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(4)))
                .andExpect(jsonPath("trainingMaterials", hasSize(4)))
                .andExpect(jsonPath("trainingMaterials[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[0].id", is(trainingMaterialVersionId1)))
                .andExpect(jsonPath("trainingMaterials[0].status", is("suggested")));

        mvc.perform(
                        get("/api/training-materials")
                                .param("approved", "false")
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(4)))
                .andExpect(jsonPath("trainingMaterials", hasSize(4)))
                .andExpect(jsonPath("trainingMaterials[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[0].id", is(trainingMaterialVersionId2)))
                .andExpect(jsonPath("trainingMaterials[0].status", is("ingested")));

        mvc.perform(
                        get("/api/training-materials")
                                .param("approved", "false")
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(5)))
                .andExpect(jsonPath("trainingMaterials", hasSize(5)))
                .andExpect(jsonPath("trainingMaterials[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[0].id", is(trainingMaterialVersionId2)))
                .andExpect(jsonPath("trainingMaterials[0].status", is("ingested")))
                .andExpect(jsonPath("trainingMaterials[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[1].id", is(trainingMaterialVersionId1)))
                .andExpect(jsonPath("trainingMaterials[1].status", is("suggested")));

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Abc: Final version of training material");
        trainingMaterial.setDescription("Lorem ipsum dolor sit finito");

        String payload = mapper.writeValueAsString(trainingMaterial);

        String trainingMaterialJson = mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("status", is("approved")))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto trainingMaterialDto = mapper.readValue(trainingMaterialJson, TrainingMaterialDto.class);
        int versionId = trainingMaterialDto.getId().intValue();

        mvc.perform(
                        get("/api/training-materials")
                                .param("approved", "false")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("trainingMaterials", hasSize(3)))
                .andExpect(jsonPath("trainingMaterials[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[0].id", is(versionId)))
                .andExpect(jsonPath("trainingMaterials[0].status", is("approved")));

        mvc.perform(
                        get("/api/training-materials")
                                .param("approved", "false")
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("trainingMaterials", hasSize(3)))
                .andExpect(jsonPath("trainingMaterials[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[0].id", is(versionId)))
                .andExpect(jsonPath("trainingMaterials[0].status", is("approved")));

        mvc.perform(
                        get("/api/training-materials")
                                .param("approved", "false")
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("trainingMaterials", hasSize(3)))
                .andExpect(jsonPath("trainingMaterials[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("trainingMaterials[0].id", is(versionId)))
                .andExpect(jsonPath("trainingMaterials[0].status", is("approved")));
    }

    @Test
    public void shouldReturnTrainingMaterial() throws Exception {
        String trainingMaterialId = "WfcKvG";
        int newestVersionId = 7;

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(newestVersionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("version", is("3.0")))
                .andExpect(jsonPath("informationContributor.id", is(1)));
    }

    @Test
    public void shouldNotFindANonExistentDraftTrainingMaterial() throws Exception {
        String trainingMaterialId = "WfcKvG";

        mvc.perform(
                        get("/api/training-materials/{id}?draft=1", trainingMaterialId)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnTrainingMaterialHistoricalVersion() throws Exception {
        String trainingMaterialId = "WfcKvG";
        int versionId = 5;

        mvc.perform(
                        get("/api/training-materials/{id}/versions/{vId}", trainingMaterialId, versionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(versionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("deprecated")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("version", is("1.0")))
                .andExpect(jsonPath("informationContributor.id", is(1)));
    }

    @Test
    public void shouldNotReturnTrainingMaterialWhenNotExist() throws Exception {
        Integer trainingMaterialId = 51;

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateDraftTrainingMaterial() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("First attempt of making a test simple blog");
        trainingMaterial.setDescription("Lorem ipsum is not enough for a blog");
        trainingMaterial.setAccessibleAt(List.of("https://programminghistorian.org/en/lessons/test-simple-blog"));
        trainingMaterial.setSourceItemId("9999");

        String payload = testMapper.writeValueAsString(trainingMaterial);

        String jsonResponse = mvc.perform(
                        post("/api/training-materials?draft=true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")))
                .andReturn().getResponse().getContentAsString();

        String trainingMaterialId = testMapper.readValue(jsonResponse, TrainingMaterialDto.class).getPersistentId();

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());

        mvc.perform(
                        get("/api/training-materials/{id}?draft=true", trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")));
    }

    @Test
    public void shouldCreateTrainingMaterialWithImplicitSourceAndSourceItemId() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test simple blog");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setAccessibleAt(List.of("https://programminghistorian.org/en/lessons/test-simple-blog"));
        trainingMaterial.setSourceItemId("9999");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test simple blog")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")));


        mvc.perform(get("/api/sources/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(2)))
                .andExpect(jsonPath("label", is("Programming Historian")))
                .andExpect(jsonPath("url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("lastHarvestedDate", notNullValue()));
    }

    @Test
    public void shouldCreateTrainingMaterialWithImplicitSourceAndSourceItemIdAndMultipleLinks() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test simple blog");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setSourceItemId("9999");
        trainingMaterial.setAccessibleAt(
                Arrays.asList(
                        "https://programminghistorian.org/en/lessons/test-simple-blog",
                        "https://test.programminghistorian.org/en/lessons/test-simple-blog",
                        "https://dev.programminghistorian.org/en/lessons/test-simple-blog"
                )
        );

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test simple blog")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("accessibleAt", hasSize(3)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("accessibleAt[1]", is("https://test.programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("accessibleAt[2]", is("https://dev.programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")));

        mvc.perform(get("/api/sources/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(2)))
                .andExpect(jsonPath("label", is("Programming Historian")))
                .andExpect(jsonPath("url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("lastHarvestedDate", notNullValue()));
    }

    @Test
    public void shouldCreateTrainingMaterialWithRelations() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test complex online course");
        trainingMaterial.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        trainingMaterial.setContributors(contributors);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("eng");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2018, Month.APRIL, 1), LocalTime.of(12, 0), ZoneId.of("UTC"));
        trainingMaterial.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2018, Month.DECEMBER, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        trainingMaterial.setDateLastUpdated(dateLastUpdated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test complex online course")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));
    }


    @Test
    public void shouldNotCreateTrainingMaterialWithImplicitSourceButWithoutSourceItemId() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test simple blog");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setAccessibleAt(Arrays.asList("https://programminghistorian.org/en/lessons/test-simple-blog"));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("sourceItemId")))
                .andExpect(jsonPath("errors[0].code", is("field.requiredInCase")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenLabelIsNull() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setDescription("Lorem ipsum");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("label")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenContributorIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(99l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        trainingMaterial.setContributors(contributors);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].actor.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenContributorRoleIsIncorrect() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(2l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("xxx");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        trainingMaterial.setContributors(contributors);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenPropertyTypeIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("yyy");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("eng");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].type.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenConceptIsIncorrect() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("zzz");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenVocabularyIsDisallowed() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("activity");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("eng");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.vocabulary")))
                .andExpect(jsonPath("errors[0].code", is("field.disallowedVocabulary")))
                .andExpect(jsonPath("errors[0].args[0]", is("iso-639-3")))
                .andExpect(jsonPath("errors[0].args[1]", is("activity")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        property1.setValue("Polish");
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldPerformDraftUpdateAndCommit() throws Exception {
        String trainingMaterialId = "WfcKvG";

        mvc.perform(get("/api/training-materials/{id}/history?draft=false", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))

                .andExpect(jsonPath("$[0].category", is("training-material")))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("$[0].status", is("approved")))

                .andExpect(jsonPath("$[1].category", is("training-material")))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[1].status", is("deprecated")))

                .andExpect(jsonPath("$[2].category", is("training-material")))
                .andExpect(jsonPath("$[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[2].status", is("deprecated")));

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("First attempt of making a test simple blog");
        trainingMaterial.setDescription("Lorem ipsum is not enough for a blog");
        trainingMaterial.setAccessibleAt(List.of("https://programminghistorian.org/en/lessons/test-simple-blog"));
        trainingMaterial.setSourceItemId("9999");

        String payload = testMapper.writeValueAsString(trainingMaterial);

        mvc.perform(
                        put("/api/training-materials/{id}?draft=1", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")));

        mvc.perform(
                        get("/api/training-materials/{id}?draft=true", trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")));

        mvc.perform(get("/api/training-materials/{id}/history?draft=true", trainingMaterialId)
                        .header("Authorization", MODERATOR_JWT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].category", is("training-material")))
                .andExpect(jsonPath("$[0].label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[0].status", is("draft")))

                .andExpect(jsonPath("$[1].category", is("training-material")))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("$[1].status", is("approved")))

                .andExpect(jsonPath("$[2].category", is("training-material")))
                .andExpect(jsonPath("$[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[2].status", is("deprecated")))

                .andExpect(jsonPath("$[3].category", is("training-material")))
                .andExpect(jsonPath("$[3].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[3].status", is("deprecated")));

        mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org/en/lessons/test-simple-blog")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("9999")));

        mvc.perform(
                        get("/api/training-materials/{id}?draft=true", trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/training-materials/{id}/history?draft=true", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isNotFound());


        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())));

        mvc.perform(get("/api/training-materials/{id}/history?draft=false", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].category", is("training-material")))
                .andExpect(jsonPath("$[0].label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[0].status", is("approved")))

                .andExpect(jsonPath("$[1].category", is("training-material")))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[1].status", is("deprecated")))

                .andExpect(jsonPath("$[2].category", is("training-material")))
                .andExpect(jsonPath("$[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[2].status", is("deprecated")))

                .andExpect(jsonPath("$[3].category", is("training-material")))
                .andExpect(jsonPath("$[3].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[3].status", is("deprecated")));

    }

    @Test
    public void shouldUpdateTrainingMaterialWithImplicitSource() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test simple training material");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setAccessibleAt(List.of("http://programminghistorian.org/en/lessons/test-simple-training-material"));
        trainingMaterial.setSourceItemId("8888");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test simple training material")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("http://programminghistorian.org/en/lessons/test-simple-training-material")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("8888")));

    }

    @Test
    public void shouldUpdateTrainingMaterialWithRelations() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Introduction to GEPHI");
        trainingMaterial.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        trainingMaterial.setContributors(contributors);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("eng");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2018, Month.APRIL, 1), LocalTime.of(12, 0), ZoneId.of("UTC"));
        trainingMaterial.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2018, Month.DECEMBER, 17), LocalTime.of(12, 20), ZoneId.of("UTC"));
        trainingMaterial.setDateLastUpdated(dateLastUpdated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));
    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenNotExist() throws Exception {
        String trainingMaterialId = "noting";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    @Deprecated
    public void shouldUpdateTrainingMaterialWithHistory() throws Exception {
        String trainingMaterialId = "WfcKvG";

        mvc.perform(get("/api/training-materials/{id}/history", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].category", is("training-material")))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[1].category", is("training-material")))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[1].status", is("deprecated")))
                .andExpect(jsonPath("$[2].category", is("training-material")))
                .andExpect(jsonPath("$[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[2].status", is("deprecated")));


        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Introduction to GEPHI");
        trainingMaterial.setVersion("4.0");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("description", is("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")));


        mvc.perform(get("/api/training-materials/{id}/history", trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].category", is("training-material")))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].version", is("4.0")))
                .andExpect(jsonPath("$[0].label", is("Introduction to GEPHI")))

                .andExpect(jsonPath("$[1].category", is("training-material")))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[1].status", is("deprecated")))

                .andExpect(jsonPath("$[2].category", is("training-material")))
                .andExpect(jsonPath("$[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[2].status", is("deprecated")))

                .andExpect(jsonPath("$[3].category", is("training-material")))
                .andExpect(jsonPath("$[3].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[3].status", is("deprecated")));

    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenLabelIsNull() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("label")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }


    @Test
    public void shouldNotUpdateTrainingMaterialWhenContributorIsUnknown() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(99l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        trainingMaterial.setContributors(contributors);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].actor.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenContributorRoleIsIncorrect() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(2l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("xxx");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        trainingMaterial.setContributors(contributors);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenPropertyTypeIsUnknown() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("yyy");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("eng");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].type.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenConceptIsIncorrect() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("zzz");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenVocabularyIsDisallowed() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("activity");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("eng");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("iso-639-3");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.vocabulary")))
                .andExpect(jsonPath("errors[0].code", is("field.disallowedVocabulary")))
                .andExpect(jsonPath("errors[0].args[0]", is("iso-639-3")))
                .andExpect(jsonPath("errors[0].args[1]", is("activity")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateTrainingMaterialWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        property1.setValue("Polish");
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldDeleteTrainingMaterial() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test complex online course");
        trainingMaterial.setDescription("Lorem Ipsum ...");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/training-materials")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String trainingMaterialPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, TrainingMaterialDto.class).getPersistentId();
        Long trainingMaterialVersionId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, TrainingMaterialDto.class).getId();

        mvc.perform(delete("/api/training-materials/{persistentId}/versions/{id}", trainingMaterialPersistentId, trainingMaterialVersionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/training-materials/{persistentId}?approved=false", trainingMaterialPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/training-materials/{persistentId}/versions/{id}", trainingMaterialPersistentId, trainingMaterialVersionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId.intValue())))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")));
    }

    @Test
    public void shouldDeleteTrainingMaterialHistoricalVersion() throws Exception {
        String trainingMaterialId = "WfcKvG";
        int versionId = 5;

        mvc.perform(delete("/api/training-materials/{persistentId}/versions/{id}", trainingMaterialId, versionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/training-materials/{persistentId}/versions/{id}", trainingMaterialId, versionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(versionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("deprecated")));
    }

    @Test
    public void shouldNotCreateTrainingMaterialWithInvalidDateProperty() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription(
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. " +
                        "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, " +
                        "when an unknown printer took a galley of type and scrambled it to make a type specimen book. " +
                        "It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. " +
                        "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages."
        );

        PropertyCore property1 = new PropertyCore(new PropertyTypeId("timestamp"), "2000-01-01");
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("timestamp"), "14732891437");
        trainingMaterial.setProperties(List.of(property1, property2));

        String payload = mapper.writeValueAsString(trainingMaterial);

        mvc.perform(
                        post("/api/training-materials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].value")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldRetrieveSuggestedTrainingMaterial() throws Exception {
        String trainingMaterialId = "WfcKvG";
        int trainingMaterialVersionId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Suggested training material");
        trainingMaterial.setDescription("This is a suggested training material");

        String payload = mapper.writeValueAsString(trainingMaterial);

        mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", not(is(trainingMaterialVersionId))))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
                                .param("approved", "false")
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", not(is(trainingMaterialVersionId))))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
                                .param("approved", "false")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")));

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")));
    }

    @Test
    public void shouldNotAccessNotOwnedVersion() throws Exception {
        mvc.perform(
                        get("/api/training-materials/{id}/versions/{verId}", "WfcKvG", 5)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateAndValidateAccessToSuggestedItemVersion() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Suggested training material version");
        trainingMaterial.setDescription("This is a suggested training material version");

        String payload = mapper.writeValueAsString(trainingMaterial);

        String trainingMaterialResponse = mvc.perform(
                        post("/api/training-materials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto trainingMaterialDto = mapper.readValue(trainingMaterialResponse, TrainingMaterialDto.class);
        String trainingMaterialId = trainingMaterialDto.getPersistentId();
        int trainingMaterialVersionId = trainingMaterialDto.getId().intValue();

        mvc.perform(
                        get("/api/training-materials/{id}/versions/{verId}", trainingMaterialId, trainingMaterialVersionId)
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                        get("/api/training-materials/{id}/versions/{verId}", trainingMaterialId, trainingMaterialVersionId)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                        get("/api/training-materials/{id}/versions/{verId}", trainingMaterialId, trainingMaterialVersionId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("id", is(trainingMaterialVersionId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                        get("/api/training-materials/{id}/versions/{verId}", trainingMaterialId, trainingMaterialVersionId)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/training-materials/{id}/versions/{verId}", trainingMaterialId, trainingMaterialVersionId))
                .andExpect(status().isForbidden());
    }

    @Ignore(value = "hidden properties have to be always rendered")
    @Test
    public void shouldNotRenderHiddenProperty() throws Exception {
        PropertyTypeCore propertyType = PropertyTypeCore.builder()
                .code("http-status")
                .label("HTTP resource status code")
                .type(PropertyTypeClass.INT)
                .groupName("status")
                .hidden(true)
                .build();

        String propertyTypePayload = mapper.writeValueAsString(propertyType);

        mvc.perform(
                        post("/api/property-types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(propertyTypePayload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("http-status")))
                .andExpect(jsonPath("hidden", is(true)));

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Confidential training material");
        trainingMaterial.setDescription("Training material with hidden property");
        trainingMaterial.setProperties(
                List.of(
                        new PropertyCore(new PropertyTypeId("http-status"), "404"),
                        new PropertyCore(new PropertyTypeId("keyword"), "confidential")
                )
        );

        String trainingMaterialPayload = mapper.writeValueAsString(trainingMaterial);

        String responseJson = mvc.perform(
                        post("/api/training-materials")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(trainingMaterialPayload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].type.code", is("http-status")))
                .andExpect(jsonPath("properties[0].type.groupName", is("status")))
                .andExpect(jsonPath("properties[0].type.hidden", is(true)))
                .andExpect(jsonPath("properties[1].type.code", is("keyword")))
                .andExpect(jsonPath("properties[1].type.hidden", is(false)))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto dto = mapper.readValue(responseJson, TrainingMaterialDto.class);
        String persistentId = dto.getPersistentId();

        mvc.perform(get("/api/training-materials/{persistentId}", persistentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(persistentId)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].type.code", is("keyword")))
                .andExpect(jsonPath("properties[0].type.hidden", is(false)));

        mvc.perform(
                        get("/api/training-materials/{persistentId}", persistentId)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(persistentId)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].type.code", is("keyword")))
                .andExpect(jsonPath("properties[0].type.hidden", is(false)));


        mvc.perform(
                        get("/api/training-materials/{persistentId}", persistentId)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(persistentId)))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].type.code", is("http-status")))
                .andExpect(jsonPath("properties[0].type.groupName", is("status")))
                .andExpect(jsonPath("properties[0].type.hidden", is(true)))
                .andExpect(jsonPath("properties[1].type.code", is("keyword")))
                .andExpect(jsonPath("properties[1].type.hidden", is(false)));
    }

    @Test
    public void shouldApproveTrainingMaterialRelatedToAToolMultipleTimes() throws Exception {
        String trainingMaterialId = "heBAGQ";
        String relatedObjectId = "n21Kfc";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Gephi: explore the networks");
        trainingMaterial.setDescription("An open source software for exploring and manipulating networks");
        trainingMaterial.setRelatedItems(
                List.of(new RelatedItemCore(relatedObjectId, new ItemRelationId("documents")))
        );

        String suggestedPayload = mapper.writeValueAsString(trainingMaterial);
        mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(suggestedPayload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(relatedObjectId)));

        mvc.perform(
                        get("/api/tools-services/{id}", relatedObjectId)
                                .param("approved", "false")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(relatedObjectId)))
                .andExpect(jsonPath("relatedItems", hasSize(3)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")))
                .andExpect(jsonPath("relatedItems[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("relatedItems[2].id", not(is(4))))
                .andExpect(jsonPath("relatedItems[2].relation.code", is("is-documented-by")));

        trainingMaterial.setLabel("Gephi: explore the networks!");

        String approvalPayload = mapper.writeValueAsString(trainingMaterial);
        mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(approvalPayload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(relatedObjectId)));
    }

    @Test
    public void shouldRemoveMultiVersionRelationToItem() throws Exception {
        String trainingMaterialId = "heBAGQ";
        String relatedObjectId = "n21Kfc";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Gephi: explore the networks");
        trainingMaterial.setDescription("An open source software for exploring and manipulating networks");
        trainingMaterial.setRelatedItems(
                List.of(new RelatedItemCore(relatedObjectId, new ItemRelationId("documents")))
        );

        String trainingMaterialPayload = mapper.writeValueAsString(trainingMaterial);
        mvc.perform(
                        put("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(trainingMaterialPayload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is(relatedObjectId)));

        mvc.perform(
                        get("/api/tools-services/{id}", relatedObjectId)
                                .param("approved", "false")
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(relatedObjectId)))
                .andExpect(jsonPath("relatedItems", hasSize(3)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")))
                .andExpect(jsonPath("relatedItems[2].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("relatedItems[2].id", not(is(4))))
                .andExpect(jsonPath("relatedItems[2].relation.code", is("is-documented-by")));

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi: lonline");
        tool.setDescription("Gephi without any relations");
        tool.setRelatedItems(List.of());

        String toolPayload = mapper.writeValueAsString(tool);
        mvc.perform(
                        put("/api/tools-services/{id}", relatedObjectId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toolPayload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("relatedItems", hasSize(0)));
    }


    @Test
    public void shouldReturnTrainingMaterialInformationContributors() throws Exception {

        String trainingMaterialPersistentId = "heBAGQ";

        mvc.perform(get("/api/training-materials/{id}/information-contributors", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].username", is("Moderator")))
                .andExpect(jsonPath("$[0].displayName", is("Moderator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[0].role", is("moderator")))
                .andExpect(jsonPath("$[0].email", is("moderator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));
    }

    @Test
    public void shouldReturnTrainingMaterialInformationContributorsForVersion() throws Exception {

        String trainingMaterialPersistentId = "heBAGQ";
        int trainingMaterialId = 4;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Gephi: explore the networks");
        trainingMaterial.setDescription("An open source software for exploring and manipulating networks");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(get("/api/training-materials/{id}/history", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[0].id", is(trainingMaterialId)));

        mvc.perform(get("/api/training-materials/{id}/information-contributors", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].username", is("Moderator")))
                .andExpect(jsonPath("$[0].displayName", is("Moderator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[0].role", is("moderator")))
                .andExpect(jsonPath("$[0].email", is("moderator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));

        String jsonResponse = mvc.perform(put("/api/training-materials/{id}", trainingMaterialPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("description", is(trainingMaterial.getDescription())))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andReturn().getResponse().getContentAsString();


        Long versionId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, DatasetDto.class).getId();

        log.debug("Dataset version Id: " + versionId);

        mvc.perform(get("/api/training-materials/{id}/versions/{versionId}/information-contributors", trainingMaterialPersistentId, versionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[0].displayName", is("Administrator")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("Moderator")))
                .andExpect(jsonPath("$[1].displayName", is("Moderator")))
                .andExpect(jsonPath("$[1].status", is("enabled")))
                .andExpect(jsonPath("$[1].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[1].role", is("moderator")))
                .andExpect(jsonPath("$[1].email", is("moderator@example.com")))
                .andExpect(jsonPath("$[1].config", is(true)));


        Long beforeVersionId = 2l;
        int beforeVersion = 2;

        mvc.perform(get("/api/training-materials/{id}/history", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[0].id", is(versionId.intValue())))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[1].id", is(trainingMaterialId)));

        mvc.perform(get("/api/training-materials/{id}/versions/{versionId}/information-contributors", trainingMaterialPersistentId, trainingMaterialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(beforeVersion)))
                .andExpect(jsonPath("$[0].username", is("Moderator")))
                .andExpect(jsonPath("$[0].displayName", is("Moderator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[0].role", is("moderator")))
                .andExpect(jsonPath("$[0].email", is("moderator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));

    }

    @Test
    public void shouldGetMergeForTrainingMaterial() throws Exception {

        String trainingMaterialId = "heBAGQ";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        mvc.perform(
                        get("/api/training-materials/{id}/merge", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi: an open source software for exploring and manipulating networks. / Creation of a dictionary")));

    }


    @Test
    public void shouldMergeIntoTrainingMaterial() throws Exception {

        String trainingMaterialId = "heBAGQ";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        String response = mvc.perform(
                        get("/api/training-materials/{id}/merge", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi: an open source software for exploring and manipulating networks. / Creation of a dictionary")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        post("/api/training-materials/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", trainingMaterialId, workflowId, toolId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi: an open source software for exploring and manipulating networks. / Creation of a dictionary")));

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldGetSourcesForTrainingMaterial() throws Exception {

        String trainingMaterialId = "WfcKvG";

        mvc.perform(
                        get("/api/training-materials/{id}/sources", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].label", is("Programming Historian")))
                .andExpect(jsonPath("$[0].url", is("https://programminghistorian.org")));


    }


    @Test
    public void shouldGetSourcesForMergedTrainingMaterial() throws Exception {

        String trainingMaterialId = "WfcKvG";
        String datasetId = "OdKfPc";
        String toolId = "Xgufde";

        String trainingMaterialSecondId = "heBAGQ";
        String workflowId = "tqmbGY";

        mvc.perform(
                        get("/api/training-materials/{id}/sources", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(
                        get("/api/training-materials/{id}/sources", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].label", is("TAPoR")))
                .andExpect(jsonPath("$[0].url", is("http://tapor.ca")));


        mvc.perform(
                        get("/api/training-materials/{id}/sources", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].label", is("Programming Historian")))
                .andExpect(jsonPath("$[0].url", is("https://programminghistorian.org")));


        String response = mvc.perform(
                        get("/api/training-materials/{id}/merge", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI / Consortium of European Social Science Data Archives / WebSty")))
                .andReturn().getResponse().getContentAsString();


        SourceId sourceId = new SourceId();
        sourceId.setId(1l);
        String sourceItemId = "1";

        TrainingMaterialDto t = TestJsonMapper.serializingObjectMapper()
                .readValue(response, TrainingMaterialDto.class);

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Introduction to GEPHI/Consortium of European Social Science Data Archives/WebSty");
        trainingMaterial.setDescription(t.getDescription());
        trainingMaterial.setSource(sourceId);
        trainingMaterial.setSourceItemId(t.getSourceItemId());
        trainingMaterial.setAccessibleAt(t.getAccessibleAt());
        trainingMaterial.setDateCreated(t.getDateCreated());
        trainingMaterial.setVersion(t.getVersion());
        trainingMaterial.setSourceItemId(sourceItemId);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);

        String mergedResponse = mvc.perform(
                        post("/api/training-materials/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", trainingMaterialId, datasetId, toolId)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI/Consortium of European Social Science Data Archives/WebSty")))
                .andReturn().getResponse().getContentAsString();


        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, TrainingMaterialDto.class).getPersistentId();

        mvc.perform(
                        get("/api/training-materials/{id}/sources", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].label", is("TAPoR")))
                .andExpect(jsonPath("$[0].url", is("http://tapor.ca")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].label", is("Programming Historian")))
                .andExpect(jsonPath("$[1].url", is("https://programminghistorian.org")));

    }
    
    @Test
    public void shouldReturnDifferenceBetweenVersionsOfTrainingMaterials() throws Exception {
        String trainingMaterialPersistentId = "WfcKvG";
        Long trainingMaterialVersionId = 5L;

        Long otherTrainingMaterialVersionId = 6L;

        mvc.perform(get("/api/training-materials/{persistentId}/versions/{versionId}/diff", trainingMaterialPersistentId, trainingMaterialVersionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", trainingMaterialPersistentId)
                        .param("otherVersionId", otherTrainingMaterialVersionId.toString())
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("item.id", is(trainingMaterialVersionId.intValue())))
                .andExpect(jsonPath("item.version", is("1.0")))
                .andExpect(jsonPath("item.category", is("training-material")))
                .andExpect(jsonPath("item.label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("item.informationContributor.id", is(1)))
                .andExpect(jsonPath("item.status", is("deprecated")))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("other.id", is(otherTrainingMaterialVersionId.intValue())))
                .andExpect(jsonPath("other.category", is("training-material")))
                .andExpect(jsonPath("other.version", is("2.0")))
                .andExpect(jsonPath("other.status", is("deprecated")));


        mvc.perform(get("/api/training-materials/{persistentId}/versions/{versionId}/diff", trainingMaterialPersistentId, trainingMaterialVersionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", trainingMaterialPersistentId)
                        .param("otherVersionId", "7")
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("item.id", is(trainingMaterialVersionId.intValue())))
                .andExpect(jsonPath("item.version", is("1.0")))
                .andExpect(jsonPath("item.category", is("training-material")))
                .andExpect(jsonPath("item.label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("item.informationContributor.id", is(1)))
                .andExpect(jsonPath("item.status", is("deprecated")))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("other.id", is(7)))
                .andExpect(jsonPath("other.category", is("training-material")))
                .andExpect(jsonPath("other.version", is("3.0")))
                .andExpect(jsonPath("other.status", is("approved")));
    }

}
