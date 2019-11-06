package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TrainingMaterialControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnTrainingMaterials() throws Exception {

        mvc.perform(get("/api/training-materials")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    

    @Test
    public void shouldReturnTrainingMaterial() throws Exception {
        Integer trainingMaterialId = 5;

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(2)));
    }

    @Test
    public void shouldntReturnTrainingMaterialWhenNotExist() throws Exception {
        Integer trainingMaterialId = 51;

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateTrainingMaterialWithoutRelation() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("blog");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test simple blog");
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test simple blog")));
    }

    @Test
    public void shouldCreateTrainingMaterialWithRelations() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("online-course");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test complex online course")))
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

    // TODO test many cases of prev versions
    @Test
    public void shouldCreateTrainingMaterialWithPrevVersion() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("software");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test complex software");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setPrevVersionId(2l);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("trainingMaterial")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(2)))
                .andExpect(jsonPath("olderVersions[0].label", is("Stata")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }


    @Test
    public void shouldntCreateTrainingMaterialWhenTypeIsIncorrect() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("xxx");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenLabelIsNull() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenLicenseIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
        trainingMaterial.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenContributorIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenContributorRoleIsIncorrect() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntCreateTrainingMaterialWhenPropertTypeIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenConceptIsIncorrect() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntCreateTrainingMaterialWhenVocabularyIsDisallowed() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldUpdateTrainingMaterialWithoutRelation() throws Exception {
        Integer trainingMaterialId = 2;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("software");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test simple software");
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("trainingMaterial")))
                .andExpect(jsonPath("label", is("Test simple software")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldUpdateTrainingMaterialWithRelations() throws Exception {
        Integer trainingMaterialId = 2;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("software");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test complex software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("trainingMaterial")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("licenses", hasSize(1)))
                .andExpect(jsonPath("licenses[0].label", is("MIT License")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenNotExist() throws Exception {
        Integer trainingMaterialId = 99;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("software");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test simple software");
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateTrainingMaterialWithPrevVersion() throws Exception {
        Integer trainingMaterialId = 2;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("software");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test complex software");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setPrevVersionId(1l);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("trainingMaterial")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(1)))
                .andExpect(jsonPath("olderVersions[0].label", is("Gephi")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }


    @Test
    public void shouldntUpdateTrainingMaterialWithPrevVersionEqualToTrainingMaterial() throws Exception {
        Integer trainingMaterialId = 2;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("software");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test complex software");
        trainingMaterial.setDescription("Lorem ipsum");
        trainingMaterial.setPrevVersionId(2l);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntUpdateTrainingMaterialWhenLabelIsNull() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntUpdateTrainingMaterialWhenLicenseIsUnknown() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
        trainingMaterial.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenContributorIsUnknown() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenContributorRoleIsIncorrect() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntUpdateTrainingMaterialWhenPropertTypeIsUnknown() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenConceptIsIncorrect() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntUpdateTrainingMaterialWhenVocabularyIsDisallowed() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        Integer trainingMaterialId = 3;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        TrainingMaterialTypeId trainingMaterialType = new TrainingMaterialTypeId();
        trainingMaterialType.setCode("service");
        trainingMaterial.setTrainingMaterialType(trainingMaterialType);
        trainingMaterial.setLabel("Test Software");
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

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldDeleteTrainingMaterial() throws Exception {
        Integer trainingMaterialId = 2;

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldDeleteTrainingMaterialWhenNotExist() throws Exception {
        Integer trainingMaterialId = 100;

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
