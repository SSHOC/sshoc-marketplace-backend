package eu.sshopencloud.marketplace.controllers.datasets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.domain.media.MediaTestUtils;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.util.MediaTestUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class DatasetControllerITCase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ObjectMapper mapper;

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
    }

    @Test
    public void shouldReturnDatasets() throws Exception {

        mvc.perform(get("/api/datasets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnDataset() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetId = 9;

        mvc.perform(get("/api/datasets/{id}", datasetPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("id", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Austin Crime Data")))
                .andExpect(jsonPath("informationContributor.id", is(3)));
    }


    @Test
    public void shouldReturnDatasetHistory() throws Exception {

        String datasetPersistentId = "dmbq4v";
        Integer datasetId = 9;

        mvc.perform(get("/api/datasets/{id}/history", datasetPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(datasetId)))
                .andExpect(jsonPath("$[0].category", is("dataset")))
                .andExpect(jsonPath("$[0].label", is("Austin Crime Data")))
                .andExpect(jsonPath("$[0].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("$[0].lastInfoUpdate", is("2020-08-04T12:29:02+0200")))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].informationContributor.id", is(3)));
    }

    @Test
    public void shouldReturnDatasetInformationContributors() throws Exception {

        String datasetPersistentId = "dmbq4v";

        mvc.perform(get("/api/datasets/{id}/information-contributors", datasetPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].username", is("Contributor")))
                .andExpect(jsonPath("$[0].displayName", is("Contributor")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[0].role", is("contributor")))
                .andExpect(jsonPath("$[0].email", is("contributor@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));
    }

    @Test
    public void shouldReturnDatasetInformationContributorsForVersion() throws Exception {

        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("license");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("MIT");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("software-license");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        dataset.setProperties(properties);
        dataset.setVersion("2.0");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test simple dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("MIT License")))
                .andReturn().getResponse().getContentAsString();


        Long versionId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, DatasetDto.class).getId();

        log.debug("Dataset version Id: " + versionId);

        mvc.perform(get("/api/datasets/{id}/versions/{versionId}/information-contributors", datasetPersistentId, versionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[0].displayName", is("Administrator")))
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[1].username", is("Contributor")))
                .andExpect(jsonPath("$[1].displayName", is("Contributor")))
                .andExpect(jsonPath("$[1].status", is("enabled")))
                .andExpect(jsonPath("$[1].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[1].role", is("contributor")))
                .andExpect(jsonPath("$[1].email", is("contributor@example.com")))
                .andExpect(jsonPath("$[1].config", is(true)));
    }


    @Test
    public void shouldNotReturnDatasetWhenNotExist() throws Exception {
        String datasetPersistentId = "xxxxxx7";

        mvc.perform(get("/api/datasets/{id}", datasetPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateDatasetWithoutRelation() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(2L), new ActorRoleId("author"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(3L), new ActorRoleId("provider"));
        dataset.setContributors(List.of(contributor1, contributor2));

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test simple dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors[0].actor.id", is(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(3)))
                .andExpect(jsonPath("contributors[1].role.code", is("provider")))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldCreateDatasetWithSourceAndSourceItemId() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(2l);
        dataset.setSource(source);
        dataset.setSourceItemId("testSourceItemId");

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test dataset with source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("testSourceItemId")));
    }

    @Test
    public void shouldCreateDatasetWithHtmlInDescription() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with HTML in description");
        dataset.setDescription("<div>Description\n"
                + "  <p>Lorem ipsum <code>class</code> <i>Ctrl</i> <strong>Alt</strong> <a href='http://example.com'>link</a></p>\n"
                + "  <ul>\n"
                + "    <li>Item 1</li>\n"
                + "    <li>\n"
                + "      <table>\n"
                + "        <thead>\n"
                + "          <tr><th> Element</th><th>Abbreviation</th><th>Expansion</th></tr>\n"
                + "        </thead>\n"
                + "        <tbody>\n"
                + "          <tr><td>Abbreviation</td><td><code>.abbreviation</code></td><td><code>*[]:</code></td></tr>\n"
                + "          <tr><td>Code fence</td><td><code>.codefence</code></td><td>``` ... ```</td></tr>\n"
                + "          <tr><td>Explicit link</td><td><code>.link</code></td><td><code>[]()</code></td></tr>\n"
                + "        </tbody>\n"
                + "      </table>\n"
                + "    </li>\n"
                + "  </ul>\n"
                + "</div>");

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test dataset with HTML in description")))
                .andExpect(jsonPath("description", is("Description\n"
                        + "\n"
                        + "Lorem ipsum `class` *Ctrl* **Alt** [link](http://example.com)\n"
                        + "\n"
                        + "* Item 1\n"
                        + "*\n"
                        + "\n"
                        + "  |    Element    |  Abbreviation   |     Expansion     |\n"
                        + "  |---------------|-----------------|-------------------|\n"
                        + "  | Abbreviation  | `.abbreviation` | `*[]:`            |\n"
                        + "  | Code fence    | `.codefence`    | \\`\\`\\` ... \\`\\`\\` |\n"
                        + "  | Explicit link | `.link`         | `[]()`            |\n")))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldNotCreateDatasetWhenAccessibleAtIsMalformed() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with malformed Url");
        dataset.setDescription("Lorem ipsum");
        dataset.setAccessibleAt(Arrays.asList("Malformed Url"));

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("accessibleAt")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldCreateDatasetWithAccessibleAtAndSourceAndSourceItemId() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(2l);
        dataset.setSource(source);
        dataset.setSourceItemId("testSourceItemId");

        dataset.setAccessibleAt(
                Arrays.asList(
                        "https://test1.programminghistorian.org",
                        "https://test2.programminghistorian.org",
                        "https://test3.programminghistorian.org"
                )
        );

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test dataset with source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("testSourceItemId")))
                .andExpect(jsonPath("accessibleAt", hasSize(3)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://test1.programminghistorian.org")))
                .andExpect(jsonPath("accessibleAt[1]", is("https://test2.programminghistorian.org")))
                .andExpect(jsonPath("accessibleAt[2]", is("https://test3.programminghistorian.org")));
    }

    @Test
    public void shouldCreateDatasetWithAccessibleAtWithSourceUrl() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        dataset.setSourceItemId("testSourceItemId");

        dataset.setAccessibleAt(
                Arrays.asList(
                        "https://programminghistorian.org",
                        "https://test.programminghistorian.org",
                        "https://dev.programminghistorian.org"
                )
        );

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test dataset with source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sourceItemId", is("testSourceItemId")))
                .andExpect(jsonPath("accessibleAt", hasSize(3)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://programminghistorian.org")))
                .andExpect(jsonPath("accessibleAt[1]", is("https://test.programminghistorian.org")))
                .andExpect(jsonPath("accessibleAt[2]", is("https://dev.programminghistorian.org")));
    }

    @Test
    public void shouldNotCreateDatasetWhenSourceNotExist() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(-1l);
        dataset.setSource(source);

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("source.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateDatasetWhenActorHasRepeatedRoles() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(2L), new ActorRoleId("author"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(2L), new ActorRoleId("provider"));
        ItemContributorId contributor3 = new ItemContributorId(new ActorId(2L), new ActorRoleId("provider"));
        dataset.setContributors(List.of(contributor1, contributor2, contributor3));

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("contributors[2]")))
                .andExpect(jsonPath("errors[0].code", is("field.repeated")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldCreateDatasetWhenActorHasManyRoles() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Label");
        dataset.setDescription("Lorem ipsum dolor");

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(1L), new ActorRoleId("contributor"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(1L), new ActorRoleId("author"));
        ItemContributorId contributor3 = new ItemContributorId(new ActorId(2L), new ActorRoleId("contributor"));

        dataset.setContributors(List.of(contributor1, contributor2, contributor3));

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Label")))
                .andExpect(jsonPath("description", is("Lorem ipsum dolor")))
                .andExpect(jsonPath("informationContributor.username", is("Moderator")))
                .andExpect(jsonPath("contributors", hasSize(3)))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("contributor")))
                .andExpect(jsonPath("contributors[1].actor.id", is(1)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("contributors[2].actor.id", is(2)))
                .andExpect(jsonPath("contributors[2].role.code", is("contributor")));
    }

    @Test
    public void shouldUpdateDatasetWithoutRelation() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetCurrentId = 9;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("license");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("MIT");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("software-license");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        dataset.setProperties(properties);


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test simple dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("MIT License")));
    }

    @Test
    public void shouldUpdateDatsetWithRelations() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetCurrentId = 9;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test complex dataset");
        dataset.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        dataset.setContributors(contributors);
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
        dataset.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2018, Month.APRIL, 1), LocalTime.of(12, 0), ZoneId.of("UTC"));
        dataset.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2018, Month.DECEMBER, 17), LocalTime.of(12, 20), ZoneId.of("UTC"));
        dataset.setDateLastUpdated(dateLastUpdated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test complex dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));

        mvc.perform(get("/api/datasets/{id}/history", datasetPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("dataset")))
                .andExpect(jsonPath("$[0].label", is("Test complex dataset")))
                .andExpect(jsonPath("$[0].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].informationContributor.id", is(1)))
                .andExpect(jsonPath("$[0].informationContributor.role", is("administrator")))
                .andExpect(jsonPath("$[1].id", is(datasetCurrentId)))
                .andExpect(jsonPath("$[1].category", is("dataset")))
                .andExpect(jsonPath("$[1].label", is("Austin Crime Data")))
                .andExpect(jsonPath("$[1].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("$[1].lastInfoUpdate", is("2020-08-04T12:29:02+0200")))
                .andExpect(jsonPath("$[1].status", is("deprecated")))
                .andExpect(jsonPath("$[1].informationContributor.id", is(3)));

    }

    @Test
    public void shouldNotUpdateDatasetWithSourceButWithoutSourceItemId() throws Exception {
        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(2l);
        dataset.setSource(source);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("sourceItemId")))
                .andExpect(jsonPath("errors[0].code", is("field.requiredInCase")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateDatasetWithSourceItemIdButWithoutSource() throws Exception {
        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        dataset.setSourceItemId("0123");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("source")))
                .andExpect(jsonPath("errors[0].code", is("field.requiredInCase")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }


    @Test
    public void shouldNotUpdateDatasetWhenSourceNotExist() throws Exception {
        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(-1l);
        dataset.setSource(source);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("source.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldDeleteDataset() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Dataset to delete");
        dataset.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        dataset.setContributors(contributors);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("language");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setUri("http://iso639-3.sil.org/code/eng");
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("material");
        property2.setType(propertyType2);
        property2.setValue("paper");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        dataset.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2018, Month.APRIL, 1), LocalTime.of(12, 0), ZoneId.of("UTC"));
        dataset.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2018, Month.DECEMBER, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        dataset.setDateLastUpdated(dateLastUpdated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String datasetPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, DatasetDto.class).getPersistentId();

        mvc.perform(delete("/api/datasets/{id}", datasetPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotCreateDatasetWithInvalidUrlProperty() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with no url");
        dataset.setDescription("Lorem ipsum dolor sit amet");

        PropertyCore property1 = new PropertyCore(new PropertyTypeId("media"), "https://google.com");
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("media"), "this:/is-not-an-url");
        dataset.setProperties(List.of(property1, property2));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].value")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldRetrieveSuggestedDataset() throws Exception {
        String datasetId = "OdKfPc";
        int datasetVersionId = 10;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Suggested dataset");
        dataset.setDescription("This is a suggested dataset");

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                put("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .param("approved", "false")
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .param("approved", "false")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", is(datasetVersionId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")));

        mvc.perform(get("/api/datasets/{id}", datasetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", is(datasetVersionId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")));
    }

    @Test
    public void shouldUpdateAndValidateAccessToSuggestedItemVersion() throws Exception {
        String datasetId = "OdKfPc";
        int datasetVersionId = 10;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Suggested dataset version");
        dataset.setDescription("This is a suggested dataset version");

        String payload = mapper.writeValueAsString(dataset);

        String datasetResponse = mvc.perform(
                put("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("ingested")))
                .andReturn().getResponse().getContentAsString();

        DatasetDto datasetDto = mapper.readValue(datasetResponse, DatasetDto.class);
        int newDatasetVersionId = datasetDto.getId().intValue();

        mvc.perform(
                get("/api/datasets/{id}/versions/{verId}", datasetId, newDatasetVersionId)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                get("/api/datasets/{id}/versions/{verId}", datasetId, newDatasetVersionId)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                get("/api/datasets/{id}/versions/{verId}", datasetId, newDatasetVersionId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                get("/api/datasets/{id}/versions/{verId}", datasetId, newDatasetVersionId)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/datasets/{id}/versions/{verId}", datasetId, newDatasetVersionId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateDatasetWithMediaAndImportedThumbnail() throws Exception {
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);
        UUID grumpycatId = MediaTestUploadUtils.importMedia(
                mvc, mapper, wireMockRule, "grumpycat.png", "image/png", CONTRIBUTOR_JWT
        );

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);

        URL grumpyUrl = new URL("http", "localhost", wireMockRule.port(), "/grumpycat.png");

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A dataset of cats");
        dataset.setDescription("This dataset contains cats");
        dataset.setMedia(List.of(grumpycat, seriouscat));
        dataset.setThumbnail(new ItemMediaCore(new MediaDetailsId(grumpycatId), "Not used caption as the thumbnail is one of item media (shared)", null));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("thumbnail.info.mediaId", is(grumpycatId.toString())))
                .andExpect(jsonPath("thumbnail.info.category", is("image")))
                .andExpect(jsonPath("thumbnail.info.location.sourceUrl", is(grumpyUrl.toString())))
                .andExpect(jsonPath("thumbnail.info.mimeType", is("image/png")))
                .andExpect(jsonPath("thumbnail.info.hasThumbnail", is(true)))
                .andExpect(jsonPath("thumbnail.caption", is("Grumpy Cat")))
                .andExpect(jsonPath("media", hasSize(2)))
                .andExpect(jsonPath("media[0].info.mediaId", is(grumpycatId.toString())))
                .andExpect(jsonPath("media[0].info.category", is("image")))
                .andExpect(jsonPath("media[0].info.location.sourceUrl", is(grumpyUrl.toString())))
                .andExpect(jsonPath("media[0].info.mimeType", is("image/png")))
                .andExpect(jsonPath("media[0].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[0].caption", is("Grumpy Cat")))
                .andExpect(jsonPath("media[1].info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("media[1].info.category", is("image")))
                .andExpect(jsonPath("media[1].info.filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("media[1].info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("media[1].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[1].caption", is("Serious Cat")));

        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, seriouscatId));
        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, grumpycatId));
    }

    @Test
    public void shouldUpdateDatasetWithMediaAndUploadedThumbnail() throws Exception {
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);
        UUID grumpycatId = MediaTestUploadUtils.importMedia(
                mvc, mapper, wireMockRule, "grumpycat.png", "image/png", CONTRIBUTOR_JWT
        );

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);

        URL grumpyUrl = new URL("http", "localhost", wireMockRule.port(), "/grumpycat.png");

        String datasetId = "OdKfPc";
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Consortium of European Social Science Data Archives v2");
        dataset.setDescription("Consortium of European Social Science Data Archives with many cat pictures");
        dataset.setMedia(List.of(grumpycat, seriouscat));
        dataset.setThumbnail(new ItemMediaCore(new MediaDetailsId(seriouscatId), null, null));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                put("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("thumbnail.info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("thumbnail.info.category", is("image")))
                .andExpect(jsonPath("thumbnail.info.filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("thumbnail.info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("thumbnail.info.hasThumbnail", is(true)))
                .andExpect(jsonPath("thumbnail.caption", is("Serious Cat")))
                .andExpect(jsonPath("media", hasSize(2)))
                .andExpect(jsonPath("media[0].info.mediaId", is(grumpycatId.toString())))
                .andExpect(jsonPath("media[0].info.category", is("image")))
                .andExpect(jsonPath("media[0].info.location.sourceUrl", is(grumpyUrl.toString())))
                .andExpect(jsonPath("media[0].info.mimeType", is("image/png")))
                .andExpect(jsonPath("media[0].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[0].caption", is("Grumpy Cat")))
                .andExpect(jsonPath("media[1].info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("media[1].info.category", is("image")))
                .andExpect(jsonPath("media[1].info.filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("media[1].info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("media[1].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[1].caption", is("Serious Cat")));

        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, seriouscatId));
        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, grumpycatId));
    }

    @Test
    public void shouldPreventInvalidMediaUpload() throws Exception {
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);
        UUID grumpycatId = MediaTestUploadUtils.importMedia(
                mvc, mapper, wireMockRule, "grumpycat.png", "image/png", CONTRIBUTOR_JWT
        );

        //ELIZA  - TO FIX NULLS
        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);
        ItemMediaCore notFound1 = new ItemMediaCore(new MediaDetailsId(UUID.randomUUID()), "404", null);
        ItemMediaCore notFound2 = new ItemMediaCore(new MediaDetailsId(UUID.randomUUID()), "404", null);

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A dataset of cats");
        dataset.setDescription("This dataset contains cats");
        dataset.setMedia(List.of(notFound1, grumpycat, seriouscat, grumpycat, notFound2));
        dataset.setThumbnail(new ItemMediaCore(new MediaDetailsId(grumpycatId), "Thumbnail", null));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)))
                .andExpect(jsonPath("errors[0].field", is("media[0].info.mediaId")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()))
                .andExpect(jsonPath("errors[1].field", is("media[3].info.mediaId")))
                .andExpect(jsonPath("errors[1].code", is("field.duplicateEntry")))
                .andExpect(jsonPath("errors[1].message", notNullValue()))
                .andExpect(jsonPath("errors[2].field", is("media[4].info.mediaId")))
                .andExpect(jsonPath("errors[2].code", is("field.notExist")))
                .andExpect(jsonPath("errors[2].message", notNullValue()));

        assertTrue(MediaTestUtils.isMediaTemporary(entityManager, seriouscatId));
        assertTrue(MediaTestUtils.isMediaTemporary(entityManager, grumpycatId));
    }

    @Test
    public void shouldCreateDatasetWithMediaWithoutThumbnailIncludedInMedia() throws Exception {
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);
        UUID grumpycatId = MediaTestUploadUtils.importMedia(mvc, mapper, wireMockRule, "grumpycat.png", "image/png", CONTRIBUTOR_JWT);
        UUID backgoundId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "jpeg_example.jpeg", CONTRIBUTOR_JWT);

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);

        URL grumpyUrl = new URL("http", "localhost", wireMockRule.port(), "/grumpycat.png");

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A dataset of cats");
        dataset.setDescription("This dataset contains cats");
        dataset.setMedia(List.of(grumpycat, seriouscat));

        dataset.setThumbnail(new ItemMediaCore(new MediaDetailsId(backgoundId), "Thumbnail caption as it is separated image (not shared with item media)", null));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("thumbnail.info.mediaId", is(backgoundId.toString())))
                .andExpect(jsonPath("thumbnail.info.category", is("image")))
                .andExpect(jsonPath("thumbnail.info.filename", is("jpeg_example.jpeg")))
                .andExpect(jsonPath("thumbnail.info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("thumbnail.info.hasThumbnail", is(true)))
                .andExpect(jsonPath("thumbnail.caption", is("Thumbnail caption as it is separated image (not shared with item media)")))
                .andExpect(jsonPath("media", hasSize(2)))
                .andExpect(jsonPath("media[0].info.mediaId", is(grumpycatId.toString())))
                .andExpect(jsonPath("media[0].info.category", is("image")))
                .andExpect(jsonPath("media[0].info.location.sourceUrl", is(grumpyUrl.toString())))
                .andExpect(jsonPath("media[0].info.mimeType", is("image/png")))
                .andExpect(jsonPath("media[0].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[0].caption", is("Grumpy Cat")))
                .andExpect(jsonPath("media[1].info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("media[1].info.category", is("image")))
                .andExpect(jsonPath("media[1].info.filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("media[1].info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("media[1].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[1].caption", is("Serious Cat")));

        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, seriouscatId));
        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, grumpycatId));

    }


    @Test
    public void shouldCreateDatasetWithMediaWithLicenseFromUri() throws Exception {
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);

        ConceptId conceptIdUri = new ConceptId();
        conceptIdUri.setUri("http://spdx.org/licenses/0BSD");

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", conceptIdUri);

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A dataset of cats");
        dataset.setDescription("This dataset contains cats");
        dataset.setMedia(List.of(seriouscat));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("media", hasSize(1)))
                .andExpect(jsonPath("media[0].info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("media[0].info.category", is("image")))
                .andExpect(jsonPath("media[0].info.filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("media[0].info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("media[0].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[0].caption", is("Serious Cat")))
                .andExpect(jsonPath("media[0].concept.code", notNullValue()))
                .andExpect(jsonPath("media[0].concept.vocabulary.code", is("software-license")))
                .andExpect(jsonPath("media[0].concept.uri", is(conceptIdUri.getUri())));

    }


    @Test
    public void shouldCreateDatasetWithMediaWithLicenseFromCodeAndVocabularyCode() throws Exception {
        UUID grumpycatId = MediaTestUploadUtils.importMedia(mvc, mapper, wireMockRule, "grumpycat.png", "image/png", CONTRIBUTOR_JWT);

        ConceptId conceptIdCode = new ConceptId();
        conceptIdCode.setCode("AFL-3.0");
        conceptIdCode.setVocabulary(new VocabularyId("software-license"));

        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", conceptIdCode);

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A dataset of cats");
        dataset.setDescription("This dataset contains cats");
        dataset.setMedia(List.of(grumpycat));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                post("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("media", hasSize(1)))
                .andExpect(jsonPath("media[0].info.mediaId", is(grumpycatId.toString())))
                .andExpect(jsonPath("media[0].info.category", is("image")))
                .andExpect(jsonPath("media[0].info.mimeType", is("image/png")))
                .andExpect(jsonPath("media[0].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[0].caption", is("Grumpy Cat")))
                .andExpect(jsonPath("media[0].concept.code", is(conceptIdCode.getCode())))
                .andExpect(jsonPath("media[0].concept.vocabulary.code", is(conceptIdCode.getVocabulary().getCode())))
                .andExpect(jsonPath("media[0].concept.uri", is("http://spdx.org/licenses/AFL-3.0")));

    }

    @Test
    public void shouldGetMergeForDataset() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        mvc.perform(
                get("/api/datasets/{id}/merge", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", workflowId, toolId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives/Creation of a dictionary/Gephi")));

    }

    @Test
    public void shouldMergeIntoDataset() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        String response = mvc.perform(
                get("/api/datasets/{id}/merge", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", workflowId, toolId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives/Creation of a dictionary/Gephi")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                post("/api/datasets/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", datasetId, workflowId, toolId)
                        .content(response)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives/Creation of a dictionary/Gephi")));


        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldGetHistoryForMergedDataset() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        String response = mvc.perform(
                get("/api/datasets/{id}/merge", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", workflowId, toolId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives/Creation of a dictionary/Gephi")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                post("/api/datasets/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", datasetId, workflowId, toolId)
                        .content(response)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives/Creation of a dictionary/Gephi")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isNotFound());


        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getPersistentId();

        String mergedLabel = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getLabel();

        mvc.perform(
                get("/api/datasets/{id}/history", mergedPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].category", is("dataset")))
                .andExpect(jsonPath("$[0].label", is(mergedLabel)))
                .andExpect(jsonPath("$[0].persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("$[1].persistentId", is(workflowId)))
                .andExpect(jsonPath("$[1].category", is("workflow")))
                .andExpect(jsonPath("$[2].persistentId", is(datasetId)))
                .andExpect(jsonPath("$[2].category", is("dataset")))
                .andExpect(jsonPath("$[3].persistentId", is(toolId)))
                .andExpect(jsonPath("$[3].category", is("tool-or-service")));

    }

}
