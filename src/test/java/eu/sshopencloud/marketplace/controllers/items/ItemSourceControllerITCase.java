package eu.sshopencloud.marketplace.controllers.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemSourceControllerITCase {

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
    public void shouldReturnAllItemSources() throws Exception {
        mvc.perform(get("/api/item-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("Wikidata")))
                .andExpect(jsonPath("$[1].code", is("GitHub")));
    }

    @Test
    public void shouldCreateItemSource() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("test")
                .label("Test source service")
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/item-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("test")))
                .andExpect(jsonPath("label", is("Test source service")));

        mvc.perform(get("/api/item-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].code", is("Wikidata")))
                .andExpect(jsonPath("$[1].code", is("test")))
                .andExpect(jsonPath("$[1].label", is("Test source service")))
                .andExpect(jsonPath("$[2].code", is("GitHub")));
    }

    @Test
    public void shouldNotCreateItemSourceAtWrongPosition() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("test")
                .label("Test...")
                .ord(50)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/item-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRetrieveItemSource() throws Exception {
        mvc.perform(get("/api/item-sources/GitHub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("GitHub")))
                .andExpect(jsonPath("label", is("GitHub")));
    }

    @Test
    public void shouldUpdateItemSource() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("Wikidata")
                .label("Wikidata v2")
                .ord(2)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                put("/api/item-sources/{sourceId}", "Wikidata")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("Wikidata")))
                .andExpect(jsonPath("label", is("Wikidata v2")));

        mvc.perform(get("/api/item-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("GitHub")))
                .andExpect(jsonPath("$[1].code", is("Wikidata")))
                .andExpect(jsonPath("$[1].label", is("Wikidata v2")));
    }

    @Test
    public void shouldRemoveItemSource() throws Exception {
        mvc.perform(
                delete("/api/item-sources/{sourceId}", "Wikidata")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(get("/api/item-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("GitHub")));
    }

    @Test
    @Ignore
    // TODO when items have the external ids assigned
    public void shouldNotRemoveItemSourceInUse() throws Exception {
        mvc.perform(
                delete("/api/item-sources/{sourceId}", "TODO")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateItemSourceUnauthorized() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("test")
                .label("Test...")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/item-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotUpdateItemSourceUnauthorized() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("GitHub")
                .label("GitHub v2")
                .ord(1)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                put("/api/item-sources/{sourceId}", "GitHub")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotDeleteItemSourceUnauthorized() throws Exception {
        mvc.perform(delete("/api/item-sources/{sourceId}", "Wikidata"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldCreateItemSourceAsAdministrator() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("test")
                .label("Test v2")
                .ord(3)
                .build();

        String payload = mapper.writeValueAsString(itemSource);

        mvc.perform(
                post("/api/item-sources")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());
    }
}
