package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.items.ItemCommentCore;
import lombok.extern.slf4j.Slf4j;
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

        mvc.perform(post("/api/item-comments/{itemId}", itemId)
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

        mvc.perform(post("/api/item-comments/{itemId}", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I **love** it!\n")));
    }

    @Test
    public void shouldntCreateItemCommentWhenCommentIsEmpty() throws Exception {
        Integer itemId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/item-comments/{itemId}", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldntCreateItemCommentWhenItemNotExist() throws Exception {
        Integer itemId = 30;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/item-comments/{itemId}", itemId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateItemComment() throws Exception {
        Integer commentId = 1;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/item-comments/{id}", commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("body", is("I love it!")));
    }

    @Test
    public void shouldntUpdateItemCommentWhenNotExist() throws Exception {
        Integer commentId = 50;

        ItemCommentCore itemComment = new ItemCommentCore();
        itemComment.setBody("I love it!");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(itemComment);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/item-comments/{id}", commentId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteItemComment() throws Exception {
        Integer commentId = 2;

        mvc.perform(delete("/api/item-comments/{id}", commentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldntDeleteItemCommentWhenNotExist() throws Exception {
        Integer commentId = 50;

        mvc.perform(delete("/api/item-comments/{id}", commentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
