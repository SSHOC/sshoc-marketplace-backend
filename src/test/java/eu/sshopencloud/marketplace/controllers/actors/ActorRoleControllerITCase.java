package eu.sshopencloud.marketplace.controllers.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@Transactional
class ActorRoleControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;


    @BeforeEach
    void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }


    @Test
    void shouldReturnAllActorRoles() throws Exception {

        mvc.perform(get("/api/actor-roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("contributor")))
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("author")))
                .andExpect(jsonPath("$[1].ord", is(2)))
                .andExpect(jsonPath("$[2].code", is("provider")))
                .andExpect(jsonPath("$[2].ord", is(3)))
                .andExpect(jsonPath("$[3].code", is("contact")))
                .andExpect(jsonPath("$[3].ord", is(4)))
                .andExpect(jsonPath("$[4].code", is("funder")))
                .andExpect(jsonPath("$[4].ord", is(5)))
                .andExpect(jsonPath("$[5].code", is("helpdesk")))
                .andExpect(jsonPath("$[5].ord", is(6)));
    }

    @Test
    void shouldCreateActorRole() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("test")
                .label("Test...")
                .ord(3)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("test")))
                .andExpect(jsonPath("label", is("Test...")));

        mvc.perform(get("/api/actor-roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("contributor")))
                .andExpect(jsonPath("$[1].code", is("author")))
                .andExpect(jsonPath("$[2].code", is("test")))
                .andExpect(jsonPath("$[3].code", is("provider")))
                .andExpect(jsonPath("$[4].code", is("contact")))
                .andExpect(jsonPath("$[5].code", is("funder")))
                .andExpect(jsonPath("$[6].code", is("helpdesk")));
    }

    @Test
    void shouldCreateActorRoleWithoutOrder() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("test")
                .label("Test...")
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("test")))
                .andExpect(jsonPath("label", is("Test...")))
                .andExpect(jsonPath("ord", is(7)));

        mvc.perform(get("/api/actor-roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("contributor")))
                .andExpect(jsonPath("$[1].code", is("author")))
                .andExpect(jsonPath("$[2].code", is("provider")))
                .andExpect(jsonPath("$[3].code", is("contact")))
                .andExpect(jsonPath("$[4].code", is("funder")))
                .andExpect(jsonPath("$[5].code", is("helpdesk")))
                .andExpect(jsonPath("$[6].code", is("test")))
                .andExpect(jsonPath("$[6].ord", is(7)));
    }

    @Test
    void shouldNotCreateActorRoleAtWrongPosition() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("test")
                .label("Test...")
                .ord(0)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRetrieveActorRole() throws Exception {
        mvc.perform(get("/api/actor-roles/contact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("contact")))
                .andExpect(jsonPath("label", is("Contact")));
    }

    @Test
    void shouldUpdateActorRole() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("provider")
                .label("Provider v2")
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                put("/api/actor-roles/{roleId}", "provider")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("provider")))
                .andExpect(jsonPath("label", is("Provider v2")));

        mvc.perform(get("/api/actor-roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("contributor")))
                .andExpect(jsonPath("$[1].code", is("provider")))
                .andExpect(jsonPath("$[2].code", is("author")))
                .andExpect(jsonPath("$[3].code", is("contact")))
                .andExpect(jsonPath("$[4].code", is("funder")))
                .andExpect(jsonPath("$[5].code", is("helpdesk")));
    }

    @Test
    void shouldRemoveActorRole() throws Exception {
        mvc.perform(
                delete("/api/actor-roles/{roleId}", "contact")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(get("/api/actor-roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("contributor")))
                .andExpect(jsonPath("$[1].code", is("author")))
                .andExpect(jsonPath("$[2].code", is("provider")))
                .andExpect(jsonPath("$[3].code", is("funder")))
                .andExpect(jsonPath("$[4].code", is("helpdesk")));
    }

    @Test
    void shouldNotRemoveActorRoleInUse() throws Exception {
        mvc.perform(
                delete("/api/actor-roles/{roleId}", "author")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateActorRoleUnauthorized() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("test")
                .label("Test...")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotUpdateActorRoleUnauthorized() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("author")
                .label("Author v2")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                put("/api/actor-roles/{roleId}", "actor")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotDeleteActorRoleUnauthorized() throws Exception {
        mvc.perform(delete("/api/actor-roles/{roleId}", "helpdesk"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotCreateActorRoleAsContributor() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("test")
                .label("Test...")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotUpdateActorRoleAsContributor() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("author")
                .label("Author v2")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles/{roleId}", "actor")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotDeleteActorRoleAsContributor() throws Exception {
        mvc.perform(
                delete("/api/actor-roles/{roleId}", "helpdesk")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateActorRoleAsAdministrator() throws Exception {
        ActorRoleCore actorRole = ActorRoleCore.builder()
                .code("test")
                .label("Test v2")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(actorRole);

        mvc.perform(
                post("/api/actor-roles")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());
    }
}
