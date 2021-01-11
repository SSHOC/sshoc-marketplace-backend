package eu.sshopencloud.marketplace.controllers.trainings;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
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

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("informationContributor.id", is(1)))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
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
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("version", is("1.0")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("informationContributor.id", is(1)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(2)));
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
        LicenseId license = new LicenseId();
        license.setCode("apache-2.0");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);
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
                .andExpect(jsonPath("licenses[0].label", is("Apache License 2.0")))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    /*
    @Test
    public void shouldCreateTrainingMaterialWithPrevVersionInChain() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test complex online course");
        trainingMaterial.setDescription("Lorem Ipsum ...");
        trainingMaterial.setPrevVersionId(7l);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test complex online course")))
                .andExpect(jsonPath("description", is("Lorem Ipsum ...")))
                .andExpect(jsonPath("olderVersions", hasSize(3)))
                .andExpect(jsonPath("olderVersions[0].id", is(7)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("3.0")))
                .andExpect(jsonPath("olderVersions[1].id", is(6)))
                .andExpect(jsonPath("olderVersions[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[1].version", is("2.0")))
                .andExpect(jsonPath("olderVersions[2].id", is(5)))
                .andExpect(jsonPath("olderVersions[2].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[2].version", is("1.0")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldCreateTrainingMaterialWithPrevVersionInSubChain() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test complex online course");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setPrevVersionId(6l);

        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test complex online course")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[0].id", is(6)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("2.0")))
                .andExpect(jsonPath("newerVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions[0].id", is(7)))
                .andExpect(jsonPath("newerVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("newerVersions[0].version", is("3.0")));
    }
    */

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
    public void shouldNotCreateTrainingMaterialWhenLicenseIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/training-materials")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("licenses[0].code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
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
        int prevVersionId = 7;

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
                .andExpect(jsonPath("sourceItemId", is("9999")))
                .andExpect(jsonPath("olderVersions", hasSize(3)))
                .andExpect(jsonPath("olderVersions[0].id", is(7)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("3.0")));

        mvc.perform(
                get("/api/training-materials/{id}?draft=true", trainingMaterialId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isNotFound());

        mvc.perform(
                get("/api/training-materials/{id}", trainingMaterialId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(trainingMaterial.getLabel())))
                .andExpect(jsonPath("olderVersions", hasSize(3)))
                .andExpect(jsonPath("olderVersions[0].id", is(7)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("3.0")));
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
                .andExpect(jsonPath("licenses", hasSize(0)))
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
        LicenseId license = new LicenseId();
        license.setCode("mit");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);
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
                .andExpect(jsonPath("licenses", hasSize(1)))
                .andExpect(jsonPath("licenses[0].label", is("MIT License")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))))
                .andExpect(jsonPath("olderVersions", hasSize(3)))
                .andExpect(jsonPath("olderVersions[0].id", is(7)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("3.0")))
                .andExpect(jsonPath("olderVersions[1].id", is(6)))
                .andExpect(jsonPath("olderVersions[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[1].version", is("2.0")))
                .andExpect(jsonPath("olderVersions[2].id", is(5)))
                .andExpect(jsonPath("olderVersions[2].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[2].version", is("1.0")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
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
    public void shouldUpdateTrainingMaterialWithPrevVersionForMiddleOfChain() throws Exception {
        String trainingMaterialId = "WfcKvG";

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
                .andExpect(jsonPath("description", is("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")))
                .andExpect(jsonPath("olderVersions", hasSize(3)))
                .andExpect(jsonPath("olderVersions[0].id", is(7)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("3.0")))
                .andExpect(jsonPath("olderVersions[1].id", is(6)))
                .andExpect(jsonPath("olderVersions[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[1].version", is("2.0")))
                .andExpect(jsonPath("olderVersions[2].id", is(5)))
                .andExpect(jsonPath("olderVersions[2].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[2].version", is("1.0")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
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
    public void shouldNotUpdateTrainingMaterialWhenLicenseIsUnknown() throws Exception {
        String trainingMaterialId = "WfcKvG";

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        trainingMaterial.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("licenses[0].code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
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

        String trainingMaterialId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, TrainingMaterialDto.class).getPersistentId();

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotDeleteTrainingMaterialWhenNotExist() throws Exception {
        String trainingMaterialId = "noting";

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
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
}
