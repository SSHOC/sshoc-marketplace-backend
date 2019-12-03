package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import org.junit.FixMethodOrder;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActorControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnActors() throws Exception {

        mvc.perform(get("/api/actors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnActorsByName() throws Exception {

        mvc.perform(get("/api/actors?q=CESSDA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("CESSDA")))
                .andExpect(jsonPath("$[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("$[0].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldReturnActor() throws Exception {
        Integer actorId = 5;

        mvc.perform(get("/api/actors/{id}", actorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorId)))
                .andExpect(jsonPath("name", is("John Smith")))
                .andExpect(jsonPath("website", is("https://example.com/")))
                .andExpect(jsonPath("email", is("john@example.com")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].id", is(3)))
                .andExpect(jsonPath("affiliations[0].name", is("SSHOC project consortium")))
                .andExpect(jsonPath("affiliations[0].website", is("https://sshopencloud.eu/")))
                .andExpect(jsonPath("affiliations[0].email", isEmptyOrNullString()));
    }

    @Test
    public void shouldntReturnActorWhenNotExist() throws Exception {
        Integer actorId = 51;

        mvc.perform(get("/api/actors/{id}", actorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateActorWithoutAffiliations() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setWebsite("http://www.example.org");
        actor.setEmail("test@example.org");

        mvc.perform(post("/api/actors")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", is("http://www.example.org")))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(0)));
    }


    @Test
    public void shouldCreateActorWithAffiliations() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(1l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        mvc.perform(post("/api/actors")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", isEmptyOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(2)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("affiliations[0].email", isEmptyOrNullString()))
                .andExpect(jsonPath("affiliations[1].name", is("CESSDA")))
                .andExpect(jsonPath("affiliations[1].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldCreateActorWhenAffiliationNotExist() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(100l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        mvc.perform(post("/api/actors")
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldUpdateActorWithoutAffiliations() throws Exception {
        Integer actorId = 2;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setWebsite("http://www.example.org");
        actor.setEmail("test@example.org");

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorId)))
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", is("http://www.example.org")))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(0)));
    }

    @Test
    public void shouldUpdateActorWithAffiliations() throws Exception {
        Integer actorId = 2;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(1l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", isEmptyOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(2)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("affiliations[0].email", isEmptyOrNullString()))
                .andExpect(jsonPath("affiliations[1].name", is("CESSDA")))
                .andExpect(jsonPath("affiliations[1].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldUpdateActorWhenAffiliationNotExist() throws Exception {
        Integer actorId = 2;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(100l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }


    @Test
    public void shouldntUpdateActorWhenNotExist() throws Exception {
        Integer actorId = 99;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setWebsite("http://www.example.org");
        actor.setEmail("test@example.org");

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(actor))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteActor() throws Exception {
        Integer actorId = 7;

        mvc.perform(delete("/api/actors/{id}", actorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

}

