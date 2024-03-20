package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.model.items.ItemComment;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class ItemCommentControllerITCase {

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
    public void shouldCreateItemComment() throws Exception {
        String itemPersistentId = "n21Kfc";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I love it!")));
    }

    @Test
    public void shouldCreateItemCommentWithHtml() throws Exception {
        String itemPersistentId = "n21Kfc";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("<p>I <b>love</b> it<span>!</span></p>");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(
                get("/api/items/{itemId}/comments", itemPersistentId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I **love** it!\n")));

        mvc.perform(
                get("/api/items/{itemId}/comments", itemPersistentId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void shouldNotCreateItemCommentWhenCommentIsEmpty() throws Exception {
        String itemPersistentId = "Xgufde";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateItemCommentWhenItemNotExist() throws Exception {
        String itemPersistentId = "xxxxxx7";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateItemCommentForCreator() throws Exception {
        String itemPersistentId = "n21Kfc";
        Integer commentId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I love it!")));
    }

    @Test
    public void shouldUpdateItemCommentForModerator() throws Exception {
        String itemPersistentId = "n21Kfc";
        Integer commentId = 2;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I love it!")));
    }


    @Test
    public void shouldNotUpdateItemCommentForNonCreatorAndNonModerator() throws Exception {
        String itemPersistentId = "n21Kfc";
        Integer commentId = 2;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotUpdateItemCommentWhenNotBelongToItem() throws Exception {
        String itemPersistentId = "DstBL5";
        Integer commentId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateItemCommentWhenNotExist() throws Exception {
        String itemPersistentId = "n21Kfc";
        Integer commentId = -50;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteItemCommentForCreator() throws Exception {
        String itemPersistentId = "n21Kfc";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("Comment to delete.");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(
                get("/api/items/{itemId}/comments", itemPersistentId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        String jsonResponse = mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(
                get("/api/items/{itemId}/comments", itemPersistentId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        Long commentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ItemComment.class).getId();

        mvc.perform(delete("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/items/{itemId}/comments", itemPersistentId)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void shouldDeleteItemCommentForModerator() throws Exception {
        String itemPersistentId = "n21Kfc";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("Comment to delete.");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long commentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ItemComment.class).getId();

        mvc.perform(delete("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotDeleteItemCommentForNonCreatorAndNonModerator() throws Exception {
        String itemPersistentId = "n21Kfc";

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("Comment to delete.");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/items/{itemId}/comments", itemPersistentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long commentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ItemComment.class).getId();

        mvc.perform(delete("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotDeleteItemCommentWhenNotBelongToItem() throws Exception {
        String itemPersistentId = "DstBL5";
        Integer commentId = 2;

        mvc.perform(delete("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotDeleteItemCommentWhenNotExist() throws Exception {
        String itemPersistentId = "n21Kfc";
        Integer commentId = -50;

        mvc.perform(delete("/api/items/{itemId}/comments/{id}", itemPersistentId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isNotFound());
    }

}
