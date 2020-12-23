package eu.sshopencloud.marketplace.controllers.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceCore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ActorSourceControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;


    @Before
    public void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }


    @Test
    public void shouldReturnAllActorSources() throws Exception {

        mvc.perform(get("/api/actor-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("ORCID")))
                .andExpect(jsonPath("$[1].code", is("DBLP")))
                .andExpect(jsonPath("$[2].code", is("Wikidata")));
    }

    @Test
    public void shouldCreateActorSource() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test source service")
                .ord(3)
                .build();

        String payload = mapper.writeValueAsString(actorSource);

        mvc.perform(
                post("/api/actor-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("test")))
                .andExpect(jsonPath("label", is("Test source service")));

        mvc.perform(get("/api/actor-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("ORCID")))
                .andExpect(jsonPath("$[1].code", is("DBLP")))
                .andExpect(jsonPath("$[2].code", is("test")))
                .andExpect(jsonPath("$[2].label", is("Test source service")))
                .andExpect(jsonPath("$[3].code", is("Wikidata")));
    }

    @Test
    public void shouldNotCreateActorSourceAtWrongPosition() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test...")
                .ord(50)
                .build();

        String payload = mapper.writeValueAsString(actorSource);

        mvc.perform(
                post("/api/actor-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRetrieveActorSource() throws Exception {
        mvc.perform(get("/api/actor-sources/Wikidata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("Wikidata")))
                .andExpect(jsonPath("label", is("Wikidata")));
    }

    @Test
    public void shouldUpdateActorSource() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("Wikidata")
                .label("Wikidata v2")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorSource);

        mvc.perform(
                put("/api/actor-sources/{sourceId}", "Wikidata")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("Wikidata")))
                .andExpect(jsonPath("label", is("Wikidata v2")));

        mvc.perform(get("/api/actor-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("Wikidata")))
                .andExpect(jsonPath("$[0].label", is("Wikidata v2")))
                .andExpect(jsonPath("$[1].code", is("ORCID")))
                .andExpect(jsonPath("$[2].code", is("DBLP")));
    }

    @Test
    public void shouldRemoveActorSource() throws Exception {
        mvc.perform(
                delete("/api/actor-sources/{sourceId}", "DBLP")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(get("/api/actor-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("ORCID")))
                .andExpect(jsonPath("$[1].code", is("Wikidata")));
    }

    @Test
    @Ignore
    // TODO when actors have the external ids assigned
    public void shouldNotRemoveActorSourceInUse() throws Exception {
        mvc.perform(
                delete("/api/actor-sources/{sourceId}", "TODO")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateActorSourceUnauthorized() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test...")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorSource);

        mvc.perform(
                post("/api/actor-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotUpdateActorSourceUnauthorized() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("OCRID")
                .label("OCRID v2")
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(actorSource);

        mvc.perform(
                post("/api/actor-sources/{sourceId}", "OCRID")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotDeleteActorSourceUnauthorized() throws Exception {
        mvc.perform(delete("/api/actor-sources/{sourceId}", "Wikidata"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateActorRoleAsAdministrator() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test v2")
                .ord(4)
                .build();

        String payload = mapper.writeValueAsString(actorSource);

        mvc.perform(
                post("/api/actor-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());
    }
}
