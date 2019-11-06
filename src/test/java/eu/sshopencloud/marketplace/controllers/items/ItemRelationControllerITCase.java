package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemRelationControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnAllItemRelations() throws Exception {

        mvc.perform(get("/api/item-relations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].code", is("relates-to")))
                .andExpect(jsonPath("$[1].code", is("is-related-to")))
                .andExpect(jsonPath("$[2].code", is("documents")))
                .andExpect(jsonPath("$[3].code", is("is-documented-by")))
                .andExpect(jsonPath("$[4].code", is("mentions")))
                .andExpect(jsonPath("$[5].code", is("is-mentioned-in")))
                .andExpect(jsonPath("$[6].code", is("extends")))
                .andExpect(jsonPath("$[7].code", is("is-extended-by")));
    }

    @Test
    public void shouldCreateItemsRelations() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 2;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("subject.id", is(subjectId)))
                .andExpect(jsonPath("object.id", is(objectId)))
                .andExpect(jsonPath("relation.code", is("mentions")))
                .andExpect(jsonPath("relation.label", is("Mentions")));
    }

    @Test
    public void shouldntCreateItemsRelationsWhenRelationIsIncorrect() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 2;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("qwerty");

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateItemsRelationsWhenExists() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 3;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldntCreateItemsRelationsWhenItemNotExist() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 30;

        ItemRelationId itemRelation = new ItemRelationId();
        itemRelation.setCode("mentions");

        mvc.perform(post("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(itemRelation))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteItemsRelations() throws Exception {
        Integer subjectId = 1;
        Integer objectId = 5;

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldntDeleteItemsRelationsWhenItemNotExist() throws Exception {
        Integer subjectId = 2;
        Integer objectId = 40;

        mvc.perform(delete("/api/items-relations/{subjectId}/{objectId}", subjectId, objectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
