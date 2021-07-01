package eu.sshopencloud.marketplace.controllers.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdCore;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdId;
import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("GitHub")))
                .andExpect(jsonPath("$[1].ord", is(2)));
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
    public void shouldCreateItemSourceWithoutOrd() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("test")
                .label("Test source service")
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
                .andExpect(jsonPath("label", is("Test source service")))
                .andExpect(jsonPath("ord", is(3)));

        mvc.perform(get("/api/item-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].code", is("Wikidata")))
                .andExpect(jsonPath("$[0].ord", is(1)))
                .andExpect(jsonPath("$[1].code", is("GitHub")))
                .andExpect(jsonPath("$[1].ord", is(2)))
                .andExpect(jsonPath("$[2].code", is("test")))
                .andExpect(jsonPath("$[2].label", is("Test source service")))
                .andExpect(jsonPath("$[2].ord", is(3)));
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
    public void shouldUpdateItemSourceWithoutOrder() throws Exception {
        ItemSourceCore itemSource = ItemSourceCore.builder()
                .code("Wikidata")
                .label("Wikidata test")
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
                .andExpect(jsonPath("label", is("Wikidata test")));


        mvc.perform(get("/api/item-sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("GitHub")))
                .andExpect(jsonPath("$[1].code", is("Wikidata")))
                .andExpect(jsonPath("$[1].label", is("Wikidata test")));


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
    public void shouldNotRemoveItemSourceInUse() throws Exception {
        TrainingMaterialCore trainingMaterial = new TrainingMaterialCore();
        trainingMaterial.setLabel("Imported training material");
        trainingMaterial.setDescription("Test Training Material imported from Wikidata and GitHub");
        trainingMaterial.setExternalIds(
                List.of(
                        new ItemExternalIdCore(new ItemExternalIdId("Wikidata"), "cdefgahc"),
                        new ItemExternalIdCore(new ItemExternalIdId("GitHub"), "code-like-chopin")
                )
        );

        String payload = mapper.writeValueAsString(trainingMaterial);

        mvc.perform(
                post("/api/training-materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("training-material")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("externalIds", hasSize(2)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("Wikidata")))
                .andExpect(jsonPath("externalIds[0].identifier", is("cdefgahc")))
                .andExpect(jsonPath("externalIds[1].identifierService.code", is("GitHub")))
                .andExpect(jsonPath("externalIds[1].identifier", is("code-like-chopin")));

        mvc.perform(
                delete("/api/item-sources/{sourceId}", "Wikidata")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());

        mvc.perform(
                delete("/api/item-sources/{sourceId}", "GitHub")
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
