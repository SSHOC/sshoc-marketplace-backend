package eu.sshopencloud.marketplace.controllers.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.dto.items.RelatedItemCore;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
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

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.sshopencloud.marketplace.util.MatcherUtils.equalValue;
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
public class WorkflowControllerITCase {

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
    public void shouldReturnWorkflows() throws Exception {

        mvc.perform(get("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnWorkflow() throws Exception {
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
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
                .andExpect(jsonPath("properties", hasSize(0)))
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
                .andExpect(jsonPath("properties", hasSize(0)))
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

        String workflowPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getPersistentId();

        StepCore step1 = new StepCore();
        step1.setLabel("Test simple step 1");
        step1.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow with steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
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

        String workflowPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getPersistentId();

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");
        step2.setStepNo(1);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step1 = new StepCore();
        step1.setLabel("Test simple step 1");
        step1.setDescription("Lorem ipsum");
        step1.setStepNo(1);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow with steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
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
        role2.setCode("contributor");
        contributor2.setRole(role2);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        workflow.setContributors(contributors);


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/workflows")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(2)))
                .andExpect(jsonPath("contributors[1].role.code", is("contributor")))
                .andReturn().getResponse().getContentAsString();

        String workflowPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getPersistentId();

        StepCore step1 = new StepCore();
        step1.setLabel("Test complex step 1");
        step1.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        jsonResponse = mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String stepPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, StepDto.class).getPersistentId();

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");
        step2.setStepNo(2);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step11 = new StepCore();
        step11.setLabel("Test simple step 1.1");
        step11.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step11);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowPersistentId, stepPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        StepCore step13 = new StepCore();
        step13.setLabel("Test simple step 1.3");
        step13.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step13);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowPersistentId, stepPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.3")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        StepCore step12 = new StepCore();
        step12.setLabel("Test simple step 1.2");
        step12.setDescription("Lorem ipsum");
        step12.setStepNo(2);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step12);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowPersistentId, stepPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}/steps/{stepId}", workflowPersistentId, stepPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test complex step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test complex workflow with nested steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
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
    public void shouldCreateComplexWorkflowAsDraftWithNestedSteps() throws Exception {
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
        role2.setCode("contributor");
        contributor2.setRole(role2);
        List<ItemContributorId> contributors = new ArrayList<>();
        contributors.add(contributor1);
        contributors.add(contributor2);
        workflow.setContributors(contributors);

        String payload = mapper.writeValueAsString(workflow);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(
                        post("/api/workflows?draft=1")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].actor.id", is(2)))
                .andExpect(jsonPath("contributors[1].role.code", is("contributor")))
                .andReturn().getResponse().getContentAsString();

        WorkflowDto createdWorkflow = mapper.readValue(jsonResponse, WorkflowDto.class);
        String workflowPersistentId = createdWorkflow.getPersistentId();
        long workflowVersionId = createdWorkflow.getId();

        StepCore step1 = new StepCore();
        step1.setLabel("Test complex step 1");
        step1.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        jsonResponse = mvc.perform(
                        post("/api/workflows/{workflowId}/steps?draft=1", workflowPersistentId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String stepPersistentId = mapper.readValue(jsonResponse, StepDto.class).getPersistentId();

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");
        step2.setStepNo(2);

        payload = mapper.writeValueAsString(step2);
        log.debug("JSON: " + payload);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps?draft=1", workflowPersistentId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        StepCore step11 = new StepCore();
        step11.setLabel("Test simple step 1.1");
        step11.setDescription("Lorem ipsum");

        payload = mapper.writeValueAsString(step11);
        log.debug("JSON: " + payload);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps/{stepId}/steps?draft=1", workflowPersistentId, stepPersistentId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        StepCore step13 = new StepCore();
        step13.setLabel("Test simple step 1.3");
        step13.setDescription("Lorem ipsum");

        payload = mapper.writeValueAsString(step13);
        log.debug("JSON: " + payload);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps/{stepId}/steps?draft=1", workflowPersistentId, stepPersistentId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.3")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        StepCore step12 = new StepCore();
        step12.setLabel("Test simple step 1.2");
        step12.setDescription("Lorem ipsum");
        step12.setStepNo(2);

        payload = mapper.writeValueAsString(step12);
        log.debug("JSON: " + payload);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps/{stepId}/steps?draft=1", workflowPersistentId, stepPersistentId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1.2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        mvc.perform(
                        get("/api/workflows/{workflowId}/steps/{stepId}?draft=1", workflowPersistentId, stepPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test complex step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)));

        mvc.perform(
                        get("/api/workflows/{workflowId}?draft=1", workflowPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", equalValue(workflowVersionId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("label", is("Test complex workflow with nested steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Test complex step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is("Test simple step 1.1")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].status", is("draft")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[1].label", is("Test simple step 1.2")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].status", is("draft")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[2].label", is("Test simple step 1.3")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].status", is("draft")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].status", is("draft")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));

        mvc.perform(
                        post("/api/workflows/{workflowId}/commit", workflowPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Test complex workflow with nested steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Test complex step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is("Test simple step 1.1")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[1].label", is("Test simple step 1.2")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[2].label", is("Test simple step 1.3")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));

        mvc.perform(
                        get("/api/workflows/{workflowId}?draft=1", workflowPersistentId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Test complex workflow with nested steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Test complex step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is("Test simple step 1.1")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[1].label", is("Test simple step 1.2")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].composedOf[2].label", is("Test simple step 1.3")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));
    }

    @Test
    public void shouldMakeDraftUpdate() throws Exception {
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;

        mvc.perform(
                        get("/api/workflows/{id}", workflowPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        WorkflowCore draftWorkflow = new WorkflowCore();
        draftWorkflow.setLabel("Evaluation of an inflectional analyzer [DRAFT]");
        draftWorkflow.setDescription("Evaluation of an inflectional analyzer... [DRAFT]");

        String workflowPayload = mapper.writeValueAsString(draftWorkflow);

        String workflowJson = mvc.perform(
                        put("/api/workflows/{workflowId}?draft=1", workflowPersistentId)
                                .content(workflowPayload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        WorkflowDto workflow = mapper.readValue(workflowJson, WorkflowDto.class);

        String stepId1 = workflow.getComposedOf().get(0).getPersistentId();
        StepCore step11 = new StepCore();
        step11.setLabel("Spam step");
        step11.setDescription("This is a spam step...");

        String stepPayload11 = mapper.writeValueAsString(step11);

        String stepId2 = workflow.getComposedOf().get(1).getPersistentId();
        StepCore step2 = new StepCore();
        step2.setLabel("Run an inflectional analyzer?");
        step2.setDescription("What exactly is an inflectional analyzer?");

        String stepPayload2 = mapper.writeValueAsString(step2);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps/{stepId}/steps?draft=1", workflowPersistentId, stepId1)
                                .content(stepPayload11)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("label", is(step11.getLabel())))
                .andExpect(jsonPath("description", is(step11.getDescription())))
                .andExpect(jsonPath("status", is("draft")));

        mvc.perform(
                        put("/api/workflows/{workflowId}/steps/{stepId}?draft=1", workflowPersistentId, stepId2)
                                .content(stepPayload2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("label", is(step2.getLabel())))
                .andExpect(jsonPath("description", is(step2.getDescription())))
                .andExpect(jsonPath("status", is("draft")));

        mvc.perform(
                        get("/api/workflows/{workflowId}?draft=1", workflowPersistentId)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(1)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].status", is("draft")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is(step11.getLabel())))
                .andExpect(jsonPath("composedOf[0].composedOf[0].description", is(step11.getDescription())))
                .andExpect(jsonPath("composedOf[1].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is(step2.getLabel())))
                .andExpect(jsonPath("composedOf[2].description", is(step2.getDescription())))
                .andExpect(jsonPath("composedOf[2].status", is("draft")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        mvc.perform(
                        post("/api/workflows/{workflowId}/commit", workflowPersistentId)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(1)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is(step11.getLabel())))
                .andExpect(jsonPath("composedOf[0].composedOf[0].description", is(step11.getDescription())))
                .andExpect(jsonPath("composedOf[1].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is(step2.getLabel())))
                .andExpect(jsonPath("composedOf[2].description", is(step2.getDescription())))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowPersistentId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(1)))
                .andExpect(jsonPath("composedOf[0].composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf[0].label", is(step11.getLabel())))
                .andExpect(jsonPath("composedOf[0].composedOf[0].description", is(step11.getDescription())))
                .andExpect(jsonPath("composedOf[1].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is(step2.getLabel())))
                .andExpect(jsonPath("composedOf[2].description", is(step2.getDescription())))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));
    }

    @Test
    public void shouldNotAddDraftStepToNonDraftWorkflow() throws Exception {
        String workflowId = "vHQEhe";

        StepCore step = new StepCore();
        step.setLabel("Non draft step");
        step.setDescription("Lorem ipsum dolor ipsum...");

        String payload = mapper.writeValueAsString(step);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps?draft=1", workflowId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotAddNonDraftStepToDraftWorkflow() throws Exception {
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
        List<ItemContributorId> contributors = new ArrayList<>();
        contributors.add(contributor1);
        workflow.setContributors(contributors);

        String payload = mapper.writeValueAsString(workflow);

        String responseJson = mvc.perform(
                        post("/api/workflows?draft=1")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("label", is(workflow.getLabel())))
                .andExpect(jsonPath("description", is(workflow.getDescription())))
                .andReturn().getResponse().getContentAsString();

        WorkflowDto created = mapper.readValue(responseJson, WorkflowDto.class);
        String workflowId = created.getPersistentId();

        StepCore step = new StepCore();
        step.setLabel("Non draft step");
        step.setDescription("Lorem ipsum...");

        payload = mapper.writeValueAsString(step);

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps", workflowId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteStepFromWorkflow() throws Exception {
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;
        String stepIdToDelete = "BNw43H";

        mvc.perform(
                get("/api/workflows/{persistentId}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(stepIdToDelete)))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[1].id", is(23)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[2].id", is(24)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        mvc.perform(
                delete("/api/workflows/{workflowId}/steps/{stepId}", workflowPersistentId, stepIdToDelete)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/workflows/{persistentId}", workflowPersistentId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[0].id", is(23)))
                .andExpect(jsonPath("composedOf[0].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[1].id", is(24)))
                .andExpect(jsonPath("composedOf[1].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));

        mvc.perform(
                get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("deprecated")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(stepIdToDelete)))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[1].id", is(23)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[2].id", is(24)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

    }


    @Test
    public void shouldDeleteStepFromDraftWorkflow() throws Exception {
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;

        mvc.perform(
                        get("/api/workflows/{id}", workflowPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        WorkflowCore draftWorkflow = new WorkflowCore();
        draftWorkflow.setLabel("Evaluation of an inflectional analyzer [DRAFT]");
        draftWorkflow.setDescription("Evaluation of an inflectional analyzer... [DRAFT]");

        String workflowPayload = mapper.writeValueAsString(draftWorkflow);

        String workflowJson = mvc.perform(
                        put("/api/workflows/{workflowId}?draft=1", workflowPersistentId)
                                .content(workflowPayload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        WorkflowDto workflow = mapper.readValue(workflowJson, WorkflowDto.class);
        String stepId2 = workflow.getComposedOf().get(1).getPersistentId();

        mvc.perform(
                        delete("/api/workflows/{workflowId}/steps/{stepId}?draft=1", workflowPersistentId, stepId2)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk());

        mvc.perform(
                        get("/api/workflows/{workflowId}?draft=1", workflowPersistentId)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));

        mvc.perform(
                        post("/api/workflows/{workflowId}/commit", workflowPersistentId)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(draftWorkflow.getLabel())))
                .andExpect(jsonPath("description", is(draftWorkflow.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)));
    }

    @Test
    public void shouldAddStepToWorkflow() throws Exception {
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}/history", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("workflow")))
                .andExpect(jsonPath("$[0].label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("$[0].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[0].id", is(workflowId)))
                .andExpect(jsonPath("$[0].status", is("approved")));

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

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
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
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));


        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is("The last step in a workflow")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}/history", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("workflow")))
                .andExpect(jsonPath("$[0].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].id", not(is(workflowId))))
                .andExpect(jsonPath("$[0].label", is("Evaluation of an inflectional analyzer")))

                .andExpect(jsonPath("$[1].category", is("workflow")))
                .andExpect(jsonPath("$[1].id", is(workflowId)))
                .andExpect(jsonPath("$[1].label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("$[1].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[1].status", is("deprecated")));

    }

    @Test
    public void shouldAddSubstepToWorkflow() throws Exception {
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{workflowId}/history", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("workflow")))
                .andExpect(jsonPath("$[0].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].id", notNullValue()));

        String stepPersistentId = "BNw43H";
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

        mvc.perform(post("/api/workflows/{workflowId}/steps/{stepId}/steps", workflowPersistentId, stepPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("The substep of a step in a workflow")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors[0].actor.id", is(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("provider")))
                .andExpect(jsonPath("contributors[1].actor.id", is(3)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("contributors[2].actor.id", is(1)))
                .andExpect(jsonPath("contributors[2].role.code", is("contributor")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));


        mvc.perform(get("/api/workflows/{workflowId}/steps/{stepId}/history", workflowPersistentId, stepPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("step")))
                .andExpect(jsonPath("$[0].persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].id", notNullValue()));

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
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

        mvc.perform(get("/api/workflows/{workflowId}/history", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("workflow")))
                .andExpect(jsonPath("$[0].persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("$[0].status", is("approved")))
                .andExpect(jsonPath("$[0].id", notNullValue()));
    }

    @Test
    public void shouldUpdateStepInWorkflowWhenActorHasManyRoles() throws Exception {
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;

        String stepPersistentId = "2CwYCU";
        Integer stepId = 14;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")));

        StepCore step = new StepCore();
        step.setLabel("A step with a triple actor");
        step.setDescription("Lorem ipsum");
        step.setStepNo(2);

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(1L), new ActorRoleId("provider"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(1L), new ActorRoleId("author"));
        ItemContributorId contributor3 = new ItemContributorId(new ActorId(1L), new ActorRoleId("contributor"));
        step.setContributors(List.of(contributor1, contributor2, contributor3));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(
                        put("/api/workflows/{workflowId}/steps/{stepId}", workflowPersistentId, stepPersistentId)
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(stepId))))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("A step with a triple actor")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors", hasSize(3)))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("contributors[0].role.code", is("provider")))
                .andExpect(jsonPath("contributors[1].actor.id", is(1)))
                .andExpect(jsonPath("contributors[1].role.code", is("author")))
                .andExpect(jsonPath("contributors[2].actor.id", is(1)))
                .andExpect(jsonPath("contributors[2].role.code", is("contributor")));

        mvc.perform(get("/api/workflows/{workflowId}", workflowPersistentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("composedOf[1].id", not(is(stepId))))
                .andExpect(jsonPath("composedOf[1].label", is("A step with a triple actor")))
                .andExpect(jsonPath("composedOf[1].contributors", hasSize(3)))
                .andExpect(jsonPath("composedOf[1].contributors[0].actor.id", is(1)))
                .andExpect(jsonPath("composedOf[1].contributors[0].role.code", is("provider")))
                .andExpect(jsonPath("composedOf[1].contributors[1].actor.id", is(1)))
                .andExpect(jsonPath("composedOf[1].contributors[1].role.code", is("author")))
                .andExpect(jsonPath("composedOf[1].contributors[2].actor.id", is(1)))
                .andExpect(jsonPath("composedOf[1].contributors[2].role.code", is("contributor")))
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
    public void shouldNotAddStepToWorkflowWhenActorHasRepeatedRoles() throws Exception {
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")));

        StepCore step = new StepCore();
        step.setLabel("A step with a triple actor");
        step.setDescription("Lorem ipsum");

        ItemContributorId contributor1 = new ItemContributorId(new ActorId(1L), new ActorRoleId("provider"));
        ItemContributorId contributor2 = new ItemContributorId(new ActorId(1L), new ActorRoleId("author"));
        ItemContributorId contributor3 = new ItemContributorId(new ActorId(1L), new ActorRoleId("provider"));
        step.setContributors(List.of(contributor1, contributor2, contributor3));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].field", is("contributors[2]")))
                .andExpect(jsonPath("errors[0].code", is("field.repeated")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotAddStepToWorkflowWhenStepNoIsIncorrect() throws Exception {
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")));

        StepCore step = new StepCore();
        step.setLabel("A step with wrong stepNo");
        step.setDescription("Lorem ipsum");
        step.setStepNo(-1);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
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
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;
        String stepPersistentId = "2CwYCU";
        Integer stepId = 14;

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is(stepPersistentId)))
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
        step.setProperties(properties);
        step.setStepNo(1);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/workflows/{workflowId}/steps/{stepId}", workflowPersistentId, stepPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Creation of a corpora")))
                .andExpect(jsonPath("description", is("...")))
                .andExpect(jsonPath("contributors[0].actor.id", is(4)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(0)));

        mvc.perform(get("/api/workflows/{id}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("description", is("Best practices for creating a born-digital dictionary, i.e. a lexicographical dataset.")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("eng")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("composedOf[0].id", not(is(stepId))))
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
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;

        mvc.perform(get("/api/workflows/{persistentId}", workflowPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));


        mvc.perform(delete("/api/workflows/{persistentId}", workflowPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/workflows/{persistentId}", workflowPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/workflows/{persistentId}", workflowPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));

    }


    @Test
    public void shouldDeletePreviousWorkflow() throws Exception {
        String workflowPersistentId = "vHQEhe";
        Integer workflowId = 21;

        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("New label of the workflow");
        workflow.setDescription("New description of the workflow");
        PropertyCore property = new PropertyCore();
        property.setType(new PropertyTypeId("keyword"));
        property.setValue("aaa");
        workflow.setProperties(Collections.singletonList(property));

        String workflowPayload = mapper.writeValueAsString(workflow);

        String workflowJson = mvc.perform(
                put("/api/workflows/{workflowId}", workflowPersistentId)
                        .content(workflowPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("New label of the workflow")))
                .andExpect(jsonPath("description", is("New description of the workflow")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].value", is("aaa")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        WorkflowDto newWorkflow = mapper.readValue(workflowJson, WorkflowDto.class);
        Long newWorkflowId = newWorkflow.getId();

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, newWorkflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(newWorkflowId.intValue())))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("New label of the workflow")))
                .andExpect(jsonPath("description", is("New description of the workflow")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].value", is("aaa")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, newWorkflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(newWorkflowId.intValue())))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("New label of the workflow")))
                .andExpect(jsonPath("description", is("New description of the workflow")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].value", is("aaa")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, workflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("status", is("deprecated")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));


        mvc.perform(delete("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(get("/api/workflows/{persistentId}", workflowPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(newWorkflowId.intValue())))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("New label of the workflow")))
                .andExpect(jsonPath("description", is("New description of the workflow")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].value", is("aaa")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, workflowId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(workflowId)))
                .andExpect(jsonPath("status", is("deprecated")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));

        mvc.perform(get("/api/workflows/{persistentId}/versions/{id}", workflowPersistentId, newWorkflowId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("id", is(newWorkflowId.intValue())))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("New label of the workflow")))
                .andExpect(jsonPath("description", is("New description of the workflow")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].value", is("aaa")))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].status", is("approved")));

    }

    @Test
    public void shouldNotCreateWorkflowWithInvalidIntProperty() throws Exception {
        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Test workflow with invalid year");
        workflow.setDescription("Lorem ipsum...");

        PropertyCore property1 = new PropertyCore(new PropertyTypeId("year"), "2021");
        PropertyCore property2 = new PropertyCore(new PropertyTypeId("year"), "one two");
        workflow.setProperties(List.of(property1, property2));

        String payload = mapper.writeValueAsString(workflow);

        mvc.perform(
                        post("/api/workflows")
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
    public void shouldRetrieveSuggestedWorkflow() throws Exception {
        String workflowId = "vHQEhe";
        int workflowVersionId = 21;

        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Suggested workflow");
        workflow.setDescription("This is a suggested workflow");

        String payload = mapper.writeValueAsString(workflow);

        mvc.perform(
                        put("/api/workflows/{id}", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("id", not(is(workflowVersionId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("suggested")));

        mvc.perform(
                        get("/api/workflows/{id}", workflowId)
                                .param("approved", "false")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("id", not(is(workflowVersionId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("suggested")));

        mvc.perform(
                        get("/api/workflows/{id}", workflowId)
                                .param("approved", "false")
                                .header("Authorization", IMPORTER_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("id", is(workflowVersionId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")));

        mvc.perform(get("/api/workflows/{id}", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("id", is(workflowVersionId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")));
    }

    @Test
    public void shouldRetrieveWorkflowStepsRelations() throws Exception {
        String workflowId = "vHQEhe";

        StepCore step = new StepCore();
        step.setLabel("Draw conclusions");
        step.setDescription("This step's purpose is to inspire the user to think a little bit and draw conclusions.");
        step.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().persistentId("OdKfPc").relation(new ItemRelationId("relates-to")).build()
                )
        );

        String payload = mapper.writeValueAsString(step);

        String stepJson = mvc.perform(
                        post("/api/workflows/{workflowId}/steps", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is(step.getLabel())))
                .andExpect(jsonPath("description", is(step.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("relatedItems[0].label", is("Consortium of European Social Science Data Archives")))
                .andExpect(jsonPath("relatedItems[0].category", is("dataset")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("relates-to")))
                .andReturn().getResponse().getContentAsString();

        StepDto stepDto = mapper.readValue(stepJson, StepDto.class);

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].label", is(step.getLabel())))
                .andExpect(jsonPath("composedOf[3].description", is(step.getDescription())))
                .andExpect(jsonPath("composedOf[3].status", is("approved")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].relatedItems", hasSize(1)))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].label", is("Consortium of European Social Science Data Archives")))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].category", is("dataset")))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].relation.code", is("relates-to")));
    }

    @Test
    public void shouldCorrectlyCreateRelationBetweenStepsFromTheSameWorkflow() throws Exception {
        String workflowId = "vHQEhe";

        StepCore step = new StepCore();
        step.setLabel("Draw conclusions");
        step.setDescription("This step's purpose is to inspire the user to think a little bit and draw conclusions.");
        step.setRelatedItems(
                List.of(
                        RelatedItemCore.builder().persistentId("gQu2wl").relation(new ItemRelationId("relates-to")).build()
                )
        );

        String payload = mapper.writeValueAsString(step);

        String stepJson = mvc.perform(
                        post("/api/workflows/{workflowId}/steps", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is(step.getLabel())))
                .andExpect(jsonPath("description", is(step.getDescription())))
                .andExpect(jsonPath("relatedItems", hasSize(1)))
                .andExpect(jsonPath("relatedItems[0].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("relatedItems[0].label", is("Interpret results")))
                .andExpect(jsonPath("relatedItems[0].category", is("step")))
                .andExpect(jsonPath("relatedItems[0].relation.code", is("relates-to")))
                .andReturn().getResponse().getContentAsString();

        StepDto stepDto = mapper.readValue(stepJson, StepDto.class);
        String stepId = stepDto.getPersistentId();
        int stepVersionId = stepDto.getId().intValue();

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("id", not(is(workflowId))))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("description", is("Evaluation of an inflectional analyzer...")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].label", is("Selection of textual works relevant for the research question")))
                .andExpect(jsonPath("composedOf[0].status", is("approved")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].label", is("Run an inflectional analyzer")))
                .andExpect(jsonPath("composedOf[1].status", is("approved")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[2].status", is("approved")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].relatedItems", hasSize(1)))
                .andExpect(jsonPath("composedOf[2].relatedItems[0].persistentId", is(stepId)))
                .andExpect(jsonPath("composedOf[2].relatedItems[0].id", is(stepVersionId)))
                .andExpect(jsonPath("composedOf[2].relatedItems[0].category", is("step")))
                .andExpect(jsonPath("composedOf[2].relatedItems[0].relation.code", is("is-related-to")))
                .andExpect(jsonPath("composedOf[3].label", is(step.getLabel())))
                .andExpect(jsonPath("composedOf[3].description", is(step.getDescription())))
                .andExpect(jsonPath("composedOf[3].status", is("approved")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].relatedItems", hasSize(1)))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].label", is("Interpret results")))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].category", is("step")))
                .andExpect(jsonPath("composedOf[3].relatedItems[0].relation.code", is("relates-to")));
    }

    @Test
    public void shouldReturnWorkflowInformationContributors() throws Exception {

        String workflowPersistentId = "vHQEhe";

        mvc.perform(get("/api/workflows/{id}/information-contributors", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[0].displayName", is("Administrator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
                .andExpect(jsonPath("$[0].role", is("administrator")))
                .andExpect(jsonPath("$[0].email", is("administrator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));
    }

    @Test
    public void shouldReturnStepInformationContributors() throws Exception {

        String workflowPersistentId = "vHQEhe";
        String stepPersistentId = "BNw43H";

        mvc.perform(get("/api/workflows/{id}/steps/{stepId}/information-contributors", workflowPersistentId, stepPersistentId)
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
    public void shouldReturnWorkflowInformationContributorsForVersion() throws Exception {

        String workflowPersistentId = "vHQEhe";

        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Suggested workflow");
        workflow.setDescription("This is a suggested workflow");

        String payload = mapper.writeValueAsString(workflow);

        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(put("/api/workflows/{id}", workflowPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is(workflow.getLabel())))
                .andExpect(jsonPath("description", is(workflow.getDescription())))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andReturn().getResponse().getContentAsString();


        Long versionId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, WorkflowDto.class).getId();

        log.debug("Workflows version Id: " + versionId);

        mvc.perform(get("/api/workflows/{id}/versions/{versionId}/information-contributors", workflowPersistentId, versionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[0].displayName", is("Administrator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
                .andExpect(jsonPath("$[0].role", is("administrator")))
                .andExpect(jsonPath("$[0].email", is("administrator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));
    }


    @Test
    public void shouldReturnStepInformationContributorsForVersion() throws Exception {

        String workflowPersistentId = "vHQEhe";
        String stepPersistentId = "BNw43H";

        StepCore step = new StepCore();
        step.setLabel("Suggested workflow");
        step.setDescription("This is a suggested workflow");

        String payload = mapper.writeValueAsString(step);

        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(put("/api/workflows/{id}/steps/{stepId}", workflowPersistentId, stepPersistentId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is(step.getLabel())))
                .andExpect(jsonPath("description", is(step.getDescription())))
                .andExpect(jsonPath("informationContributor.username", is("Administrator")))
                .andExpect(jsonPath("contributors", hasSize(0)))
                .andReturn().getResponse().getContentAsString();


        Long versionId = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, StepDto.class).getId();

        log.debug("Workflows version Id: " + versionId);

        mvc.perform(get("/api/workflows/{id}/steps/{stepId}/versions/{versionId}/information-contributors", workflowPersistentId, stepPersistentId, versionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("Administrator")))
                .andExpect(jsonPath("$[0].displayName", is("Administrator")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is(LocalDateTime.parse("2020-08-04T12:29:00").atZone(ZoneOffset.UTC).format(ApiDateTimeFormatter.dateTimeFormatter))))
                .andExpect(jsonPath("$[0].role", is("administrator")))
                .andExpect(jsonPath("$[0].email", is("administrator@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)))
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
    public void shouldGetMergeForWorkflow() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        mvc.perform(
                        get("/api/workflows/{id}/merge", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Creation of a dictionary / Consortium of European Social Science Data Archives / Gephi")));

    }

    @Test
    public void shouldMergeIntoWorkflow() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";

        String response = mvc.perform(
                        get("/api/workflows/{id}/merge", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Creation of a dictionary / Consortium of European Social Science Data Archives / Gephi")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        post("/api/workflows/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowId, datasetId, toolId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(workflowId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Creation of a dictionary / Consortium of European Social Science Data Archives / Gephi")));

        mvc.perform(
                        get("/api/datasets/{id}", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldGetMergeForStep() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";
        String stepId = "prblMo";

        mvc.perform(
                        get("/api/workflows/{workflowId}/steps/{id}/merge", workflowId, stepId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(stepId)))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Build the model of the dictionary / Consortium of European Social Science Data Archives / Gephi")));

    }

    @Test
    public void shouldMergeIntoStep() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "tqmbGY";
        String toolId = "n21Kfc";
        String stepId = "prblMo";

        String response = mvc.perform(
                        get("/api/workflows/{workflowId}/steps/{id}/merge", workflowId, stepId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, toolId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(stepId)))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Build the model of the dictionary / Consortium of European Social Science Data Archives / Gephi")))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        post("/api/workflows/{workflowId}/steps/merge", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", stepId, datasetId, toolId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(stepId)))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Build the model of the dictionary / Consortium of European Social Science Data Archives / Gephi")));


        mvc.perform(
                        get("/api/datasets/{id}", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldNotMergeStepsFromDifferentWorkflows() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "vHQEhe";
        String stepId = "BNw43H";
        String differentWorkflowStepId = "prblMo";

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(stepId)))
                .andExpect(jsonPath("composedOf[1].persistentId", not(differentWorkflowStepId)))
                .andExpect(jsonPath("composedOf[2].persistentId", not(differentWorkflowStepId)));


        mvc.perform(
                        get("/api/workflows/{workflowId}/steps/{id}/merge", workflowId, stepId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, differentWorkflowStepId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().is4xxClientError());


    }

    @Test
    public void shouldMergeStepsFromTheSameWorkflow() throws Exception {

        String datasetId = "OdKfPc";
        String workflowId = "vHQEhe";
        String stepOneId = "BNw43H";
        String stepTwoId = "sQY6US";

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(stepOneId)))
                .andExpect(jsonPath("composedOf[1].id", is(23)))
                .andExpect(jsonPath("composedOf[1].persistentId", is(stepTwoId)))
                .andExpect(jsonPath("composedOf[2].id", is(24)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("gQu2wl")));

        mvc.perform(
                        get("/api/workflows/{id}/history", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(21)))
                .andExpect(jsonPath("$[0].persistentId", is(workflowId)));


        String response = mvc.perform(
                        get("/api/workflows/{workflowId}/steps/{id}/merge", workflowId, stepOneId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, stepTwoId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Selection of textual works relevant for the research question / Consortium of European Social Science Data Archives / Run an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/workflows/{workflowId}/steps/merge", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", stepOneId, datasetId, stepTwoId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(stepOneId)))
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Selection of textual works relevant for the research question / Consortium of European Social Science Data Archives / Run an inflectional analyzer")))
                .andReturn().getResponse().getContentAsString();


        String mergedStepPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, StepDto.class).getPersistentId();

        int mergedStepId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, StepDto.class).getId().intValue();

        String mergedStepLabel = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, StepDto.class).getLabel();

        mvc.perform(
                        get("/api/workflows/{workflowId}/steps/{id}/history", workflowId, mergedStepPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].category", is("step")))
                .andExpect(jsonPath("$[0].label", is(mergedStepLabel)))
                .andExpect(jsonPath("$[0].persistentId", is(mergedStepPersistentId)))
                .andExpect(jsonPath("$[1].persistentId", is(stepTwoId)))
                .andExpect(jsonPath("$[1].category", is("step")))
                .andExpect(jsonPath("$[2].persistentId", is(stepOneId)))
                .andExpect(jsonPath("$[2].category", is("step")))
                .andExpect(jsonPath("$[3].persistentId", is(datasetId)))
                .andExpect(jsonPath("$[3].category", is("dataset")));

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].id", is(mergedStepId)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(mergedStepPersistentId)))
                .andExpect(jsonPath("composedOf[1].id", is(24)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("gQu2wl")));
        ;


        mvc.perform(
                        get("/api/workflows/{id}/history", workflowId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", not(21)))
                .andExpect(jsonPath("$[0].persistentId", is(workflowId)))
                .andExpect(jsonPath("$[1].id", is(21)))
                .andExpect(jsonPath("$[1].persistentId", is(workflowId)));
    }

    @Test
    public void shouldMergeDifferentWorkflowsWithStepCollection() throws Exception {

        String datasetId = "OdKfPc";
        String workflowOneId = "tqmbGY";
        String workflowTwoId = "vHQEhe";
        String toolId = "n21Kfc";

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowOneId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].id", is(13)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("prblMo")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(14)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("2CwYCU")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].persistentId", is("dVZeir")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].persistentId", is("EPax9f")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].persistentId", is("HLYtzq")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].persistentId", is("xYpCdU")))
                .andExpect(jsonPath("composedOf[2].id", is(19)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("k68NbF")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].id", is(20)))
                .andExpect(jsonPath("composedOf[3].persistentId", is("U8vUos")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));


        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowTwoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(23)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].id", is(24)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));

        String response = mvc.perform(
                        get("/api/workflows/{id}/merge", workflowOneId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", datasetId, toolId, workflowTwoId)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowOneId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Creation of a dictionary / Consortium of European Social Science Data Archives / Gephi / Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andReturn().getResponse().getContentAsString();

        String mergedResponse = mvc.perform(
                        post("/api/workflows/merge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", workflowOneId, datasetId, toolId, workflowTwoId)
                                .content(response)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", not(workflowOneId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is("Creation of a dictionary / Consortium of European Social Science Data Archives / Gephi / Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("composedOf", hasSize(7)))

                //steps of first workflow
                .andExpect(jsonPath("composedOf[0].id", is(13)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("prblMo")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(14)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("2CwYCU")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].id", is(15)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].persistentId", is("dVZeir")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].id", is(16)))
                .andExpect(jsonPath("composedOf[1].composedOf[1].persistentId", is("EPax9f")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].id", is(17)))
                .andExpect(jsonPath("composedOf[1].composedOf[2].persistentId", is("HLYtzq")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].id", is(18)))
                .andExpect(jsonPath("composedOf[1].composedOf[3].persistentId", is("xYpCdU")))
                .andExpect(jsonPath("composedOf[2].id", is(19)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("k68NbF")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].id", is(20)))
                .andExpect(jsonPath("composedOf[3].persistentId", is("U8vUos")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)))

                //steps of second workflow
                .andExpect(jsonPath("composedOf[4].id", is(22)))
                .andExpect(jsonPath("composedOf[4].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[4].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[5].id", is(23)))
                .andExpect(jsonPath("composedOf[5].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[5].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[6].id", is(24)))
                .andExpect(jsonPath("composedOf[6].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[6].composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();


        String mergedWorkflowPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, WorkflowDto.class).getPersistentId();

        String mergedWorkflowLabel = TestJsonMapper.serializingObjectMapper()
                .readValue(mergedResponse, WorkflowDto.class).getLabel();

        mvc.perform(
                        get("/api/datasets/{id}", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isNotFound());


        mvc.perform(
                        get("/api/workflows/{workflowId}", mergedWorkflowPersistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("label", is(mergedWorkflowLabel)))
                .andExpect(jsonPath("composedOf", hasSize(7)))

                //steps of first workflow
                .andExpect(jsonPath("composedOf[0].id", is(13)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("prblMo")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(14)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("2CwYCU")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].persistentId", is("dVZeir")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].persistentId", is("EPax9f")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].persistentId", is("HLYtzq")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].persistentId", is("xYpCdU")))
                .andExpect(jsonPath("composedOf[2].id", is(19)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("k68NbF")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].id", is(20)))
                .andExpect(jsonPath("composedOf[3].persistentId", is("U8vUos")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)))

                //steps of second workflow
                .andExpect(jsonPath("composedOf[4].id", is(22)))
                .andExpect(jsonPath("composedOf[4].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[4].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[5].id", is(23)))
                .andExpect(jsonPath("composedOf[5].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[5].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[6].id", is(24)))
                .andExpect(jsonPath("composedOf[6].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[6].composedOf", hasSize(0)));


        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowOneId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("approved", "false")
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].id", is(13)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("prblMo")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(14)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("2CwYCU")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[1].composedOf[0].persistentId", is("dVZeir")))
                .andExpect(jsonPath("composedOf[1].composedOf[1].persistentId", is("EPax9f")))
                .andExpect(jsonPath("composedOf[1].composedOf[2].persistentId", is("HLYtzq")))
                .andExpect(jsonPath("composedOf[1].composedOf[3].persistentId", is("xYpCdU")))
                .andExpect(jsonPath("composedOf[2].id", is(19)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("k68NbF")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[3].id", is(20)))
                .andExpect(jsonPath("composedOf[3].persistentId", is("U8vUos")))
                .andExpect(jsonPath("composedOf[3].composedOf", hasSize(0)));

        mvc.perform(
                        get("/api/workflows/{workflowId}", workflowTwoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("approved", "false")
                                .header("Authorization", MODERATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].id", is(22)))
                .andExpect(jsonPath("composedOf[0].persistentId", is("BNw43H")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].id", is(23)))
                .andExpect(jsonPath("composedOf[1].persistentId", is("sQY6US")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].id", is(24)))
                .andExpect(jsonPath("composedOf[2].persistentId", is("gQu2wl")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)));


    }

    @Test
    public void shouldWorkflowWithStepsAndChangeItsOrder() throws Exception {
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

        String workflowPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, WorkflowDto.class).getPersistentId();

        StepCore step3 = new StepCore();
        step3.setLabel("Test simple step 3");
        step3.setDescription("Lorem ipsum");
        step3.setStepNo(1);
        step3.setSource(null);

        String payload3 = TestJsonMapper.serializingObjectMapper().writeValueAsString(step3);
        log.debug("JSON: " + payload3);

        String responseStep3 = mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 3")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();


        String responseStep3persistentId = mapper.readValue(responseStep3, StepDto.class).getPersistentId();

        StepCore step2 = new StepCore();
        step2.setLabel("Test simple step 2");
        step2.setDescription("Lorem ipsum");
        step2.setStepNo(1);

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(step2);
        log.debug("JSON: " + payload2);

        String responseStep2 = mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 2")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        String responseStep2persistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(responseStep2, StepDto.class).getPersistentId();

        StepCore step1 = new StepCore();
        step1.setLabel("Test simple step 1");
        step1.setDescription("Lorem ipsum");
        step1.setStepNo(1);

        String payload1 = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload1);

        String responseStep1 = mvc.perform(post("/api/workflows/{workflowId}/steps", workflowPersistentId)
                        .content(payload1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 1")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        String responseStep1persistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(responseStep1, StepDto.class).getPersistentId();

        mvc.perform(get("/api/workflows/{workflowId}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow with steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Test simple step 1")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(responseStep1persistentId)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is(responseStep2persistentId)))
                .andExpect(jsonPath("composedOf[2].label", is("Test simple step 3")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].persistentId", is(responseStep3persistentId)));


        step3.setStepNo(1);

        String payloadPut = TestJsonMapper.serializingObjectMapper().writeValueAsString(step3);
        log.debug("JSON: " + payloadPut);

        mvc.perform(put("/api/workflows/{workflowId}/steps/{stepPersistentId}", workflowPersistentId, responseStep3persistentId)
                        .content(payloadPut)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("step")))
                .andExpect(jsonPath("label", is("Test simple step 3")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)));


        mvc.perform(get("/api/workflows/{workflowId}", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("label", is("Test simple workflow with steps")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(3)))
                .andExpect(jsonPath("composedOf[0].label", is("Test simple step 3")))
                .andExpect(jsonPath("composedOf[0].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[0].persistentId", is(responseStep3persistentId)))
                .andExpect(jsonPath("composedOf[1].label", is("Test simple step 1")))
                .andExpect(jsonPath("composedOf[1].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[1].persistentId", is(responseStep1persistentId)))
                .andExpect(jsonPath("composedOf[2].label", is("Test simple step 2")))
                .andExpect(jsonPath("composedOf[2].composedOf", hasSize(0)))
                .andExpect(jsonPath("composedOf[2].persistentId", is(responseStep2persistentId)));
    }

    @Test
    public void shouldReturnDifferenceBetweenStepAndTrainingMaterial() throws Exception {
        String workflowPersistentId = "tqmbGY";
        String stepPersistentId = "prblMo";
        Long stepId = 13L;
        String trainingMaterialPersistentId = "WfcKvG";

        String response = mvc.perform(
                get("/api/training-materials/{id}", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andReturn().getResponse().getContentAsString();

        TrainingMaterialDto trainingMaterialDto = TestJsonMapper.serializingObjectMapper()
                .readValue(response, TrainingMaterialDto.class);

        TrainingMaterialCore trainingMaterialCore = new TrainingMaterialCore();
        trainingMaterialCore.setLabel(trainingMaterialDto.getLabel());
        trainingMaterialCore.setDescription(trainingMaterialDto.getDescription());
        trainingMaterialCore.setAccessibleAt(trainingMaterialDto.getAccessibleAt());
        trainingMaterialCore.setDateLastUpdated(ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC")));
        trainingMaterialCore.setVersion(trainingMaterialDto.getVersion());

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(trainingMaterialCore);

        mvc.perform(
                put("/api/training-materials/{id}", trainingMaterialPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("dateLastUpdated", is("2020-04-15T12:00:00+0000")));


        mvc.perform(get("/api/workflows/{persistentId}/steps/{stepPersistentId}/diff", workflowPersistentId, stepPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", trainingMaterialPersistentId )
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(stepPersistentId)))
                .andExpect(jsonPath("item.id", is(stepId.intValue())))
                .andExpect(jsonPath("item.category", is("step")))
                .andExpect(jsonPath("item.label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("item.informationContributor.id", is(3)))
                .andExpect(jsonPath("item.status", is("approved")))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is(trainingMaterialPersistentId)))
                .andExpect(jsonPath("other.category", is("training-material")))
                .andExpect(jsonPath("other.version", is("3.0")))
                .andExpect(jsonPath("other.status", is("approved")))
                .andExpect(jsonPath("other.description", is("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")))
                .andExpect(jsonPath("other.accessibleAt[0]", is("https://www.youtube.com/watch?v=2FqM4gKeNO4")))
                .andExpect(jsonPath("other.dateLastUpdated", is("2020-04-15T12:00:00+0000")));

    }

    @Test
    public void shouldReturnDifferenceBetweenWorkflows() throws Exception {
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;
        String otherWorkflowPersistentId = "vHQEhe";
        Integer otherWorkflowId = 21;

        mvc.perform(get("/api/workflows/{persistentId}/diff", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", otherWorkflowPersistentId)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("item.id", is(workflowId)))
                .andExpect(jsonPath("item.category", is("workflow")))
                .andExpect(jsonPath("item.label", is("Creation of a dictionary")))
                .andExpect(jsonPath("item.informationContributor.id", is(3)))
                .andExpect(jsonPath("item.properties[0].type.code", is("language")))
                .andExpect(jsonPath("item.properties[0].concept.code", is("eng")))
                .andExpect(jsonPath("item.composedOf", hasSize(4)))
                .andExpect(jsonPath("equal", is(false)))
                .andExpect(jsonPath("other.persistentId", is(otherWorkflowPersistentId)))
                .andExpect(jsonPath("other.id", is(otherWorkflowId)))
                .andExpect(jsonPath("other.category", is("workflow")))
                .andExpect(jsonPath("other.label", is("Evaluation of an inflectional analyzer")))
                .andExpect(jsonPath("other.composedOf", hasSize(3)))
                .andExpect(jsonPath("other.informationContributor.id", is(1)));
    }

    @Test
    public void shouldNotReturnDifferenceBetweenComposedOf() throws Exception {
        String workflowPersistentId = "tqmbGY";
        Integer workflowId = 12;

        mvc.perform(get("/api/workflows/{persistentId}/diff", workflowPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("with", workflowPersistentId)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("item.persistentId", is(workflowPersistentId)))
                .andExpect(jsonPath("item.id", is(workflowId)))
                .andExpect(jsonPath("item.category", is("workflow")))
                .andExpect(jsonPath("item.label", is("Creation of a dictionary")))
                .andExpect(jsonPath("item.informationContributor.id", is(3)))
                .andExpect(jsonPath("item.properties[0].type.code", is("language")))
                .andExpect(jsonPath("item.properties[0].concept.code", is("eng")))
                .andExpect(jsonPath("item.composedOf", hasSize(4)))
                .andExpect(jsonPath("equal", is(true)))
                .andExpect(jsonPath("other.composedOf[0]", nullValue()))
                .andExpect(jsonPath("other.composedOf[1]", nullValue()))
                .andExpect(jsonPath("other.composedOf[2]", nullValue()))
                .andExpect(jsonPath("other.composedOf[3]", nullValue()));
    }

    @Test
    public void shouldDeleteAndRevertWorkflow() throws Exception {

        WorkflowCore workflow = new WorkflowCore();
        workflow.setLabel("Workflow to revert");
        workflow.setDescription("Lorem ipsum dolor");

        String workflowPayload = mapper.writeValueAsString(workflow);

        String workflowJSON = mvc.perform(
                        post("/api/workflows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(workflowPayload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(workflow.getLabel())))
                .andExpect(jsonPath("description", is(workflow.getDescription())))
                .andReturn().getResponse().getContentAsString();

        WorkflowDto workflowDto = mapper.readValue(workflowJSON, WorkflowDto.class);

        mvc.perform(delete("/api/workflows/{id}", workflowDto.getPersistentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(
                        put("/api/workflows/{id}/revert", workflowDto.getPersistentId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", ADMINISTRATOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(workflow.getLabel())))
                .andExpect(jsonPath("description", is(workflow.getDescription())))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                        get("/api/workflows/{id}", workflowDto.getPersistentId())
                                .param("approved", "true")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(workflowDto.getPersistentId())))
                .andExpect(jsonPath("id", is(workflowDto.getId().intValue())))
                .andExpect(jsonPath("category", is("workflow")))
                .andExpect(jsonPath("status", is("approved")))
                .andExpect(jsonPath("label", is(workflow.getLabel())))
                .andExpect(jsonPath("description", is(workflow.getDescription())));
    }
}
