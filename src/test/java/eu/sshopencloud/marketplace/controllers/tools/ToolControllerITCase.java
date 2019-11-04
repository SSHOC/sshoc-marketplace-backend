package eu.sshopencloud.marketplace.controllers.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.licenses.LicenseId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
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
                .andExpect(jsonPath("licenses[0].label", is("Common Development and Distribution License 1.0")));;
    }

    @Test
    public void shouldntReturnToolWhenNotExist() throws Exception {
        Integer toolId = 51;

        mvc.perform(get("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateToolWithoutRelation() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("software");
        tool.setToolType(toolType);
        tool.setLabel("Test simple software");
        tool.setDescription("Lorem ipsum");

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test simple software")));
    }

    @Test
    public void shouldCreateToolWithRelations() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("software");
        tool.setToolType(toolType);
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
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        tool.setProperties(properties);

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test complex software")))
                .andExpect(jsonPath("licenses[0].label", is("Apache License 2.0")))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")));
    }



    @Test
    public void shouldntCreateToolWhenTypeIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("xxx");
        tool.setToolType(toolType);
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateToolWhenLabelIsNull() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("service");
        tool.setToolType(toolType);
        tool.setDescription("Lorem ipsum");

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateToolWhenLicenseIsUnknown() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("service");
        tool.setToolType(toolType);
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");
        LicenseId license = new LicenseId();
        license.setCode("qwerty1");
        List<LicenseId> licenses = new ArrayList<LicenseId>();
        licenses.add(license);
        tool.setLicenses(licenses);

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateToolWhenContributorIsUnknown() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("service");
        tool.setToolType(toolType);
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

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateToolWhenContributorRoleIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("service");
        tool.setToolType(toolType);
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

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

}
