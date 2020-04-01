package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.ItemComment;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ItemCommentControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldCreateItemComment() throws Exception {
        Integer itemId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/item/{itemId}/comments", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I love it!")));
    }

    @Test
    public void shouldCreateItemCommentWithHtml() throws Exception {
        Integer itemId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("<p>I <b>love</b> it<span>!</span></p>");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/item/{itemId}/comments", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I **love** it!\n")));
    }

    @Test
    public void shouldNotCreateItemCommentWhenCommentIsEmpty() throws Exception {
        Integer itemId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/item/{itemId}/comments", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateItemCommentWhenItemNotExist() throws Exception {
        Integer itemId = 30;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/item/{itemId}/comments", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateItemComment() throws Exception {
        Integer itemId = 1;
        Integer commentId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/item/{itemId}/comments/{id}", itemId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I love it!")));
    }

    @Test
    public void shouldNotUpdateItemCommentWhenNotBelongToItem() throws Exception {
        Integer itemId = 2;
        Integer commentId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/item/{itemId}/comments/{id}", itemId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateItemCommentWhenNotExist() throws Exception {
        Integer itemId = 1;
        Integer commentId = 50;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/item/{itemId}/comments/{id}", itemId, commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteItemComment() throws Exception {
        Integer itemId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("Comment to delete.");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/item/{itemId}/comments", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long commentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, ItemComment.class).getId();

        mvc.perform(delete("/api/item/{itemId}/comments/{id}", itemId, commentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotDeleteItemCommentWhenNotBelongToItem() throws Exception {
        Integer itemId = 2;
        Integer commentId = 2;

        mvc.perform(delete("/api/item/{itemId}/comments/{id}", itemId, commentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotDeleteItemCommentWhenNotExist() throws Exception {
        Integer itemId = 1;
        Integer commentId = 50;

        mvc.perform(delete("/api/item/{itemId}/comments/{id}", itemId, commentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
