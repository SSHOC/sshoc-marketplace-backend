package eu.sshopencloud.marketplace.controllers.datasets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.domain.media.MediaTestUtils;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.util.MediaTestUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
public class DatasetControllerITCase {

    @RegisterExtension
    public static WireMockExtension wireMockExtension = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();
    //public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

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
    private String SYSTEM_MODERATOR_JWT;

    @BeforeEach
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
        SYSTEM_MODERATOR_JWT = LogInTestClient.getJwt(mvc, "System moderator", "q1w2e3r4t5");
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
                .andExpect(jsonPath("$[0].lastInfoUpdate", is(LocalDateTime.parse("2020-08-04T12:29:02").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
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
                .andExpect(jsonPath("$[0].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
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
        List<PropertyCore> properties = new ArrayList<>();
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
                .andExpect(jsonPath("$[1].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
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
        source.setId(2L);
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
        dataset.setAccessibleAt(List.of("Malformed Url"));

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
        source.setId(2L);
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
        SourceId sourceId = new SourceId();
        sourceId.setId(2L);
        dataset.setSource(sourceId);

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
        source.setId(-1L);
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
        List<PropertyCore> properties = new ArrayList<>();
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
    public void shouldUpdateDatasetWithRelations() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetCurrentId = 9;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test complex dataset");
        dataset.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3L);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<>();
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
        List<PropertyCore> properties = new ArrayList<>();
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
                .andExpect(jsonPath("$[1].lastInfoUpdate", is(LocalDateTime.parse("2020-08-04T12:29:02").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
                .andExpect(jsonPath("$[1].status", is("deprecated")))
                .andExpect(jsonPath("$[1].informationContributor.id", is(3)));

    }

    @Test
    public void shouldUpdateDatasetWithApprovedFalseForSystemModerator() throws Exception {
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
        List<PropertyCore> properties = new ArrayList<>();
        properties.add(property1);
        dataset.setProperties(properties);


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
                        .content(payload)
                        .param("approved", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", SYSTEM_MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test simple dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("informationContributor.username", is("System moderator")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("MIT License")));
    }

    @Test
    public void shouldNotUpdateDatasetWithSourceButWithoutSourceItemId() throws Exception {
        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test dataset with source");
        dataset.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(2L);
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
        source.setId(-1L);
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
        actor.setId(3L);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<>();
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
        List<PropertyCore> properties = new ArrayList<>();
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

        mvc.perform(get("/api/datasets/{id}", datasetPersistentId)
                        .param("approved", "false")
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("disapproved")));
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
                mvc, mapper, wireMockExtension, "grumpycat.png", "image/png", CONTRIBUTOR_JWT
        );

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);

        URL grumpyUrl = new URL("http", "localhost", wireMockExtension.getRuntimeInfo().getHttpPort(), "/grumpycat.png");

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
                mvc, mapper, wireMockExtension, "grumpycat.png", "image/png", CONTRIBUTOR_JWT
        );

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);

        URL grumpyUrl = new URL("http", "localhost", wireMockExtension.getRuntimeInfo().getHttpPort(), "/grumpycat.png");

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
                mvc, mapper, wireMockExtension, "grumpycat.png", "image/png", CONTRIBUTOR_JWT
        );


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
        UUID grumpycatId = MediaTestUploadUtils.importMedia(mvc, mapper, wireMockExtension, "grumpycat.png", "image/png", CONTRIBUTOR_JWT);
        UUID backgoundId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "jpeg_example.jpeg", CONTRIBUTOR_JWT);

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", null);
        ItemMediaCore grumpycat = new ItemMediaCore(new MediaDetailsId(grumpycatId), "Grumpy Cat", null);

        URL grumpyUrl = new URL("http", "localhost", wireMockExtension.getRuntimeInfo().getHttpPort(), "/grumpycat.png");

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
        UUID grumpycatId = MediaTestUploadUtils.importMedia(mvc, mapper, wireMockExtension, "grumpycat.png", "image/png", CONTRIBUTOR_JWT);

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
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Gephi")));

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
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Gephi")))
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
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Gephi")));


        mvc.perform(
                        get("/api/datasets/{id}?approved=false", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("deprecated")));
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
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Gephi")))
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
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Gephi")))
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

    @Test
    public void shouldGetHistoryForMergedDatasetAndTrainingMaterialWithHistory() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String trainingMaterialId = "WfcKvG";

        String response = mvc.perform(
                        get("/api/datasets/{id}/merge", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowId, trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, workflowId, trainingMaterialId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
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
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].category", is("dataset")))
                .andExpect(jsonPath("$[0].label", is(mergedLabel)))
                .andExpect(jsonPath("$[0].persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("$[1].persistentId", is(workflowId)))
                .andExpect(jsonPath("$[1].category", is("workflow")))
                .andExpect(jsonPath("$[1].id", is(12)))
                .andExpect(jsonPath("$[2].persistentId", is(datasetId)))
                .andExpect(jsonPath("$[2].category", is("dataset")))
                .andExpect(jsonPath("$[2].id", is(10)))
                .andExpect(jsonPath("$[3].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[3].category", is("training-material")))
                .andExpect(jsonPath("$[3].id", is(7)))
                .andExpect(jsonPath("$[4].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[4].category", is("training-material")))
                .andExpect(jsonPath("$[4].id", is(6)))
                .andExpect(jsonPath("$[5].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[5].category", is("training-material")))
                .andExpect(jsonPath("$[5].id", is(5)));

    }

    @Test
    public void shouldGetInformationContributorsForMergedDataset() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String trainingMaterialId = "WfcKvG";

        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("Administrator")));

        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("Contributor")));

        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", trainingMaterialId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[1].username", is("System importer")));

        String response = mvc.perform(
                        get("/api/datasets/{id}/merge", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowId, trainingMaterialId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, workflowId, trainingMaterialId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        get("/api/training-materials/{id}", trainingMaterialId)
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
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].category", is("dataset")))
                .andExpect(jsonPath("$[0].label", is(mergedLabel)))
                .andExpect(jsonPath("$[0].persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("$[1].persistentId", is(workflowId)))
                .andExpect(jsonPath("$[1].category", is("workflow")))
                .andExpect(jsonPath("$[2].persistentId", is(datasetId)))
                .andExpect(jsonPath("$[2].category", is("dataset")))
                .andExpect(jsonPath("$[3].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[3].category", is("training-material")))
                .andExpect(jsonPath("$[4].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[4].category", is("training-material")))
                .andExpect(jsonPath("$[5].persistentId", is(trainingMaterialId)))
                .andExpect(jsonPath("$[5].category", is("training-material")));

        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[1].username", is("Moderator")))
                .andExpect(jsonPath("$[2].username", is("Contributor")))
                .andExpect(jsonPath("$[3].username", is("System importer")));

    }


    @Test
    public void shouldGetInformationContributorsForMultipleMergedDataset() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String trainingMaterialId = "WfcKvG";

        String toolId = "Xgufde";
        String datasetSecondId = "dmbq4v";

        String workflowSecondId = "vHQEhe";

        String response = mvc.perform(
                        get("/api/datasets/{id}/merge", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowId, trainingMaterialId)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, workflowId, trainingMaterialId)
                                .content(response)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getPersistentId();

        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[1].username", is("Contributor")))
                .andExpect(jsonPath("$[2].username", is("System importer")));

        String response2 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", toolId, datasetSecondId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data")))
                .andReturn().getResponse().getContentAsString();


        String mergedResponse2 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedPersistentId, toolId, datasetSecondId)
                                .content(response2)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data"))).andReturn().getResponse().getContentAsString();

        String mergedSecondPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse2, DatasetDto.class).getPersistentId();

        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", mergedSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[1].username", is("Moderator")))
                .andExpect(jsonPath("$[2].username", is("Contributor")))
                .andExpect(jsonPath("$[3].username", is("System importer")));

        String response3 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowSecondId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedSecondPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse3 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedSecondPersistentId, workflowSecondId)
                                .content(response3)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedThirdPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse3, DatasetDto.class).getPersistentId();


        mvc.perform(
                        get("/api/datasets/{id}/information-contributors", mergedThirdPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[1].username", is("Moderator")))
                .andExpect(jsonPath("$[2].username", is("Contributor")))
                .andExpect(jsonPath("$[3].username", is("System importer")));

    }

    @Test
    public void shouldGetHistoryForMultipleMergedDataset() throws Exception {

        String datasetPersistentId = "OdKfPc";
        int datasetId = 10;

        String workflowPersistentId = "tqmbGY";
        int workflowId = 12;
        String trainingMaterialPersistentId = "WfcKvG";
        int trainingMaterialId = 7;

        String toolPersistentId = "Xgufde";
        int toolId = 3;
        String datasetSecondPersistentId = "dmbq4v";

        String workflowSecondPersistentId = "vHQEhe";
        int workflowSecondId = 21;

        mvc.perform(
                        get("/api/datasets/{id}/history", datasetPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("$[0].id", is(datasetId)));


        mvc.perform(
                        get("/api/workflows/{id}/history", workflowPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[0].id", is(workflowId)));

        mvc.perform(
                        get("/api/training-materials/{id}/history", trainingMaterialPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[0].id", is(trainingMaterialId)))
                .andExpect(jsonPath("$[1].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[1].id", is(6)))
                .andExpect(jsonPath("$[2].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[2].id", is(5)));

        String response = mvc.perform(
                        get("/api/datasets/{id}/merge", datasetPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowPersistentId, trainingMaterialPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetPersistentId, workflowPersistentId, trainingMaterialPersistentId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getPersistentId();

        int mergedId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getId().intValue();

        mvc.perform(
                        get("/api/datasets/{id}/history", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("$[0].id", is(mergedId)))
                .andExpect(jsonPath("$[1].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[1].id", is(workflowId)))
                .andExpect(jsonPath("$[2].persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("$[2].id", is(datasetId)))
                .andExpect(jsonPath("$[3].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[3].id", is(trainingMaterialId)))
                .andExpect(jsonPath("$[4].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[4].id", is(6)))
                .andExpect(jsonPath("$[5].persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("$[5].id", is(5)));


        mvc.perform(
                        get("/api/tools-services/{id}/history", toolPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("$[0].id", is(toolId)));


        mvc.perform(
                        get("/api/datasets/{id}/history", datasetSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].persistentId", is(datasetSecondPersistentId)))
                .andExpect(jsonPath("$[1].persistentId", is(datasetSecondPersistentId)))
                .andExpect(jsonPath("$[1].id", is(9)));

        String response2 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", toolPersistentId, datasetSecondPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data")))
                .andReturn().getResponse().getContentAsString();


        String mergedResponse2 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedPersistentId, toolPersistentId, datasetSecondPersistentId)
                                .content(response2)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data"))).andReturn().getResponse().getContentAsString();

        String mergedSecondPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse2, DatasetDto.class).getPersistentId();


        mvc.perform(
                        get("/api/datasets/{id}/history", mergedSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)));

        mvc.perform(
                        get("/api/workflows/{id}/history", workflowSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].persistentId", is(workflowSecondPersistentId)))
                .andExpect(jsonPath("$[0].id", is(workflowSecondId)));

        String response3 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowSecondPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedSecondPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse3 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedSecondPersistentId, workflowSecondPersistentId)
                                .content(response3)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedThirdPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse3, DatasetDto.class).getPersistentId();


        mvc.perform(
                        get("/api/datasets/{id}/history", mergedThirdPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(14)))
                .andExpect(jsonPath("$[0].persistentId", is(mergedThirdPersistentId)));
    }

    @Test
    public void shouldCreateDatasetWithContributorMultipleRoles() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(2L), new ActorRoleId("author"));
        ItemContributorId contributor3 = new ItemContributorId(new ActorId(2L), new ActorRoleId("provider"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(3L), new ActorRoleId("provider"));
        dataset.setContributors(List.of(contributor1, contributor2, contributor3));
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
    public void shouldReturnDifferenceBetweenDatasets() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetId = 9;
        String otherDatasetPersistentId = "OdKfPc";
        Integer otherDatasetId = 10;

        mvc.perform(get("/api/datasets/{persistentId}/diff", datasetPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", otherDatasetPersistentId)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("item.id", is(datasetId)))
                .andExpect(jsonPath("item.category", is("dataset")))
                .andExpect(jsonPath("item.label", is("Austin Crime Data")))
                .andExpect(jsonPath("item.informationContributor.id", is(3)))
                .andExpect(jsonPath("item.properties[0].type.code", is("language")))
                .andExpect(jsonPath("item.properties[0].concept.code", is("eng")))
                .andExpect(jsonPath("item.accessibleAt[0]", is("https://console.cloud.google.com/marketplace/details/city-of-austin/austin-crime")))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is(otherDatasetPersistentId)))
                .andExpect(jsonPath("other.id", is(otherDatasetId)))
                .andExpect(jsonPath("other.category", is("dataset")))
                .andExpect(jsonPath("other.label", is("Consortium of European Social Science Data Archives")))
                .andExpect(jsonPath("other.informationContributor.id", is(1)))
                .andExpect(jsonPath("other.properties[0].type.code", is("activity")))
                .andExpect(jsonPath("other.properties[0].concept.code", is("ActivityType-Seeking")))
                .andExpect(jsonPath("other.accessibleAt[0]", is("https://datacatalogue.cessda.eu/")));
    }

    @Test
    public void shouldReturnNoDifferenceBetweenDatasets() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetId = 9;
        String otherDatasetPersistentId = "dmbq4v";

        mvc.perform(get("/api/datasets/{persistentId}/diff", datasetPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", otherDatasetPersistentId)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("item.id", is(datasetId)))
                .andExpect(jsonPath("item.category", is("dataset")))
                .andExpect(jsonPath("item.label", is("Austin Crime Data")))
                .andExpect(jsonPath("item.informationContributor.id", is(3)))
                .andExpect(jsonPath("item.accessibleAt[0]", is("https://console.cloud.google.com/marketplace/details/city-of-austin/austin-crime")))
                .andExpect(jsonPath("equal", is(true)))
                .andExpect(jsonPath("other.persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("other.id", is(datasetId)))
                .andExpect(jsonPath("other.category", is("dataset")))
                .andExpect(jsonPath("other.informationContributor.id", is(3)))
                .andExpect(jsonPath("other.accessibleAt[0]", nullValue()));
    }

    @Test
    public void shouldReturnDifferenceBetweenDatasetAndTool() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetId = 9;

        String otherToolPersistentId = "Xgufde";
        Integer otherToolId = 3;

        String responseDataset = mvc.perform(
                        get("/api/datasets/{id}",datasetPersistentId)
                                .param("approved", "false")
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("id",is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Austin Crime Data")))
                .andExpect(jsonPath("informationContributor.id", is(3)))
                .andExpect(jsonPath("description", is("This dataset includes Part 1 crimes for 2014 and 2015. Data is provided by the Austin Police Department and may differ from official APD crime data due to the variety of reporting and collection methods used.")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(4)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("relatedItems[0].id", is(10)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")))

                .andReturn().getResponse().getContentAsString();

        DatasetDto dataset = TestJsonMapper.serializingObjectMapper()
                .readValue(responseDataset, DatasetDto.class);

        dataset.setDateCreated(ZonedDateTime.of(LocalDate.of(2021,12,1), LocalTime.of(3,10), ZoneId.of("Europe/Paris")));

        String payload = mapper.writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(
                        put("/api/datasets/{id}", datasetPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Austin Crime Data")))
                .andExpect(jsonPath("informationContributor.id", is(1)))
                .andExpect(jsonPath("dateCreated", is("2021-12-01T02:10:00+0000")))
                .andExpect(jsonPath("description", is("This dataset includes Part 1 crimes for 2014 and 2015. Data is provided by the Austin Police Department and may differ from official APD crime data due to the variety of reporting and collection methods used.")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(4)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("relatedItems[0].id", is(10)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-mentioned-in")));


        mvc.perform(
                get("/api/tools-services/{id}", otherToolPersistentId )
                        .param("approved", "false")
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(otherToolPersistentId)))
                .andExpect(jsonPath("id",is(otherToolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("WebSty")))
                .andExpect(jsonPath("informationContributor.id", is(1)))
                .andExpect(jsonPath("description", is("Stylometric analysis tool.")))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(5)))
                .andExpect(jsonPath("contributors[1].role.code", is("contributor")))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("relatedItems[0].id", is(1)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("relates-to")));


        mvc.perform(get("/api/datasets/{persistentId}/diff", datasetPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", otherToolPersistentId)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("item.category", is("dataset")))
                .andExpect(jsonPath("item.label", is("Austin Crime Data")))
                .andExpect(jsonPath("item.informationContributor.id", is(1)))
                .andExpect(jsonPath("item.dateCreated", is("2021-12-01T02:10:00+0000")))
                .andExpect(jsonPath("item.description", is("This dataset includes Part 1 crimes for 2014 and 2015. Data is provided by the Austin Police Department and may differ from official APD crime data due to the variety of reporting and collection methods used.")))
                .andExpect(jsonPath("item.contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("item.contributors[0].role.code", is("author")))
                .andExpect(jsonPath("item.contributors[1].actor.id", is(4)))
                .andExpect(jsonPath("item.contributors[1].role.code", is("author")))
                .andExpect(jsonPath("item.relatedItems[0].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("item.relatedItems[0].id", is(10)))
                .andExpect(jsonPath("item.relatedItems[0].relation.code", is("is-mentioned-in")))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is( otherToolPersistentId)))
                .andExpect(jsonPath("other.id", is(otherToolId)))
                .andExpect(jsonPath("other.category", is("tool-or-service")))
                .andExpect(jsonPath("other.label", is("WebSty")))
                .andExpect(jsonPath("other.informationContributor.id", is(1)))
                .andExpect(jsonPath("other.description", is("Stylometric analysis tool.")))
                .andExpect(jsonPath("other.contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("other.contributors[0].role.code", is("author")))
                .andExpect(jsonPath("other.contributors[1].actor.id", is(5)))
                .andExpect(jsonPath("other.contributors[1].role.code", is("contributor")))
                .andExpect(jsonPath("other.relatedItems[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("other.relatedItems[0].id", is(1)))
                .andExpect(jsonPath("other.relatedItems[0].relation.code", is("relates-to")));
    }


    @Test
    public void shouldReturnDifferenceBetweenDatasetAndVersionOfTrainingMaterial() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetId = 9;

        String otherTrainingMaterialPersistentId = "WfcKvG";
        Long otherTrainingMaterialVersionId = 6L;

        mvc.perform(get("/api/datasets/{persistentId}/diff", datasetPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", otherTrainingMaterialPersistentId)
                        .param("otherVersionId", otherTrainingMaterialVersionId.toString())
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("item.id", is(datasetId)))
                .andExpect(jsonPath("item.category", is("dataset")))
                .andExpect(jsonPath("item.label", is("Austin Crime Data")))
                .andExpect(jsonPath("item.informationContributor.id", is(3)))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is(otherTrainingMaterialPersistentId)))
                .andExpect(jsonPath("other.id", is(otherTrainingMaterialVersionId.intValue())))
                .andExpect(jsonPath("other.category", is("training-material")))
                .andExpect(jsonPath("other.label", is("Introduction to GEPHI")));
    }


    @Test
    public void shouldNotReturnDifferenceBetweenTrainingMaterialWhenNotExists() throws Exception {
        String trainingMaterialPersistentId = "NONEXISTING";

        String otherDatasetPersistentId = "dmbq4v";

        mvc.perform(get("/api/datasets/{persistentId}/diff", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", otherDatasetPersistentId)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRedirectToMergedDataset() throws Exception {

        String datasetPersistentId = "OdKfPc";

        String workflowPersistentId = "tqmbGY";
        String trainingMaterialPersistentId = "WfcKvG";

        String toolPersistentId = "Xgufde";
        String datasetSecondPersistentId = "dmbq4v";

        String workflowSecondPersistentId = "vHQEhe";

        String response = mvc.perform(
                        get("/api/datasets/{id}/merge", datasetPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowPersistentId, trainingMaterialPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetPersistentId, workflowPersistentId, trainingMaterialPersistentId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getPersistentId();

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId )
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedPersistentId)));

        String response2 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", toolPersistentId, datasetSecondPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data")))
                .andReturn().getResponse().getContentAsString();


        String mergedResponse2 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedPersistentId, toolPersistentId, datasetSecondPersistentId)
                                .content(response2)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data"))).andReturn().getResponse().getContentAsString();

        String mergedSecondPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse2, DatasetDto.class).getPersistentId();

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedSecondPersistentId)));


        String response3 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowSecondPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedSecondPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse3 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedSecondPersistentId, workflowSecondPersistentId)
                                .content(response3)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedThirdPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse3, DatasetDto.class).getPersistentId();


        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedThirdPersistentId)));
    }

    @Test
    public void shouldNotRedirectToMergedDataset() throws Exception {

        String datasetPersistentId = "OdKfPc";

        String workflowPersistentId = "tqmbGY";
        String trainingMaterialPersistentId = "WfcKvG";

        String toolPersistentId = "Xgufde";
        String datasetSecondPersistentId = "dmbq4v";

        String workflowSecondPersistentId = "vHQEhe";

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)));


        String response = mvc.perform(
                        get("/api/datasets/{id}/merge", datasetPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowPersistentId, trainingMaterialPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetPersistentId, workflowPersistentId, trainingMaterialPersistentId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(datasetPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI")))
                .andReturn().getResponse().getContentAsString();

        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, DatasetDto.class).getPersistentId();

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId )
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedPersistentId)));

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId )
                                .param("redirect", "false")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().is4xxClientError());

        String response2 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", toolPersistentId, datasetSecondPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data")))
                .andReturn().getResponse().getContentAsString();


        String mergedResponse2 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedPersistentId, toolPersistentId, datasetSecondPersistentId)
                                .content(response2)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data"))).andReturn().getResponse().getContentAsString();

        String mergedSecondPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse2, DatasetDto.class).getPersistentId();

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedSecondPersistentId)));


        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "false")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().is4xxClientError());

        String response3 = mvc.perform(
                        get("/api/datasets/{id}/merge", mergedSecondPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowSecondPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedSecondPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse3 = mvc.perform(
                        post("/api/datasets/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergedSecondPersistentId, workflowSecondPersistentId)
                                .content(response3)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(mergedPersistentId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives / Creation of a dictionary / Introduction to GEPHI / WebSty / Austin Crime Data / Evaluation of an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedThirdPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse3, DatasetDto.class).getPersistentId();


        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(mergedThirdPersistentId)));

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .param("redirect", "false")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldDeleteAndRevertDataset() throws Exception {

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Dataset to revert");
        dataset.setDescription("Lorem ipsum dolor");

        String datasetPayload = mapper.writeValueAsString(dataset);

        String datasetJSON = mvc.perform(
                        post("/api/datasets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(datasetPayload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())))
                .andReturn().getResponse().getContentAsString();

        DatasetDto datasetDto = mapper.readValue(datasetJSON, DatasetDto.class);

        mvc.perform(delete("/api/datasets/{id}", datasetDto.getPersistentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(
                        put("/api/datasets/{id}/revert", datasetDto.getPersistentId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        get("/api/datasets/{id}", datasetDto.getPersistentId())
                                .param("approved", "true")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", is(datasetDto.getId().intValue())))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())));
    }

    @Test
    public void shouldPatchDataset() throws Exception {
        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");
        dataset.setVersion("1.0.0");
        SourceId sourceId = new SourceId();
        sourceId.setId(1L);
        dataset.setSource(sourceId);
        dataset.setSourceItemId("patchedDataset");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        String datasetResponse = mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
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
                .andExpect(jsonPath("sourceItemId", is("patchedDataset")))
                .andExpect(jsonPath("version", is("1.0.0")))
                .andExpect(jsonPath("contributors", hasSize(0))).andReturn().getResponse().getContentAsString();

        DatasetDto datasetDto = mapper.readValue(datasetResponse, DatasetDto.class);
        String datasetPID = datasetDto.getPersistentId();

        mvc.perform(get("/api/datasets/{id}", datasetPID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", is(datasetDto.getId().intValue())))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())));

        dataset.setSourceItemId(null);
        dataset.setSource(null);
        dataset.setDescription("New description");
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
        List<PropertyCore> properties = new ArrayList<>();
        properties.add(property1);
        dataset.setProperties(properties);

        String payloadUpdated = mapper.writeValueAsString(dataset);
        String jsonUpdated = mvc.perform(
                        put("/api/datasets/{id}", datasetPID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadUpdated)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(datasetDto.getId().intValue()))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("sourceItemId").doesNotExist())
                .andExpect(jsonPath("source").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        dataset.setDescription("Patched dataset description changed!");
        dataset.setSource(sourceId);
        dataset.setSourceItemId("patchedDataset");
        dataset.setVersion(null);

        String payloadPatch = mapper.writeValueAsString(dataset);

        mvc.perform(
                        patch("/api/datasets/{id}", datasetPID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadPatch)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("sourceItemId", is("patchedDataset")))
                .andExpect(jsonPath("source").exists())
                .andExpect(jsonPath("version", is("1.0.0")))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void shouldNotPatchButUpdateDataset() throws Exception {
        String datasetPersistentId = "dmbq4v";

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");
        SourceId sourceId = new SourceId();
        sourceId.setId(1L);
        dataset.setSource(sourceId);
        dataset.setSourceItemId("patchedDataset");
        dataset.setVersion("1.0.0");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        String datasetResponse = mvc.perform(put("/api/datasets/{id}", datasetPersistentId)
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
                .andExpect(jsonPath("sourceItemId", is("patchedDataset")))
                .andExpect(jsonPath("contributors", hasSize(0))).andReturn().getResponse().getContentAsString();

        DatasetDto datasetDto = mapper.readValue(datasetResponse, DatasetDto.class);
        String datasetPID = datasetDto.getPersistentId();

        mvc.perform(get("/api/datasets/{id}", datasetPID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", is(datasetDto.getId().intValue())))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())));

        dataset.setSourceItemId(null);
        dataset.setSource(null);
        dataset.setDescription("New description");
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
        List<PropertyCore> properties = new ArrayList<>();
        properties.add(property1);
        dataset.setProperties(properties);

        String payloadUpdated = mapper.writeValueAsString(dataset);
        String jsonUpdated = mvc.perform(
                        put("/api/datasets/{id}", datasetPID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadUpdated)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(datasetDto.getId().intValue()))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("sourceItemId").doesNotExist())
                .andExpect(jsonPath("source").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        dataset.setDescription("Patched dataset description changed!");
        dataset.setSource(sourceId);
        dataset.setSourceItemId("patchedDataset");
        dataset.setVersion(null);

        String payloadPatch = mapper.writeValueAsString(dataset);

        mvc.perform(
                        put("/api/datasets/{id}", datasetPID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadPatch)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetDto.getPersistentId())))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is("Patched dataset description changed!")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("sourceItemId", is("patchedDataset")))
                .andExpect(jsonPath("source").exists())
                .andExpect(jsonPath("version").doesNotExist())
                .andReturn().getResponse().getContentAsString();
    }
}
