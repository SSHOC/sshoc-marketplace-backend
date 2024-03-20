package eu.sshopencloud.marketplace.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.auth.NewPasswordData;
import eu.sshopencloud.marketplace.dto.auth.UserCore;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.model.auth.UserRole;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class UserControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

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
    public void shouldReturnUsers() throws Exception {
        mvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotReturnUsersForUnauthorized() throws Exception {
        mvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotReturnUsersForContributor() throws Exception {
        mvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUser() throws Exception {
        Integer userId = 1;

        mvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(userId)));
    }

    @Test
    public void shouldNotReturnUserWhenNotExist() throws Exception {
        Integer userId = 51;

        mvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateConfigUserAndChangePassword() throws Exception {
        UserCore user = new UserCore();
        String username = "New Config";
        user.setUsername(username);
        user.setDisplayName("New Config User");
        user.setEmail("test@example.com");
        user.setRole(UserRole.SYSTEM_MODERATOR);
        String password = "qwerty";
        user.setPassword(password);

        String payload = mapper.writeValueAsString(user);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/users")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username", is(username)))
                .andExpect(jsonPath("displayName", is("New Config User")))
                .andExpect(jsonPath("email", is("test@example.com")))
                .andExpect(jsonPath("config", is(true)))
                .andExpect(jsonPath("status", is("enabled")))
                .andExpect(jsonPath("role", is("system-moderator")))
                .andReturn().getResponse().getContentAsString();

        Long userId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, UserDto.class).getId();

        String jwt = LogInTestClient.getJwt(mvc, username, password);
        assertThat(jwt, not(blankOrNullString()));

        NewPasswordData newPasswordData = new NewPasswordData();
        newPasswordData.setCurrentPassword(password);
        String newPassword = "q1w2e3r4";
        newPasswordData.setNewPassword(newPassword);
        newPasswordData.setVerifiedPassword(newPassword);

        payload = mapper.writeValueAsString(newPasswordData);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/users/{id}/password", userId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username", is(username)));

        jwt = LogInTestClient.getJwt(mvc, username, password);
        assertThat(jwt, blankOrNullString());

        jwt = LogInTestClient.getJwt(mvc, username, newPassword);
        assertThat(jwt, not(blankOrNullString()));
    }

    @Test
    public void shouldCreateConfigContributorUserWhenRoleNotSpecified() throws Exception {
        UserCore user = new UserCore();
        user.setUsername("New Config");
        user.setDisplayName("New Config User");
        user.setEmail("test@example.com");
        user.setPassword("qwerty");

        String payload = mapper.writeValueAsString(user);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/users")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username", is(user.getUsername())))
                .andExpect(jsonPath("displayName", is(user.getDisplayName())))
                .andExpect(jsonPath("email", is(user.getEmail())))
                .andExpect(jsonPath("config", is(true)))
                .andExpect(jsonPath("status", is("enabled")))
                .andExpect(jsonPath("role", is("contributor")));
    }

    @Test
    public void shouldNotCreateConfigUserForModerator() throws Exception {
        UserCore user = new UserCore();
        user.setUsername("New Config");
        user.setDisplayName("New Config User");
        user.setEmail("test@example.com");
        user.setRole(UserRole.SYSTEM_MODERATOR);
        user.setPassword("qwerty");

        String payload = mapper.writeValueAsString(user);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/users")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUsersSortedByLabel() throws Exception {

        mvc.perform(get("/api/users?order=username")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(5)))
                .andExpect(jsonPath("users[0].username", is("Administrator")))
                .andExpect(jsonPath("users[1].username", is("Contributor")))
                .andExpect(jsonPath("users[2].username", is("Moderator")))
                .andExpect(jsonPath("users[3].username", is("System importer")))
                .andExpect(jsonPath("users[4].username", is("System moderator")));
    }

    @Test
    public void shouldReturnUsersSortedByRegistrationDate() throws Exception {

        mvc.perform(get("/api/users?order=date")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(5)))
                .andExpect(jsonPath("users[0].username", is("System moderator")))
                .andExpect(jsonPath("users[0].registrationDate", is("2021-09-03T13:37:00+0200")))
                .andExpect(jsonPath("users[1].username", is("System importer")))
                .andExpect(jsonPath("users[1].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("users[2].username", is("Contributor")))
                .andExpect(jsonPath("users[2].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("users[3].username", is("Moderator")))
                .andExpect(jsonPath("users[3].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("users[4].username", is("Administrator")))
                .andExpect(jsonPath("users[4].registrationDate", is("2020-08-04T12:29:00+0200")));
    }

}
