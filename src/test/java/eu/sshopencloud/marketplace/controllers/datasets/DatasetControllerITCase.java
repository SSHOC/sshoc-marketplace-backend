package eu.sshopencloud.marketplace.controllers.datasets;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
public class DatasetControllerITCase {

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
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
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
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("informationContributors[0].username", is("Moderator")))
                .andExpect(jsonPath("licenses", hasSize(0)))
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
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("informationContributors[0].username", is("Administrator")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(datasetCurrentId)))
                .andExpect(jsonPath("olderVersions[0].label", is("Austin Crime Data")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldUpdateDatsetWithRelations() throws Exception {
        String datasetPersistentId = "dmbq4v";
        Integer datasetCurrentId = 9;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test complex dataset");
        dataset.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("mit");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        dataset.setLicenses(licenses);
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
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("informationContributors[0].username", is("Administrator")))
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
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(datasetCurrentId)))
                .andExpect(jsonPath("olderVersions[0].label", is("Austin Crime Data")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
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
        LicenseId license = new LicenseId();
        license.setCode("apache-2.0");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        dataset.setLicenses(licenses);
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

}
