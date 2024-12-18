package eu.sshopencloud.marketplace.controllers.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
public class ToolControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;

    @BeforeEach
    public void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
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
                .andExpect(jsonPath("informationContributor.id", is(2)));
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
                .andExpect(jsonPath("informationContributor.username", is("Contributor")))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldCreateToolWithRelations() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test complex software");
        tool.setDescription("Lorem ipsum");
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
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")));
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
                .andExpect(jsonPath("description", is("Draft Stata is the solution for your data science needs. Obtain and manipulate data. Explore. Visualize. Model. Make inferences. Collect your results into reproducible reports.")));


        mvc.perform(get("/api/tools-services/{id}/history?draft=true", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("tool-or-service")))
                .andExpect(jsonPath("$[0].label", is("Draft Stata")))
                .andExpect(jsonPath("$[0].persistentId", is( toolPersistentId)))
                .andExpect(jsonPath("$[0].status", is("draft")))

                .andExpect(jsonPath("$[1].category", is("tool-or-service")))
                .andExpect(jsonPath("$[1].persistentId", is( toolPersistentId)))
                .andExpect(jsonPath("$[1].status", is("approved")));

        mvc.perform(get("/api/tools-services/{id}/history", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("tool-or-service")))
                .andExpect(jsonPath("$[0].label", not(is("Draft Stata"))))
                .andExpect(jsonPath("$[0].persistentId", is( toolPersistentId)))
                .andExpect(jsonPath("$[0].status", is("approved")));


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
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldUpdateToolWithRelations() throws Exception {
        String toolPersistentId = "Xgufde";
        Integer toolCurrentId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test complex software");
        tool.setDescription("Lorem ipsum");
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
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[1].value", is("paper")));
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
        int toolCurrentId = 1;

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi");
        tool.setDescription("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.");
        tool.setAccessibleAt(Arrays.asList("https://gephi.org/"));

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(5L), new ActorRoleId("author"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(4L), new ActorRoleId("funder"));
        tool.setContributors(List.of(contributor1, contributor2));

        PropertyCore property1 = new PropertyCore(
                new PropertyTypeId("activity"), new ConceptId("7", new VocabularyId("tadirah-activity"), null)
        );
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("keyword"), "graph");
        PropertyCore property3 = new PropertyCore(new PropertyTypeId("keyword"), "social network analysis");
        PropertyCore property4 = new PropertyCore(new PropertyTypeId("repository-url"), "https://github.com/gephi/gephi");
        tool.setProperties(List.of(property1, property2, property3, property4));

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
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("contributors", hasSize(2)))
                .andExpect(jsonPath("properties", hasSize(4)));
    }

    @Test
    public void shouldUpdateToolAddAndRemoveExternalId() throws Exception {
        String toolPersistentId = "n21Kfc";
        int toolCurrentId = 1;

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi");
        tool.setDescription("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.");
        tool.setAccessibleAt(Arrays.asList("https://gephi.org/"));

        ItemExternalIdCore id = new ItemExternalIdCore();
        id.setIdentifier("myId");
        ItemExternalIdId idid = new ItemExternalIdId();
        idid.setCode("GitHub");
        id.setIdentifierService(idid);

        tool.setExternalIds(Arrays.asList(id));

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
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("externalIds[0].identifier", is("myId")));

        tool.setExternalIds(List.of());
        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);

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
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("externalIds", hasSize(0)));
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

        PropertyCore property1 = new PropertyCore(
                new PropertyTypeId("language"), new ConceptId("zzz", new VocabularyId("iso-639-3"), null)
        );
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("material"), "paper");
        tool.setProperties(List.of(property1, property2));

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

        PropertyCore property1 = new PropertyCore(new PropertyTypeId("language"), "Polish");
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("material"), "paper");
        tool.setProperties(List.of(property1, property2));

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
        Long toolVersionId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ToolDto.class).getId();

        mvc.perform(delete("/api/tools-services/{persistentId}/versions/{id}", toolPersistentId, toolVersionId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/tools-services/{persistentId}", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/tools-services/{persistentId}/versions/{id}", toolPersistentId, toolVersionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("id", is(toolVersionId.intValue())))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to delete")))
                .andExpect(jsonPath("status", is("approved")));
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
                .andExpect(jsonPath("informationContributor.username", is("Contributor")))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldUpdateToolWithPropertyValuesValidation() throws Exception {
        PropertyTypeCore propertyType = PropertyTypeCore.builder()
                .code("rating")
                .label("Rating")
                .type(PropertyTypeClass.FLOAT)
                .allowedVocabularies(null)
                .build();
        String propertyTypePayload = mapper.writeValueAsString(propertyType);

        mvc.perform(
                post("/api/property-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyTypePayload)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());

        String toolPersistentId = "n21Kfc";
        int toolCurrentId = 1;

        ToolCore tool = new ToolCore();
        tool.setLabel("Gephi");
        tool.setDescription("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.");
        tool.setAccessibleAt(List.of("https://gephi.org/"));

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(5L), new ActorRoleId("author"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(4L), new ActorRoleId("funder"));
        tool.setContributors(List.of(contributor1, contributor2));

        PropertyCore property1 = new PropertyCore(
                new PropertyTypeId("activity"), new ConceptId("7", new VocabularyId("tadirah-activity"), null)
        );
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("keyword"), "graph");
        PropertyCore property3 = new PropertyCore(new PropertyTypeId("year"), "2020");
        PropertyCore property4 = new PropertyCore(new PropertyTypeId("timestamp"), "2020-12-31");
        PropertyCore property5 = new PropertyCore(new PropertyTypeId("repository-url"), "https://github.com/gephi/gephi");
        PropertyCore property6 = new PropertyCore(new PropertyTypeId("rating"), "4.5");
        tool.setProperties(List.of(property1, property2, property3, property4, property5, property6));

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                put("/api/tools-services/{id}", toolPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("description", is("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("contributors", hasSize(2)))
                .andExpect(jsonPath("properties", hasSize(6)));
    }

    @Test
    public void shouldNotCreateToolWithInvalidFloatProperty() throws Exception {
        PropertyTypeCore propertyType = PropertyTypeCore.builder()
                .code("rating")
                .label("Rating")
                .type(PropertyTypeClass.FLOAT)
                .allowedVocabularies(null)
                .build();
        String propertyTypePayload = mapper.writeValueAsString(propertyType);

        mvc.perform(
                post("/api/property-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyTypePayload)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());

        ToolCore tool = new ToolCore();
        tool.setLabel("Test simple software v2");
        tool.setDescription("Lorem ipsum v2");

        PropertyCore property1 = new PropertyCore(new PropertyTypeId("rating"), "5.125");
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("rating"), "2/10");
        tool.setProperties(List.of(property1, property2));

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                post("/api/tools-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("properties[1].value")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldRetrieveSuggestedTool() throws Exception {
        String toolId = "n21Kfc";
        int toolVersionId = 1;

        ToolCore tool = new ToolCore();
        tool.setLabel("Suggested tool or service");
        tool.setDescription("This is a suggested tool or service");

        String payload = mapper.writeValueAsString(tool);

        mvc.perform(
                put("/api/tools-services/{id}", toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", not(is(toolVersionId))))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("suggested")));

        mvc.perform(
                get("/api/tools-services/{id}", toolId)
                        .param("approved", "false")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", not(is(toolVersionId))))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("suggested")));

        mvc.perform(
                get("/api/tools-services/{id}", toolId)
                        .param("approved", "false")
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(toolVersionId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")));

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(toolVersionId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")));
    }

    @Test
    public void shouldCreateToolWithExternalId() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tesseract");
        tool.setDescription("The best tool for Optical Character Recognition");
        tool.setExternalIds(List.of(
                new ItemExternalIdCore(new ItemExternalIdId("GitHub"), "https://github.com/tesseract-ocr/tesseract")
        ));

        String payload = mapper.writeValueAsString(tool);

        String toolJson = mvc.perform(
                post("/api/tools-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andExpect(jsonPath("externalIds", hasSize(1)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("GitHub")))
                .andExpect(jsonPath("externalIds[0].identifier", is("https://github.com/tesseract-ocr/tesseract")))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();
        int toolVersionId = toolDto.getId().intValue();

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(toolVersionId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andExpect(jsonPath("externalIds", hasSize(1)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("GitHub")))
                .andExpect(jsonPath("externalIds[0].identifier", is("https://github.com/tesseract-ocr/tesseract")));
    }

    @Test
    public void shouldCreateToolWithAccessibleAtUrlSameAsExistingSource() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tapor related tool");
        tool.setDescription("The tool that has tapor url in the accessible at property");
        tool.setAccessibleAt(List.of("http://tapor.ca/fakeId"));

        String payload = mapper.writeValueAsString(tool);

        String toolJson = mvc.perform(
                        post("/api/tools-services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();
        int toolVersionId = toolDto.getId().intValue();

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andExpect(jsonPath("accessibleAt", hasSize(1)));
    }

    @Test
    public void shouldCreateMultipleToolDrafts() throws Exception {
        String toolId = "DstBL5";

        ToolCore firstDraft = new ToolCore();
        firstDraft.setLabel("Stata v1.1");
        firstDraft.setDescription("Stata with many bugfixes");

        ToolCore secondDraft = new ToolCore();
        secondDraft.setLabel("Stata v1.2");
        secondDraft.setDescription("Stata with new features");

        String firstPayload = mapper.writeValueAsString(firstDraft);
        String secondPayload = mapper.writeValueAsString(secondDraft);

        String firstResponse = mvc.perform(
                put("/api/tools-services/{id}", toolId)
                        .param("draft", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstPayload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("status", is("draft")))
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mvc.perform(
                put("/api/tools-services/{id}", toolId)
                        .param("draft", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondPayload)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("status", is("draft")))
                .andReturn().getResponse().getContentAsString();

        ToolDto firstDto = mapper.readValue(firstResponse, ToolDto.class);
        ToolDto secondDto = mapper.readValue(secondResponse, ToolDto.class);
        int firstVersionId = firstDto.getId().intValue();
        int secondVersionId = secondDto.getId().intValue();

        mvc.perform(
                get("/api/tools-services/{id}", toolId)
                        .param("draft", "true")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(firstVersionId)))
                .andExpect(jsonPath("status", is("draft")));

        mvc.perform(
                get("/api/tools-services/{id}", toolId)
                        .param("draft", "true")
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("id", is(secondVersionId)))
                .andExpect(jsonPath("status", is("draft")));
    }

    @Test
    public void shouldUpdateToolWhenDraftIsPresent() throws Exception {
        String toolId = "Xgufde";

        ToolCore draftTool = new ToolCore();
        draftTool.setLabel("WebSty v2");
        draftTool.setDescription("WebSty version 2. draft");
        draftTool.setRelatedItems(
                List.of(new RelatedItemCore("n21Kfc", new ItemRelationId("relates-to")))
        );

        String draftPayload = mapper.writeValueAsString(draftTool);
        mvc.perform(
                put("/api/tools-services/{toolId}", toolId)
                        .param("draft", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftPayload)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk());

        ToolCore tool = new ToolCore();
        tool.setLabel("WebSty v1.1");
        tool.setDescription("WebSty legacy support");
        tool.setRelatedItems(
                List.of(new RelatedItemCore("n21Kfc", new ItemRelationId("relates-to")))
        );

        String payload = mapper.writeValueAsString(tool);
        mvc.perform(
                put("/api/tools-services/{toolId}", toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnToolInformationContributors() throws Exception {

        String toolPersistentId = "n21Kfc";

        mvc.perform(get("/api/tools-services/{id}/information-contributors", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].username", is("Moderator")))
                .andExpect(jsonPath("$[0].displayName", is("Moderator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
                .andExpect(jsonPath("$[0].role", is("moderator")))
                .andExpect(jsonPath("$[0].email", is("moderator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));
    }

    @Test
    public void shouldReturnToolInformationContributorsForVersion() throws Exception {

        String toolPersistentId = "n21Kfc";

        ToolCore draftTool = new ToolCore();
        draftTool.setLabel("WebSty v2");
        draftTool.setDescription("WebSty version 2. draft");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(draftTool);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(put("/api/tools-services/{id}", toolPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        Long versionId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, DatasetDto.class).getId();

        log.debug("datasetId: " + versionId);

        mvc.perform(get("/api/tools-services/{id}/versions/{versionId}/information-contributors", toolPersistentId, versionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[0].displayName", is("Administrator")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("Moderator")))
                .andExpect(jsonPath("$[1].displayName", is("Moderator")))
                .andExpect(jsonPath("$[1].status", is("enabled")))
                .andExpect(jsonPath("$[1].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
                .andExpect(jsonPath("$[1].role", is("moderator")))
                .andExpect(jsonPath("$[1].email", is("moderator@example.com")))
                .andExpect(jsonPath("$[1].config", is(true)));
    }


    @Test
    public void shouldGetMergeForTool() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        String response = mvc.perform(
                get("/api/tools-services/{id}/merge", toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with",  datasetId, workflowId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi / Consortium of European Social Science Data Archives / Creation of a dictionary")))
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    public void shouldMergeIntoTool() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";


        mvc.perform(
                get("/api/tools-services/{id}", toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("informationContributor.username", is("Moderator")))
                .andExpect(jsonPath("description", is("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks.")))
                .andExpect(jsonPath("contributors[0].actor.name", is("John Smith")))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("properties[0].type.code", is("activity")))
                .andExpect(jsonPath("properties[0].concept.code", is("7")))
                .andExpect(jsonPath("properties[0].concept.label", is("Capture")))
                .andExpect(jsonPath("properties[0].concept.uri", is("https://sshoc.poolparty.biz/Vocabularies/tadirah-activities/7")))
                .andExpect(jsonPath("properties[1].type.code", is("keyword")))
                .andExpect(jsonPath("properties[1].value", is("graph")))
                .andExpect(jsonPath("properties[2].type.code", is("keyword")))
                .andExpect(jsonPath("properties[2].value", is("social network analysis")))
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].id", is(3)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].id", is(4)))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")));

        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Consortium of European Social Science Data Archives")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("description", is("The CESSDA Data Catalogue contains the metadata of all data in the holdings of CESSDA'a service providers. It is a one-stop-shop for search and discovery, enabling effective access to European social science research data.")))
                .andExpect(jsonPath("contributors[0].actor.name", is("CESSDA")))
                .andExpect(jsonPath("contributors[0].role.code", is("provider")))
                .andExpect(jsonPath("properties[0].type.code", is("activity")))
                .andExpect(jsonPath("properties[0].concept.code", is("ActivityType-Seeking")))
                .andExpect(jsonPath("properties[0].concept.label", is("Seeking")))
                .andExpect(jsonPath("properties[0].concept.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Seeking")))
                .andExpect(jsonPath("accessibleAt[0]", is("https://datacatalogue.cessda.eu/")))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("relatedItems[0].id", is(9)))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("mentions")));

        mvc.perform(
                get("/api/workflows/{id}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("informationContributor.username", is("Contributor")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("contributors[0].actor.name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.name", is("CESSDA")))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("properties[0].type.code", is("language")))
                .andExpect(jsonPath("properties[0].concept.code", is("eng")))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("properties[0].concept.uri", is("http://iso639-3.sil.org/code/eng")));


        String response = mvc.perform(
                get("/api/tools-services/{id}/merge", toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with",  datasetId, workflowId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi / Consortium of European Social Science Data Archives / Creation of a dictionary")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                post("/api/tools-services/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", toolId, datasetId, workflowId)
                        .content(response)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi / Consortium of European Social Science Data Archives / Creation of a dictionary")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("informationContributor.username", is("Moderator")))
                .andExpect(jsonPath("description", is("**Gephi** is the leading visualization and exploration software for all kinds of graphs and networks. / The CESSDA Data Catalogue contains the metadata of all data in the holdings of CESSDA'a service providers. It is a one-stop-shop for search and discovery, enabling effective access to European social science research data. / Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("contributors[0].actor.name", is("John Smith")))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.name", is("CESSDA")))
                .andExpect(jsonPath("contributors[1].role.code", is("provider")))
                .andExpect(jsonPath("contributors[2].actor.name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("contributors[2].role.code", is("author")))
                .andExpect(jsonPath("contributors[3].actor.name", is("CESSDA")))
                .andExpect(jsonPath("contributors[3].role.code", is("author")))
                .andExpect(jsonPath("properties[0].type.code", is("activity")))
                .andExpect(jsonPath("properties[0].concept.code", is("7")))
                .andExpect(jsonPath("properties[0].concept.label", is("Capture")))
                .andExpect(jsonPath("properties[0].concept.uri", is("https://sshoc.poolparty.biz/Vocabularies/tadirah-activities/7")))
                .andExpect(jsonPath("properties[1].type.code", is("keyword")))
                .andExpect(jsonPath("properties[1].value", is("graph")))
                .andExpect(jsonPath("properties[2].type.code", is("keyword")))
                .andExpect(jsonPath("properties[2].value", is("social network analysis")))
                .andExpect(jsonPath("properties[3].type.code", is("activity")))
                .andExpect(jsonPath("properties[3].concept.code", is("ActivityType-Seeking")))
                .andExpect(jsonPath("properties[3].concept.label", is("Seeking")))
                .andExpect(jsonPath("properties[3].concept.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Seeking")))
                .andExpect(jsonPath("properties[4].type.code", is("language")))
                .andExpect(jsonPath("properties[4].concept.code", is("eng")))
                .andExpect(jsonPath("properties[4].concept.label", is("eng")))
                .andExpect(jsonPath("properties[4].concept.uri", is("http://iso639-3.sil.org/code/eng")))
                .andExpect(jsonPath("accessibleAt[0]", is("https://gephi.org/")))
                .andExpect(jsonPath("accessibleAt[1]", is("https://datacatalogue.cessda.eu/")))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("Xgufde")))
                .andExpect(jsonPath("relatedItems[0].id", not(is(3))))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("relatedItems[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("relatedItems[1].id", not(is(4))))
                .andExpect(jsonPath("relatedItems[1].relation.code", is("is-documented-by")))
                .andExpect(jsonPath("relatedItems[2].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("relatedItems[2].id", not(is(9))))
                .andExpect(jsonPath("relatedItems[2].relation.code", is("mentions")));

        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldGetHistoryForMergedTool() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        String response = mvc.perform(
                get("/api/tools-services/{id}/merge", toolId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with",  datasetId, workflowId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi / Consortium of European Social Science Data Archives / Creation of a dictionary")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                post("/api/tools-services/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", toolId, datasetId, workflowId)
                        .content(response)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(toolId)))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Gephi / Consortium of European Social Science Data Archives / Creation of a dictionary")))
                .andReturn().getResponse().getContentAsString();


        mvc.perform(
                get("/api/datasets/{id}", datasetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isNotFound());

        String mergedPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, ToolDto.class).getPersistentId();

        String mergedLabel = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, ToolDto.class).getLabel();


        mvc.perform(
                get("/api/tools-services/{id}/history", mergedPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].category", is("tool-or-service")))
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
    public void shouldUpdateToolWithoutLineBreakInLabel() throws Exception {
        String toolPersistentId = "Xgufde";
        Integer toolCurrentId = 3;

        ToolCore tool = new ToolCore();
        tool.setLabel("Test \n\rsimple \nsoftware\r");
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
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andExpect(jsonPath("properties", hasSize(0)));

        mvc.perform(get("/api/tools-services/{id}/history", toolPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("tool-or-service")))
                .andExpect(jsonPath("$[0].label", is("Test simple software")))
                .andExpect(jsonPath("$[0].persistentId", is( toolPersistentId)));
    }

    @Test
    public void shouldCreateToolWithoutLineBreaksInLabel() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Test \n\rsimple \nsoftware\r");
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
                .andExpect(jsonPath("informationContributor.username", is("Contributor")))
                .andExpect(jsonPath("properties", hasSize(0)));
    }

    @Test
    public void shouldDeleteAndRevertTool() throws Exception {

        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to revert");
        tool.setDescription("Lorem ipsum dolor");

        String toolPayload = mapper.writeValueAsString(tool);

        String toolJSON = mvc.perform(
                        post("/api/tools-services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toolPayload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJSON, ToolDto.class);

        mvc.perform(delete("/api/tools-services/{id}", toolDto.getPersistentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(
                        put("/api/tools-services/{id}/revert", toolDto.getPersistentId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        get("/api/tools-services/{id}", toolDto.getPersistentId())
                                .param("approved", "true")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", is(toolDto.getId().intValue())))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())));
    }

    @Test
    public void shouldPatchTool2() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Patched tool");
        tool.setDescription("This is a tool to be patched!");
        SourceId sourceId = new SourceId();
        sourceId.setId(1L);
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");

        String payload = mapper.writeValueAsString(tool);

        String toolJson = mvc.perform(
                        post("/api/tools-services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("This is a tool to be patched!")))
                .andExpect(jsonPath("accessibleAt", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();
        int toolVersionId = toolDto.getId().intValue();

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", is(toolDto.getId().intValue())))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())));

        tool.setAccessibleAt(List.of("http://tapor.ca/tools/patchedTool"));
        tool.setSourceItemId(null);
        tool.setSource(null);
        tool.setDescription("New description");
        String payloadUpdated = mapper.writeValueAsString(tool);
        String toolJsonUpdated = mvc.perform(
                        put("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadUpdated)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(toolDto.getId().intValue()))))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("sourceItemId").doesNotExist())
                .andExpect(jsonPath("source").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        tool.setDescription("This is a tool to be patched!");
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");
        tool.setAccessibleAt(null);

        String payloadPatch = mapper.writeValueAsString(tool);

        String toolJsonPatched = mvc.perform(
                        patch("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadPatch)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isNotModified())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void shouldPatchTool() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Patched tool");
        tool.setDescription("This is a tool to be patched!");
        SourceId sourceId = new SourceId();
        sourceId.setId(1L);
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");

        String payload = mapper.writeValueAsString(tool);

        String toolJson = mvc.perform(
                        post("/api/tools-services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("This is a tool to be patched!")))
                .andExpect(jsonPath("accessibleAt", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();
        int toolVersionId = toolDto.getId().intValue();

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", is(toolDto.getId().intValue())))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())));

        tool.setAccessibleAt(List.of("http://tapor.ca/tools/patchedTool"));
        tool.setSourceItemId(null);
        tool.setSource(null);
        String payloadUpdated = mapper.writeValueAsString(tool);
        String toolJsonUpdated = mvc.perform(
                        put("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadUpdated)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(toolDto.getId().intValue()))))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("This is a tool to be patched!")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("sourceItemId").doesNotExist())
                .andExpect(jsonPath("source").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        tool.setDescription("New description");
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");
        tool.setAccessibleAt(null);

        String payloadPatch = mapper.writeValueAsString(tool);

        String toolJsonPatched = mvc.perform(
                        patch("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadPatch)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("sourceItemId", is("patchedTool")))
                .andExpect(jsonPath("source").exists())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void shouldConflictInPatchTool() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Patched tool");
        tool.setDescription("This is a tool to be patched!");
        SourceId sourceId = new SourceId();
        sourceId.setId(1L);
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");

        String payload = mapper.writeValueAsString(tool);

        String toolJson = mvc.perform(
                        post("/api/tools-services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("This is a tool to be patched!")))
                .andExpect(jsonPath("accessibleAt", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();
        int toolVersionId = toolDto.getId().intValue();

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", is(toolDto.getId().intValue())))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())));

        tool.setAccessibleAt(List.of("http://tapor.ca/tools/patchedTool"));
        tool.setDescription("New description");
        tool.setSourceItemId(null);
        tool.setSource(null);
        String payloadUpdated = mapper.writeValueAsString(tool);
        String toolJsonUpdated = mvc.perform(
                        put("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadUpdated)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(toolDto.getId().intValue()))))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("sourceItemId").doesNotExist())
                .andExpect(jsonPath("source").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        tool.setDescription("New description even more");
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");
        tool.setAccessibleAt(null);

        String payloadPatch = mapper.writeValueAsString(tool);

        String toolJsonPatched = mvc.perform(
                        patch("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadPatch)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("New description")))
                .andExpect(jsonPath("properties[0].type.code", is("conflict-at-source")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("sourceItemId", is("patchedTool")))
                .andExpect(jsonPath("source").exists())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void shouldNotPatchButUpdateTool() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Patched tool");
        tool.setDescription("This is a tool to be patched!");
        SourceId sourceId = new SourceId();
        sourceId.setId(1L);
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");

        String payload = mapper.writeValueAsString(tool);

        String toolJson = mvc.perform(
                        post("/api/tools-services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("This is a tool to be patched!")))
                .andExpect(jsonPath("accessibleAt", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();
        int toolVersionId = toolDto.getId().intValue();

        mvc.perform(get("/api/tools-services/{id}", toolId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", is(toolDto.getId().intValue())))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is(tool.getDescription())));

        tool.setAccessibleAt(List.of("http://tapor.ca/tools/patchedTool"));
        tool.setSourceItemId(null);
        tool.setSource(null);
        String payloadUpdated = mapper.writeValueAsString(tool);
        String toolJsonUpdated = mvc.perform(
                        put("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadUpdated)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", not(is(toolDto.getId().intValue()))))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("This is a tool to be patched!")))
                .andExpect(jsonPath("accessibleAt", hasSize(1)))
                .andExpect(jsonPath("sourceItemId").doesNotExist())
                .andExpect(jsonPath("source").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        tool.setDescription("Patched tool description changed!");
        tool.setSource(sourceId);
        tool.setSourceItemId("patchedTool");
        tool.setAccessibleAt(null);

        String payloadPatch = mapper.writeValueAsString(tool);

        String toolJsonPatched = mvc.perform(
                        put("/api/tools-services/{id}", toolId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadPatch)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(toolDto.getPersistentId())))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(tool.getLabel())))
                .andExpect(jsonPath("description", is("Patched tool description changed!")))
                .andExpect(jsonPath("accessibleAt", hasSize(0)))
                .andExpect(jsonPath("sourceItemId", is("patchedTool")))
                .andExpect(jsonPath("source").exists())
                .andReturn().getResponse().getContentAsString();
    }
}
