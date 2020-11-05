package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
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
public class ToolControllerITCase {

    @Autowired
    private MockMvc mvc;

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
    public void shouldReturnTools() throws Exception {

        mvc.perform(get("/api/tools-services")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnTool() throws Exception {
        String toolPersistentId = "n21Kfc";
        Integer toolId = 1;

        mvc.perform(get("/api/tools-services/{id}", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("licenses[0].label", is("Common Development and Distribution License 1.0")))
                .andExpect(jsonPath("informationContributors", hasSize(2)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldNotReturnToolWhenNotExist() throws Exception {
        String toolPersistentId = "xxxxxx7";

        mvc.perform(get("/api/tools-services/{id}", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateToolWithoutSource() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");
        tool.setAccessibleAt(Arrays.asList("http://fake.tapor.ca"));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Test simple software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("http://fake.tapor.ca")))
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("informationContributors[0].username", is("Contributor")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source", nullValue()));
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses[0].label", is("Apache License 2.0")))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }


    @Test
    public void shouldNotCreateToolWhenLabelIsNull() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setDescription("Lorem ipsum");

        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
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
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
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
        actor.setId(-99l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        tool.setContributors(contributors);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
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
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].type.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenConceptIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateToolWhenVocabularyIsDisallowed() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
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
    public void shouldNotCreateToolWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldUpdateToolAsDraft() throws Exception {
        String toolPersistentId = "DstBL5";

        ToolCore tool = new ToolCore();
        tool.setLabel("Draft Stata");
        tool.setDescription("Draft Stata is the solution for your data science needs. Obtain and manipulate data. Explore. Visualize. Model. Make inferences. Collect your results into reproducible reports.");
        List<PropertyCore> properties = new ArrayList<>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}?draft=true", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Draft Stata")))
                .andExpect(jsonPath("description", is("Draft Stata is the solution for your data science needs. Obtain and manipulate data. Explore. Visualize. Model. Make inferences. Collect your results into reproducible reports.")))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldUpdateToolWithoutSource() throws Exception {
        String toolPersistentId = "Xgufde";
        Integer toolCurrentId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");
        tool.setAccessibleAt(Arrays.asList("http://example.com"));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Test simple software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("http://example.com")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source", nullValue()))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(toolCurrentId)))
                .andExpect(jsonPath("olderVersions[0].label", is("WebSty")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldUpdateToolWithRelations() throws Exception {
        String toolPersistentId = "Xgufde";
        Integer toolCurrentId = 3;

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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("licenses", hasSize(1)))
                .andExpect(jsonPath("licenses[0].label", is("MIT License")))
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(toolCurrentId)))
                .andExpect(jsonPath("olderVersions[0].label", is("WebSty")))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldNotUpdateToolWhenNotExist() throws Exception {
        String toolPersistentId = "xxxxxx7";

        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateTool() throws Exception {
        String toolPersistentId = "n21Kfc";
        Integer toolCurrentId = 1;

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi");
        tool.setDescription("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.");
        tool.setAccessibleAt(Arrays.asList("https://gephi.org/"));
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
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("activity");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("7");
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

        PropertyCore property4 = new PropertyCore();
        PropertyTypeId propertyType4 = new PropertyTypeId();
        propertyType4.setCode("repository-url");
        property4.setType(propertyType4);
        property4.setValue("https://github.com/gephi/gephi");

        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        properties.add(property3);
        properties.add(property4);

        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("description", is("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.")))
                .andExpect(jsonPath("olderVersions", hasSize(1)))
                .andExpect(jsonPath("olderVersions[0].id", is(toolCurrentId)))
                .andExpect(jsonPath("olderVersions[0].label", is("Gephi")))
                .andExpect(jsonPath("newerVersions", hasSize(0)))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("licenses", hasSize(2)))
                .andExpect(jsonPath("contributors", hasSize(2)))
                .andExpect(jsonPath("properties", hasSize(4)));
    }

    @Test
    public void shouldNotUpdateToolWhenLabelIsNull() throws Exception {
        String toolPersistentId = "Xgufde";

        ToolCore tool = new ToolCore();
        tool.setDescription("Lorem ipsum");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("label")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenLicenseIsUnknown() throws Exception {
        String toolPersistentId = "Xgufde";

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        tool.setLicenses(licenses);
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("licenses[0].code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenContributorIsUnknown() throws Exception {
        String toolPersistentId = "Xgufde";

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
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].actor.id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenContributorRoleIsIncorrect() throws Exception {
        String toolPersistentId = "Xgufde";

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
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenPropertyTypeIsUnknown() throws Exception {
        String toolPersistentId = "Xgufde";

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].type.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenConceptIsIncorrect() throws Exception {
        String toolPersistentId = "Xgufde";

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.code")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenVocabularyIsDisallowed() throws Exception {
        String toolPersistentId = "Xgufde";

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept.vocabulary")))
                .andExpect(jsonPath("errors[0].code", is("field.disallowedVocabulary")))
                .andExpect(jsonPath("errors[0].args[0]", is("iso-639-3")))
                .andExpect(jsonPath("errors[0].args[1]", is("activity")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotUpdateToolWhenValueIsGivenForMandatoryVocabulary() throws Exception {
        String toolPersistentId = "Xgufde";

        ToolCore tool = new ToolCore();
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
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
        tool.setProperties(properties);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[0].concept")))
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

        String jsonResponse = mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String toolPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ToolDto.class).getPersistentId();

        mvc.perform(delete("/api/tools-services/{id}", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/tools-services/{id}", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotDeleteToolWhenNotExist() throws Exception {
        Integer toolId = 100;

        mvc.perform(delete("/api/tools-services/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateToolWithMultipleAccessibleAtUrlsWithoutSource() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");
        tool.setAccessibleAt(
                Arrays.asList(
                        "http://fake.tapor.ca",
                        "http://fake.tapor.com",
                        "http://fake.tapor.org"
                )
        );

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Test simple software")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("accessibleAt", hasSize(3)))
                .andExpect(jsonPath("accessibleAt[0]", is("http://fake.tapor.ca")))
                .andExpect(jsonPath("accessibleAt[1]", is("http://fake.tapor.com")))
                .andExpect(jsonPath("accessibleAt[2]", is("http://fake.tapor.org")))
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("informationContributors[0].username", is("Contributor")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source", nullValue()));
    }
}
