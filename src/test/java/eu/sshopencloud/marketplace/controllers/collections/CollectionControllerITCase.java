package eu.sshopencloud.marketplace.controllers.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionSuggestionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionSuggestionStatusActionDto;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
class CollectionControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;


    @BeforeEach
    void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    void shouldReturnEmptyCollectionsList() throws Exception {

        mvc.perform(get("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(0)));
    }

    private CollectionDto createCollection(CollectionCreationDto dto) throws Exception {

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dto);

        String cratedCollection = mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(cratedCollection, CollectionDto.class);
    }

    private void removeCollection(long collectionId) throws Exception {

        mvc.perform(delete("/api/collections/{collectionId}", collectionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAllPublicCollections() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //then
        mvc.perform(get("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(1)))
                .andExpect(jsonPath("collections[0].title", is("Simple collection")))
                .andExpect(jsonPath("collections[0].description", is("Simple collection description")))
                .andExpect(jsonPath("collections[0].visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("collections[0].collectionItems", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldReturnEmptyCollectionsListEvenThoughThereAreSomePrivateCollections() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(false);

        CollectionDto createdCollection = createCollection(collection);

        //then
        mvc.perform(get("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldReturnEmptyPrivateCollectionsForNonAuthenticatedUser() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //then
        mvc.perform(get("/api/collections?private=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldReturnOnlyPrivateCollectionsForAuthenticatedUser() throws Exception {

        //given
        CollectionCreationDto publicCollection = new CollectionCreationDto();
        publicCollection.setTitle("Simple collection");
        publicCollection.setDescription("Simple collection description");
        publicCollection.setVisible(true);

        CollectionDto createdPublicCollection = createCollection(publicCollection);


        CollectionCreationDto privateCollection = new CollectionCreationDto();
        privateCollection.setTitle("Simple  private collection");
        privateCollection.setDescription("Simple private collection description");
        privateCollection.setVisible(false);

        CollectionDto createdPrivateCollection = createCollection(privateCollection);

        //then
        mvc.perform(get("/api/collections?private=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(1)));

        //cleanup
        removeCollection(createdPrivateCollection.getId());
        removeCollection(createdPublicCollection.getId());
    }

    @Test
    void shouldNotAllowToCreateCollectionForNonAuthenticatedUser() throws Exception {
        mvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateCollectionWithoutItems() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        String cratedCollection = mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        //cleanup
        CollectionDto createdCollection = mapper.readValue(cratedCollection, CollectionDto.class);
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldNotAllowToCreateCollectionWithNonExistingItems() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setContainedItems(List.of("abc","abc"));
        collection.setVisible(true);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateCollectionWithItems() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setContainedItems(List.of("vHQEhe", "WfcKvG"));
        collection.setVisible(true);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        String cratedCollection = mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(2)))
                .andExpect(jsonPath("collectionItems[0].persistentId", is("vHQEhe")))
                .andExpect(jsonPath("collectionItems[1].persistentId", is("WfcKvG")))
                .andReturn().getResponse().getContentAsString();

        //cleanup
        CollectionDto createdCollection = mapper.readValue(cratedCollection, CollectionDto.class);
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldReadCreatedCollection() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldModifyExistingCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //when
        collection.setTitle("Modified collection title");
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Modified collection title")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Modified collection title")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        //cleanup
        removeCollection(createdCollection.getId());
    }


    @Test
    void shouldRemoveCollection() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //when
        mvc.perform(delete("/api/collections/{collectionId}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldConvertCollectionFromPrivateToPublic() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(false);

        CollectionDto createdCollection = createCollection(collection);

        //when
        collection.setVisible(true);
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldAddSuggestionToPublicCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //when
        CollectionSuggestionCreationDto collectionSuggestionCreationDto = new CollectionSuggestionCreationDto();
        collectionSuggestionCreationDto.setComment("Example comment");
        collectionSuggestionCreationDto.setItemPersistentId("WfcKvG");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collectionSuggestionCreationDto);

        mvc.perform(post("/api/collections/{id}/suggestions", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems[0].comment", is("Example comment")));
        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldNotAllowToAddSuggestionForNonExistingItem() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(false);

        CollectionDto createdCollection = createCollection(collection);

        //when
        CollectionSuggestionCreationDto collectionSuggestionCreationDto = new CollectionSuggestionCreationDto();
        collectionSuggestionCreationDto.setComment("Example comment");
        collectionSuggestionCreationDto.setItemPersistentId("abc");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collectionSuggestionCreationDto);

        mvc.perform(post("/api/collections/{id}/suggestions", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest());

        //cleanup
        removeCollection(createdCollection.getId());

    }

    @Test
    void shouldGenerateMessageToUserWhileAddingSuggestionToCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        //when
        CollectionSuggestionCreationDto collectionSuggestionCreationDto = new CollectionSuggestionCreationDto();
        collectionSuggestionCreationDto.setComment("Example comment");
        collectionSuggestionCreationDto.setItemPersistentId("WfcKvG");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collectionSuggestionCreationDto);

        mvc.perform(post("/api/collections/{id}/suggestions", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        //then
        mvc.perform(get("/api/inbox")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())

                .andExpect(jsonPath("messages", Matchers.hasSize(1)))
                .andExpect(jsonPath("messages[0].content", is("New collection suggestion received")));
        //cleanup
        removeCollection(createdCollection.getId());
    }



    @Test
    void shouldChangeSuggestionStatusToApproved() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        CollectionSuggestionCreationDto collectionSuggestionCreationDto = new CollectionSuggestionCreationDto();
        collectionSuggestionCreationDto.setComment("Example comment");
        collectionSuggestionCreationDto.setItemPersistentId("WfcKvG");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collectionSuggestionCreationDto);

        mvc.perform(post("/api/collections/{id}/suggestions", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        String retrievedCollection = mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems[0].suggested", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        CollectionDto collectionDto = mapper.readValue(retrievedCollection, CollectionDto.class);

        //when
        CollectionSuggestionStatusActionDto suggestionStatusActionDto = new CollectionSuggestionStatusActionDto();
        suggestionStatusActionDto.setAction(CollectionSuggestionStatusActionDto.ACTION.APPROVE);

        String suggestionPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestionStatusActionDto);

        mvc.perform(post("/api/collections/{id}/suggestions/{suggestionId}", createdCollection.getId(), collectionDto.getCollectionItems().get(0).getId())
                        .content(suggestionPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems[0].suggested", is(Boolean.valueOf("false"))));

        //cleanup
        removeCollection(createdCollection.getId());
    }

    @Test
    void shouldChangeSuggestionStatusToRejected() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection);

        CollectionSuggestionCreationDto collectionSuggestionCreationDto = new CollectionSuggestionCreationDto();
        collectionSuggestionCreationDto.setComment("Example comment");
        collectionSuggestionCreationDto.setItemPersistentId("WfcKvG");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collectionSuggestionCreationDto);

        mvc.perform(post("/api/collections/{id}/suggestions", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        String retrievedCollection = mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems[0].suggested", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        CollectionDto collectionDto = mapper.readValue(retrievedCollection, CollectionDto.class);

        //when
        CollectionSuggestionStatusActionDto suggestionStatusActionDto = new CollectionSuggestionStatusActionDto();
        suggestionStatusActionDto.setAction(CollectionSuggestionStatusActionDto.ACTION.REJECT);

        String suggestionPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestionStatusActionDto);

        mvc.perform(post("/api/collections/{id}/suggestions/{suggestionId}", createdCollection.getId(), collectionDto.getCollectionItems().get(0).getId())
                        .content(suggestionPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collectionItems", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId());
    }
}