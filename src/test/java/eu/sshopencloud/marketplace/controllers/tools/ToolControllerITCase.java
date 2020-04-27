package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
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

import java.util.ArrayList;
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
public class ToolControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnTools() throws Exception {

        mvc.perform(get("/api/tools")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnTool() throws Exception {
        Integer toolId = 1;

        mvc.perform(get("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(toolId)))
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("licenses[0].label", is("Common Development and Distribution License 1.0")))
                .andExpect(jsonPath("informationContributors", hasSize(2)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldNotReturnToolWhenNotExist() throws Exception {
        Integer toolId = 51;

        mvc.perform(get("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateToolWithoutRelation() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test simple software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Tool")));
    }

    @Test
    public void shouldCreateToolWithRelations() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test complex software");
        tool.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("apache-2.0");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        tool.setLicenses(licenses);
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses[0].label", is("Apache License 2.0")))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("Software")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("properties[2].value", is("paper")))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldCreateToolWithPrevVersion() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test complex software");
        tool.setDescription("Lorem ipsum");
        tool.setPrevVersionId(3l);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(3)))
                .andExpect(jsonPath("olderVersions[0].label", is("WebSty")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldNotCreateToolWhenObjectTypeIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("y2y4y200");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenLabelIsNull() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("label")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenLicenseIsUnknown() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        tool.setLicenses(licenses);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("licenses[0].code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenContributorIsUnknown() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(99l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].actor.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenContributorRoleIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(2l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("xxx");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenPropertyTypeIsUnknown() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].type.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenConceptIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenVocabularyIsDisallowed() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept.vocabulary")))
                .andExpect(jsonPath("errors[0].code", is("field.disallowedVocabulary")))
                .andExpect(jsonPath("errors[0].args[0]", is("iso-639-3")))
                .andExpect(jsonPath("errors[0].args[1]", is("activity")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldUpdateToolWithoutRelation() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(toolId)))
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test simple software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Tool")));
    }

    @Test
    public void shouldUpdateToolWithRelations() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test complex software");
        tool.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("mit");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        tool.setLicenses(licenses);
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(toolId)))
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses", hasSize(1)))
                .andExpect(jsonPath("licenses[0].label", is("MIT License")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(3)))
                .andExpect(jsonPath("properties[0].concept.label", is("Software")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("properties[2].value", is("paper")))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(1)));
    }

    @Test
    public void shouldNotUpdateToolWhenNotExist() throws Exception {
        Integer toolId = 99;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateToolWithPrevVersion() throws Exception {
        Integer toolId = 1;

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi");
        tool.setDescription("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.");
        tool.setPrevVersionId(3l);
        tool.setAccessibleAt("https://gephi.org/");
        tool.setRepository("https://github.com/gephi/gephi");
        LicenseId license1 = new LicenseId();
        license1.setCode("cddl-1.0");
        LicenseId license2 = new LicenseId();
        license2.setCode("gpl-3.0");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license1);
        licenses.add(license2);
        tool.setLicenses(licenses);
        ItemContributorId contributor1 = new ItemContributorId();
        ActorId actor1 = new ActorId();
        actor1.setId(5l);
        contributor1.setActor(actor1);
        ActorRoleId role1 = new ActorRoleId();
        role1.setCode("author");
        contributor1.setRole(role1);
        ItemContributorId contributor2 = new ItemContributorId();
        ActorId actor2 = new ActorId();
        actor2.setId(4l);
        contributor2.setActor(actor2);
        ActorRoleId role2 = new ActorRoleId();
        role2.setCode("founder");
        contributor2.setRole(role2);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("activity");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("Capture");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("tadirah-activity");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("keyword");
        property2.setType(propertyType2);
        property2.setValue("graph");
        PropertyCore property3 = new PropertyCore();
        PropertyTypeId propertyType3 = new PropertyTypeId();
        propertyType3.setCode("keyword");
        property3.setType(propertyType3);
        property3.setValue("social network analysis");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        properties.add(property1);
        properties.add(property2);
        properties.add(property3);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(toolId)))
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("description", is("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(3)))
                .andExpect(jsonPath("olderVersions[0].label", is("WebSty")))
                .andExpect(jsonPath("newerVersions", hasSize(0)))
                .andExpect(jsonPath("accessibleAt", is("https://gephi.org/")))
                .andExpect(jsonPath("repository", is("https://github.com/gephi/gephi")))
                .andExpect(jsonPath("licenses", hasSize(2)))
                .andExpect(jsonPath("contributors", hasSize(2)))
                .andExpect(jsonPath("properties", hasSize(4)));
    }

    @Test
    public void shouldNotUpdateToolWithPrevVersionEqualToTool() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test service");
        tool.setDescription("Lorem ipsum");
        tool.setPrevVersionId(3l);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("prevVersionId")))
                .andExpect(jsonPath("errors[0].code", is("field.cycle")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenLabelIsNull() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("label")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToollWhenObjectTypeIsAmbiguous() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("object-type");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("software");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("object-type");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        properties.add(property1);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.tooManyObjectTypes")))
                .andExpect(jsonPath("errors[0].args[0]", is("tool")))
                .andExpect(jsonPath("errors[0].args[1]", is("software")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenLicenseIsUnknown() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        tool.setLicenses(licenses);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("service");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("licenses[0].code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenContributorIsUnknown() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(99l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].actor.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenContributorRoleIsIncorrect() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(2l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("xxx");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
        VocabularyId vocabulary0 = new VocabularyId();
        vocabulary0.setCode("object-type");
        concept0.setVocabulary(vocabulary0);
        property0.setConcept(concept0);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenPropertyTypeIsUnknown() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].type.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenConceptIsIncorrect() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenVocabularyIsDisallowed() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept.vocabulary")))
                .andExpect(jsonPath("errors[0].code", is("field.disallowedVocabulary")))
                .andExpect(jsonPath("errors[0].args[0]", is("iso-639-3")))
                .andExpect(jsonPath("errors[0].args[1]", is("activity")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        Integer toolId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("software");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools/{id}", toolId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].concept")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldDeleteTool() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to delete");
        tool.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/tools")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long toolId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ToolDto.class).getId();

        mvc.perform(delete("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotDeleteToolWhenNotExist() throws Exception {
        Integer toolId = 100;

        mvc.perform(delete("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
