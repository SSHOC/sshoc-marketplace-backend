package eu.sshopencloud.marketplace.controllers.activities;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.activities.ActivityCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.activities.Activity;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ActivityControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnActivities() throws Exception {

        mvc.perform(get("/api/activities")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnSimpleActivity() throws Exception {
        Integer activityId = 17;

        mvc.perform(get("/api/activities/{id}", activityId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(activityId)))
                .andExpect(jsonPath("category", is("activity")))
                .andExpect(jsonPath("label", is("Publishing")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(0)))
                .andExpect(jsonPath("partOf", hasSize(0)));
    }


    @Test
    public void shouldReturnComplexActivity() throws Exception {
        Integer activityId = 20;

        mvc.perform(get("/api/activities/{id}", activityId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(activityId)))
                .andExpect(jsonPath("category", is("activity")))
                .andExpect(jsonPath("label", is("Creation of a dictionary")))
                .andExpect(jsonPath("licenses", hasSize(0)))
                .andExpect(jsonPath("informationContributors", hasSize(1)))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)))
                .andExpect(jsonPath("composedOf", hasSize(4)))
                .andExpect(jsonPath("composedOf[0].id", is(12)))
                .andExpect(jsonPath("composedOf[0].label", is("Build the model of the dictionary")))
                .andExpect(jsonPath("composedOf[1].id", is(18)))
                .andExpect(jsonPath("composedOf[1].label", is("Creation of a corpora")))
                .andExpect(jsonPath("composedOf[2].id", is(19)))
                .andExpect(jsonPath("composedOf[2].label", is("Write a dictionary")))
                .andExpect(jsonPath("composedOf[3].id", is(17)))
                .andExpect(jsonPath("composedOf[3].label", is("Publishing")))
                .andExpect(jsonPath("partOf", hasSize(0)));
   }

    @Test
    public void shouldNotReturnActivityWhenNotExist() throws Exception {
        Integer datasetId = 1009;

        mvc.perform(get("/api/activities/{id}", datasetId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateSimpleActivity() throws Exception {
        ActivityCore activity = new ActivityCore();
        activity.setLabel("Test simple activity");
        activity.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(activity);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/activities")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("activity")))
                .andExpect(jsonPath("label", is("Test simple activity")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Activity")))
                .andExpect(jsonPath("composedOf", hasSize(0)))
                .andExpect(jsonPath("partOf", hasSize(0)));
    }


    @Test
    public void shouldCreateComplexActivity() throws Exception {
        ActivityCore step1 = new ActivityCore();
        step1.setLabel("Test step 1 activity");
        step1.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/activities")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long step1Id = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Activity.class).getId();

        ActivityCore step2 = new ActivityCore();
        step1.setLabel("Test step 2 activity");
        step1.setDescription("Lorem ipsum");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        jsonResponse = mvc.perform(post("/api/activities")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long step2Id = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Activity.class).getId();

        ActivityCore activity = new ActivityCore();
        activity.setLabel("Test complex activity");
        activity.setDescription("Lorem ipsum");
        List<Long> steps = new ArrayList<Long>();
        steps.add(step1Id);
        steps.add(step2Id);
        activity.setComposedOf(steps);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(activity);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/activities")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("activity")))
                .andExpect(jsonPath("label", is("Test complex activity")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.label", is("Activity")))
                .andExpect(jsonPath("composedOf", hasSize(2)))
                .andExpect(jsonPath("composedOf[0].id", is((int)step1Id)))
                .andExpect(jsonPath("composedOf[0].label", is("Test step 1 activity")))
                .andExpect(jsonPath("composedOf[1].id", is((int)step2Id)))
                .andExpect(jsonPath("composedOf[1].label", is("Test step 2 activity")))
                .andExpect(jsonPath("partOf", hasSize(0)));
    }

    @Test
    public void shouldNotCreateComplexActivityWhenStepNotExist() throws Exception {
        ActivityCore step1 = new ActivityCore();
        step1.setLabel("Test step 2 activity");
        step1.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(step1);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/activities")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long step2Id = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Activity.class).getId();

        ActivityCore activity = new ActivityCore();
        activity.setLabel("Test complex activity");
        activity.setDescription("Lorem ipsum");
        List<Long> steps = new ArrayList<Long>();
        steps.add(150l);
        steps.add(step2Id);
        steps.add(151l);
        activity.setComposedOf(steps);

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(activity);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/activities")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("composedOf[0]")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()))
                .andExpect(jsonPath("errors[1].field", is("composedOf[2]")))
                .andExpect(jsonPath("errors[1].code", is("field.notExist")))
                .andExpect(jsonPath("errors[1].message", notNullValue()));
    }

}
