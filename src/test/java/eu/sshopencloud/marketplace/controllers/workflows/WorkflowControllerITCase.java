package eu.sshopencloud.marketplace.controllers.workflows;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
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
public class WorkflowControllerITCase {

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
    public void shouldReturnWorkflows() throws Exception {

        mvc.perform(get("/api/workflows")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnWorkflow() throws Exception {
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Creation of a corpora")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].label", is("Corpus composition")))
                .andExpect(jsonPath("composedOf[1].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[1].label", is("Linguistic annotation")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[2].label", is("Selection of a license")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Write a dictionary")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));
    }


    @Test
    public void shouldCreateSimpleWorkflow() throws Exception {
        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Test simple workflow");
        workflow.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(0)));
    }

    @Test
    public void shouldCreateWorkflowWithSourceAndImplicitSourceAndSourceItemId() throws Exception {
        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Test workflow with source");
        workflow.setDescription("Lorem ipsum");
        workflow.setAccessibleAt(Arrays.asList("https://programminghistorian.org/en/lessons/test-workflow"));
        SourceId source = new SourceId();
        source.setId(1l);
        workflow.setSource(source);
        workflow.setSourceItemId("https://programminghistorian.org/en/lessons/test-workflow");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test workflow with source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(0)))
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is("https://programminghistorian.org/en/lessons/test-workflow")));
    }

    @Test
    public void shouldCreateSimpleWorkflowWithSteps() throws Exception {
        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Test simple workflow with steps");
        workflow.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/workflows")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long workflowId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getId();

        StepCore step1 = new StepCore();
        step1.setLabel("Test simple step 1");
        step1.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}", workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((int)workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow with steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Test simple step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));
    }


    @Test
    public void shouldCreateSimpleWorkflowWithStepsInGivenOrder() throws Exception {
        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Test simple workflow with steps");
        workflow.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/workflows")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long workflowId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getId();

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");
        step2.setStepNo(1);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step1 = new StepCore();
        step1.setLabel("Test simple step 1");
        step1.setDescription("Lorem ipsum");
        step1.setStepNo(1);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}", workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((int)workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow with steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Test simple step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));
    }

    @Test
    public void shouldCreateComplexWorkflowWithNestedSteps() throws Exception {
        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Test complex workflow with nested steps");
        workflow.setDescription("Lorem ipsum");
        ItemContributorId contributor1 = new ItemContributorId();
        ActorId actor1 = new ActorId();
        actor1.setId(1l);
        contributor1.setActor(actor1);
        ActorRoleId role1 = new ActorRoleId();
        role1.setCode("author");
        contributor1.setRole(role1);
        ItemContributorId contributor2 = new ItemContributorId();
        ActorId actor2 = new ActorId();
        actor2.setId(2l);
        contributor2.setActor(actor2);
        ActorRoleId role2 = new ActorRoleId();
        role2.setCode("author");
        contributor2.setRole(role2);
        ItemContributorId contributor3 = new ItemContributorId();
        ActorId actor3 = new ActorId();
        actor3.setId(3l);
        contributor3.setActor(actor3);
        ActorRoleId role3 = new ActorRoleId();
        role3.setCode("founder");
        contributor3.setRole(role3);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        contributors.add(contributor3);
        workflow.setContributors(contributors);


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/workflows")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(2)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("contributors[2].actor.id", is(3)))
                .andExpect(jsonPath("contributors[2].role.code", is("founder")))
                .andReturn().getResponse().getContentAsString();

        long workflowId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getId();

        StepCore step1 = new StepCore();
        step1.setLabel("Test complex step 1");
        step1.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        jsonResponse = mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long stepId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, StepDto.class).getId();

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");
        step2.setStepNo(2);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step11 = new StepCore();
        step11.setLabel("Test simple step 1.1");
        step11.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step11);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowId, stepId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step13 = new StepCore();
        step13.setLabel("Test simple step 1.3");
        step13.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step13);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowId, stepId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.3")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step12 = new StepCore();
        step12.setLabel("Test simple step 1.2");
        step12.setDescription("Lorem ipsum");
        step12.setStepNo(2);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step12);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowId, stepId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}/steps/{stepId}", workflowId, stepId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((int)stepId)))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test complex step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Test simple step 1.1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 1.2")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Test simple step 1.3")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));


        mvc.perform(get("/api/workflows/{workflowId}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((int)workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test complex workflow with nested steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Test complex step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is("Test simple step 1.1")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[1].label", is("Test simple step 1.2")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[2].label", is("Test simple step 1.3")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));
    }

    @Test
    public void shouldAddStepToWorkflow() throws Exception {
        Integer workflowId = 21;

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));


        StepCore step = new StepCore();
        step.setLabel("The last step in a workflow");
        step.setDescription("Lorem ipsum");
        ItemContributorId contributor1 = new ItemContributorId();
        ActorId actor1 = new ActorId();
        actor1.setId(1l);
        contributor1.setActor(actor1);
        ActorRoleId role1 = new ActorRoleId();
        role1.setCode("author");
        contributor1.setRole(role1);
        ItemContributorId contributor2 = new ItemContributorId();
        ActorId actor2 = new ActorId();
        actor2.setId(2l);
        contributor2.setActor(actor2);
        ActorRoleId role2 = new ActorRoleId();
        role2.setCode("author");
        contributor2.setRole(role2);
        ItemContributorId contributor3 = new ItemContributorId();
        ActorId actor3 = new ActorId();
        actor3.setId(3l);
        contributor3.setActor(actor3);
        ActorRoleId role3 = new ActorRoleId();
        role3.setCode("author");
        contributor3.setRole(role3);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        contributors.add(contributor3);
        step.setContributors(contributors);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("The last step in a workflow")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(2)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("contributors[2].actor.id", is(3)))
                .andExpect(jsonPath("contributors[2].role.code", is("author")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("The last step in a workflow")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));

    }

    @Test
    public void shouldAddSubstepToWorkflow() throws Exception {
        Integer workflowId = 21;

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        Integer stepId = 22;

        StepCore step = new StepCore();
        step.setLabel("The substep of a step in a workflow");
        step.setDescription("Lorem ipsum");
        ItemContributorId contributor1 = new ItemContributorId();
        ActorId actor1 = new ActorId();
        actor1.setId(2l);
        contributor1.setActor(actor1);
        ActorRoleId role1 = new ActorRoleId();
        role1.setCode("provider");
        contributor1.setRole(role1);
        ItemContributorId contributor2 = new ItemContributorId();
        ActorId actor2 = new ActorId();
        actor2.setId(3l);
        contributor2.setActor(actor2);
        ActorRoleId role2 = new ActorRoleId();
        role2.setCode("author");
        contributor2.setRole(role2);
        ItemContributorId contributor3 = new ItemContributorId();
        ActorId actor3 = new ActorId();
        actor3.setId(1l);
        contributor3.setActor(actor3);
        ActorRoleId role3 = new ActorRoleId();
        role3.setCode("contributor");
        contributor3.setRole(role3);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        contributors.add(contributor3);
        step.setContributors(contributors);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowId, stepId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("The substep of a step in a workflow")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors[0].actor.id", is(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("provider")))
                .andExpect(jsonPath("contributors[1].actor.id", is(3)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("contributors[2].actor.id", is(1)))
                .andExpect(jsonPath("contributors[2].role.code", is("contributor")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(1)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is("The substep of a step in a workflow")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].description", is("Lorem ipsum")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].contributors[0].actor.id", is(2)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].contributors[0].role.code", is("provider")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].contributors[1].actor.id", is(3)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].contributors[1].role.code", is("author")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].contributors[2].actor.id", is(1)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].contributors[2].role.code", is("contributor")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));
    }

    @Test
    public void shouldNotAddStepToWorkflowWhenActorHasManyRoles() throws Exception {
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")));

        StepCore step = new StepCore();
        step.setLabel("A step with a triple actor");
        step.setDescription("Lorem ipsum");
        ItemContributorId contributor1 = new ItemContributorId();
        ActorId actor1 = new ActorId();
        actor1.setId(1l);
        contributor1.setActor(actor1);
        ActorRoleId role1 = new ActorRoleId();
        role1.setCode("provider");
        contributor1.setRole(role1);
        ItemContributorId contributor2 = new ItemContributorId();
        ActorId actor2 = new ActorId();
        actor2.setId(1l);
        contributor2.setActor(actor2);
        ActorRoleId role2 = new ActorRoleId();
        role2.setCode("author");
        contributor2.setRole(role2);
        ItemContributorId contributor3 = new ItemContributorId();
        ActorId actor3 = new ActorId();
        actor3.setId(1l);
        contributor3.setActor(actor3);
        ActorRoleId role3 = new ActorRoleId();
        role3.setCode("contributor");
        contributor3.setRole(role3);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        contributors.add(contributor3);
        step.setContributors(contributors);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(2)))
                .andExpect(jsonPath("errors[0].field", is("contributors[1].actor.id")))
                .andExpect(jsonPath("errors[0].code", is("field.repeated")))
                .andExpect(jsonPath("errors[0].message", notNullValue()))
                .andExpect(jsonPath("errors[1].field", is("contributors[2].actor.id")))
                .andExpect(jsonPath("errors[1].code", is("field.repeated")))
                .andExpect(jsonPath("errors[1].message", notNullValue()));
    }

    @Test
    public void shouldNotAddStepToWorkflowWhenStepNoIsIncorrect() throws Exception {
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")));

        StepCore step = new StepCore();
        step.setLabel("A step with wrong stepNo");
        step.setDescription("Lorem ipsum");
        step.setStepNo(-1);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("stepNo")))
                .andExpect(jsonPath("errors[0].code", is("field.incorrect")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldUpdateStep() throws Exception {
        Integer workflowId = 12;
        Integer stepId = 14;

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(stepId)))
                .andExpect(jsonPath("composedOf[1].label", is("Creation of a corpora")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].label", is("Corpus composition")))
                .andExpect(jsonPath("composedOf[1].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[1].label", is("Linguistic annotation")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[2].label", is("Selection of a license")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Write a dictionary")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));

        StepCore step = new StepCore();
        step.setLabel("Creation of a corpora");
        step.setDescription("...");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(4l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        step.setContributors(contributors);
        PropertyCore property0 = new PropertyCore();
        PropertyTypeId propertyType0 = new PropertyTypeId();
        propertyType0.setCode("object-type");
        property0.setType(propertyType0);
        ConceptId concept0 = new ConceptId();
        concept0.setCode("step");
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
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property0);
        properties.add(property1);
        step.setProperties(properties);
        step.setStepNo(1);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/workflows/{workflowId}/steps/{stepId}", workflowId, stepId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Creation of a corpora")))
                .andExpect(jsonPath("description", is("...")))
                .andExpect(jsonPath("contributors[0].actor.id", is(4)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("Step")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Corpus composition")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Linguistic annotation")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Selection of a license")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(2)))
                .andExpect(jsonPath("properties[0].concept.label", is("Workflow")))
                .andExpect(jsonPath("properties[1].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].id", is(stepId)))
                .andExpect(jsonPath("composedOf[0].label", is("Creation of a corpora")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is("Corpus composition")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[1].label", is("Linguistic annotation")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[2].label", is("Selection of a license")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[0].composedOf[3].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Write a dictionary")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));
    }

    @Test
    public void shouldDeleteWorkflow() throws Exception {
        Integer workflowId = 21;

        mvc.perform(delete("/api/workflows/{id}", workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }

}
