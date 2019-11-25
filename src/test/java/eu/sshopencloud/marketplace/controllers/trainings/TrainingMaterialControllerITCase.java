package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
                .andExpect(jsonPath("newerVersions", hasSize(3)));
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
        trainingMaterial.setLabel("Test simple blog");
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test simple blog")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Training material")));
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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("online-course");
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
                .andExpect(jsonPath("properties[0].concept.label", is("Online course")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("properties[2].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldCreateTrainingMaterialWithPrevVersionInChain() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test complex online course");
        trainingMaterial.setDescription("Lorem Ipsum ...");
        trainingMaterial.setPrevVersionId(7l);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("online-course");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test complex online course")))
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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("online-course");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test complex online course")))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[0].id", is(6)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("2.0")))
                .andExpect(jsonPath("newerVersions", hasSize(2)))
                .andExpect(jsonPath("newerVersions[0].id", is(7)))
                .andExpect(jsonPath("newerVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("newerVersions[0].version", is("3.0")));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenObjectTypeIsIncorrect() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("x1x2x1");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntCreateTrainingMaterialWhenObjectTypeIsAmbiguous() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("blog");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("object-type");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("paper");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("object-type");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        properties.add(property1);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenLabelIsNull() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntCreateTrainingMaterialWhenLicenseIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenContributorIsUnknown() throws Exception {
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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenContributorRoleIsIncorrect() throws Exception {
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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(post("/api/training-materials")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateTrainingMaterialWhenPropertyTypeIsUnknown() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("blog");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        Integer trainingMaterialId = 5;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test simple training material");
        trainingMaterial.setDescription("Lorem ipsum");

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Test simple training material")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Training material")));
    }

    @Test
    public void shouldUpdateTrainingMaterialWithRelations() throws Exception {
        Integer trainingMaterialId = 5;

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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("blog");
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
        trainingMaterial.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2018, Month.APRIL, 1), LocalTime.of(12, 0), ZoneId.of("UTC"));
        trainingMaterial.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2018, Month.DECEMBER, 17), LocalTime.of(12, 20), ZoneId.of("UTC"));
        trainingMaterial.setDateLastUpdated(dateLastUpdated);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("licenses", hasSize(1)))
                .andExpect(jsonPath("licenses[0].label", is("MIT License")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(3)))
                .andExpect(jsonPath("properties[0].concept.label", is("Blog")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("properties[2].value", is("paper")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(1)))
                .andExpect(jsonPath("newerVersions[0].id", is(7)))
                .andExpect(jsonPath("newerVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("newerVersions[0].version", is("3.0")));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenNotExist() throws Exception {
        Integer trainingMaterialId = 99;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateTrainingMaterialWithPrevVersionForEndOfChain() throws Exception {
        Integer trainingMaterialId = 5;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Introduction to GEPHI");
        trainingMaterial.setVersion("1.0");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        trainingMaterial.setPrevVersionId(7l);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[0].id", is(7)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("3.0")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldUpdateTrainingMaterialWithPrevVersionForMiddleOfChain() throws Exception {
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Introduction to GEPHI");
        trainingMaterial.setVersion("3.0");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        trainingMaterial.setPrevVersionId(5l);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[0].id", is(5)))
                .andExpect(jsonPath("olderVersions[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[0].version", is("1.0")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWithPrevVersionEqualToTrainingMaterial() throws Exception {
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        trainingMaterial.setPrevVersionId(7l);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenLabelIsNull() throws Exception {
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenLicenseIsUnknown() throws Exception {
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        trainingMaterial.setLicenses(licenses);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenContributorIsUnknown() throws Exception {
        Integer trainingMaterialId = 7;

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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntUpdateTrainingMaterialWhenContributorRoleIsIncorrect() throws Exception {
        Integer trainingMaterialId = 7;

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
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        trainingMaterial.setProperties(properties);

        mvc.perform(put("/api/training-materials/{id}", trainingMaterialId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterial))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntUpdateTrainingMaterialWhenPropertyTypeIsUnknown() throws Exception {
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        Integer trainingMaterialId = 7;

        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Test training material");
        trainingMaterial.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("paper");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
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
        properties.add(property0);
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
        Integer trainingMaterialId = 8;

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldDeleteTrainingMaterialAndSwitchVersions() throws Exception {
        Integer trainingMaterialId = 6;

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        trainingMaterialId = 7;

        mvc.perform(get("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(trainingMaterialId)))
                .andExpect(jsonPath("olderVersions", hasSize(2)))
                .andExpect(jsonPath("olderVersions[1].id", is(5)))
                .andExpect(jsonPath("olderVersions[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("olderVersions[1].version", is("1.0")))
                .andExpect(jsonPath("newerVersions", hasSize(1)));

    }

    @Test
    public void shouldntDeleteTrainingMaterialWhenNotExist() throws Exception {
        Integer trainingMaterialId = 100;

        mvc.perform(delete("/api/training-materials/{id}", trainingMaterialId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
