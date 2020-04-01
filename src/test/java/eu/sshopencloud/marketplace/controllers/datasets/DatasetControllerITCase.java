package eu.sshopencloud.marketplace.controllers.datasets;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import lombok.extern.slf4j.Slf4j;
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

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class DatasetControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnDatasets() throws Exception {

        mvc.perform(get("/api/datasets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnDataset() throws Exception {
        Integer datasetId = 9;

        mvc.perform(get("/api/datasets/{id}", datasetId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
        Integer datasetId = 1009;

        mvc.perform(get("/api/datasets/{id}", datasetId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateDatasetWithoutRelation() throws Exception {
        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test simple dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Dataset")));
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

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/datasets")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
                        + "  | Explicit link | `.link`         | `[]()`            |\n"
                        + "\n")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Dataset")));
    }

    @Test
    public void shouldUpdateDatasetWithoutRelation() throws Exception {
        Integer datasetId = 9;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test simple dataset");
        dataset.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test simple dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Dataset")));
    }

    @Test
    public void shouldUpdateDatsetWithRelations() throws Exception {
        Integer datasetId = 9;

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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("data-catalogue");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
        properties.add(property1);
        properties.add(property2);
        dataset.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2018, Month.APRIL, 1), LocalTime.of(12, 0), ZoneId.of("UTC"));
        dataset.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2018, Month.DECEMBER, 17), LocalTime.of(12, 20), ZoneId.of("UTC"));
        dataset.setDateLastUpdated(dateLastUpdated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dataset);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/datasets/{id}", datasetId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("label", is("Test complex dataset")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses", hasSize(1)))
                .andExpect(jsonPath("licenses[0].label", is("MIT License")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(3)))
                .andExpect(jsonPath("properties[0].concept.label", is("Data Catalogue")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("properties[2].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("data-catalogue");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long datasetId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Dataset.class).getId();

        mvc.perform(delete("/api/datasets/{id}", datasetId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
