package eu.sshopencloud.marketplace.controllers.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorExternalIdCore;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceCore;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceId;
import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("DBLP")))
                .andExpect(jsonPath("$[1].ord", is(2)))
                .andExpect(jsonPath("$[2].code", is("Wikidata")))
                .andExpect(jsonPath("$[2].ord", is(3)));
    }

    @Test
    public void shouldCreateActorSource() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test source service")
                .urlTemplate("https://www.test.org/{source-actor-id}")
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
                .andExpect(jsonPath("urlTemplate", is("https://www.test.org/{source-actor-id}")))
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
    public void shouldCreateActorSourceWithoutOrd() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test source service")
                .urlTemplate("https://www.test.org/{source-actor-id}")
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
                .andExpect(jsonPath("label", is("Test source service")))
                .andExpect(jsonPath("urlTemplate", is("https://www.test.org/{source-actor-id}")))
                .andExpect(jsonPath("ord", is(4)));

        mvc.perform(get("/api/actor-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("ORCID")))
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("DBLP")))
                .andExpect(jsonPath("$[1].ord", is(2)))
                .andExpect(jsonPath("$[2].code", is("Wikidata")))
                .andExpect(jsonPath("$[2].ord", is(3)))
                .andExpect(jsonPath("$[3].label", is("Test source service")))
                .andExpect(jsonPath("$[3].code", is("test")))
                .andExpect(jsonPath("$[3].ord", is(4)));
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
                .andExpect(jsonPath("urlTemplate", is("https://www.wikidata.org/wiki/{source-actor-id}")))
                .andExpect(jsonPath("label", is("Wikidata")));
    }

    @Test
    public void shouldUpdateActorSource() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("Wikidata")
                .label("Wikidata v2")
                .urlTemplate("https://www.wikidata.org/wiki/{source-actor-id}")
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
                .andExpect(jsonPath("urlTemplate", is("https://www.wikidata.org/wiki/{source-actor-id}")))
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
    public void shouldNotRemoveActorSourceInUse() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42")
        ));

        String payload = mapper.writeValueAsString(actor);

        mvc.perform(
                post("/api/actors")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("externalIds", hasSize(1)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("Wikidata")));

        mvc.perform(
                delete("/api/actor-sources/{sourceId}", "Wikidata")
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
                put("/api/actor-sources/{sourceId}", "OCRID")
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
    public void shouldCreateActorSourceAsAdministrator() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test v2")
                .urlTemplate("https://www.test.org/{source-actor-id}")
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

    @Test
    public void shouldCreateItemSourceWithoutUrlTemplate() throws Exception {
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
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("test")))
                .andExpect(jsonPath("label", is("Test...")));
    }

    @Test
    public void shouldNotCreateItemSourceWithWrongUrlTemplate() throws Exception {
        ActorSourceCore actorSource = ActorSourceCore.builder()
                .code("test")
                .label("Test...")
                .urlTemplate("https://www.test.org/{item-id}")
                .ord(1)
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
}
