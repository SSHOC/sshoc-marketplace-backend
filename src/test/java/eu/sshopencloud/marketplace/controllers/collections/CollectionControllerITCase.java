package eu.sshopencloud.marketplace.controllers.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionSuggestionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionSuggestionStatusActionDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.model.search.IndexCollection;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hamcrest.MatcherAssert;
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

import static org.hamcrest.Matchers.*;
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
class CollectionControllerITCase extends CollectionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;
    @Autowired
    private SolrClient solrClient;


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

    @Test
    void shouldReturnAllPublicCollections() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        mvc.perform(get("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(1)))
                .andExpect(jsonPath("collections[0].title", is("Simple collection")))
                .andExpect(jsonPath("collections[0].description", is("Simple collection description")))
                .andExpect(jsonPath("collections[0].visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("collections[0].collectionItems").doesNotExist());

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnEmptyCollectionsListEvenThoughThereAreSomePrivateCollections() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(false);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        mvc.perform(get("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnEmptyPrivateCollectionsForNonAuthenticatedUser() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        mvc.perform(get("/api/collections?readMode=PRIVATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnOnlyPrivateCollectionsForAuthenticatedUser() throws Exception {

        //given
        CollectionCreationDto publicCollection = new CollectionCreationDto();
        publicCollection.setTitle("Simple collection");
        publicCollection.setDescription("Simple collection description");
        publicCollection.setVisible(true);

        CollectionDto createdPublicCollection = createCollection(publicCollection, CONTRIBUTOR_JWT);


        CollectionCreationDto privateCollection = new CollectionCreationDto();
        privateCollection.setTitle("Simple private collection");
        privateCollection.setDescription("Simple private collection description");
        privateCollection.setVisible(false);

        CollectionDto createdPrivateCollection = createCollection(privateCollection, CONTRIBUTOR_JWT);
        //then
        mvc.perform(get("/api/collections?readMode=PRIVATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(1)))
                .andExpect(jsonPath("collections[0].title", Matchers.is("Simple private collection")));

        //cleanup
        removeCollection(createdPrivateCollection.getId(), CONTRIBUTOR_JWT);
        removeCollection(createdPublicCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnAllCollectionsOwnedByAuthenticatedUser() throws Exception {

        //given
        CollectionCreationDto userPrivateCollection = new CollectionCreationDto();
        userPrivateCollection.setTitle("Simple collection");
        userPrivateCollection.setDescription("Simple collection description");
        userPrivateCollection.setVisible(true);

        CollectionDto createdPrivateCollection = createCollection(userPrivateCollection, CONTRIBUTOR_JWT);


        CollectionCreationDto userPublicCollection = new CollectionCreationDto();
        userPublicCollection.setTitle("Simple public collection");
        userPublicCollection.setDescription("Simple public collection description");
        userPublicCollection.setVisible(false);

        CollectionDto createdPublicCollection = createCollection(userPublicCollection, CONTRIBUTOR_JWT);
        //then
        mvc.perform(get("/api/collections?readMode=OWNED_BY_CALLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(2)))
                .andExpect(jsonPath("collections[*].title", hasItems("Simple public collection", "Simple collection")))
                .andExpect(jsonPath("collections[*].description", hasItems("Simple collection description", "Simple public collection description")));

        //cleanup
        removeCollection(createdPrivateCollection.getId(), CONTRIBUTOR_JWT);
        removeCollection(createdPublicCollection.getId(), CONTRIBUTOR_JWT);
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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        //cleanup
        CollectionDto createdCollection = mapper.readValue(cratedCollection, CollectionDto.class);
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(2)))
                .andExpect(jsonPath("collectionItems.items[0].persistentId", is("vHQEhe")))
                .andExpect(jsonPath("collectionItems.items[1].persistentId", is("WfcKvG")))
                .andReturn().getResponse().getContentAsString();

        //cleanup
        CollectionDto createdCollection = mapper.readValue(cratedCollection, CollectionDto.class);
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldCreateCollectionWithProperty() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection with property");
        collection.setDescription("Simple collection description");
        collection.setContainedItems(List.of("vHQEhe", "WfcKvG"));
        collection.setVisible(true);

        PropertyCore propertyCore = new PropertyCore();
        PropertyTypeId propertyTypeId = new PropertyTypeId();
        propertyTypeId.setCode("keyword");
        propertyCore.setType(propertyTypeId);
        propertyCore.setValue("recommended");
        collection.setProperties(List.of(propertyCore));

        //when
        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection with property")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("properties", Matchers.hasSize(1)))
                .andExpect(jsonPath("properties[0].value", is("recommended")))
                .andExpect(jsonPath("properties[0].type.code", is("keyword")));

        SolrQuery solrQuery = new SolrQuery("id:\"" + createdCollection.getId()+"\"");

        QueryResponse results = solrClient.query(IndexCollection.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);

        MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(1));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexCollection.ID_FIELD), Matchers.is(String.valueOf(createdCollection.getId())));
        MatcherAssert.assertThat(
                (List<String>) results.getResults().getFirst().get("dynamic_property_keyword_ss"),
                Matchers.contains("recommended")
        );

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReadCreatedCollection() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldModifyExistingCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReindexExistingCollectionDuringCollectionModification() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when
        collection.setTitle("Modified collection title");
        collection.setDescription("Modified collection description");
        collection.setVisible(false);
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Modified collection title")))
                .andExpect(jsonPath("description", is("Modified collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("false"))))
                .andReturn().getResponse().getContentAsString();

        //then
        SolrQuery solrQuery = new SolrQuery("id:\"" + createdCollection.getId()+"\"");

        QueryResponse results = solrClient.query(IndexCollection.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);

        MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(1));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexCollection.ID_FIELD), Matchers.is(String.valueOf(createdCollection.getId())));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexCollection.TITLE_FIELD), Matchers.is("Modified collection title"));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexCollection.DESCRIPTION_FIELD), Matchers.is("Modified collection description"));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexCollection.VISIBLE_FIELD), Matchers.is(Boolean.valueOf("false")));


        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldRemoveIndexedCollectionFromSolrDuringCollectionRemoval() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);

        //then
        SolrQuery solrQuery = new SolrQuery("id:\"" + createdCollection.getId()+"\"");
        QueryResponse results = solrClient.query(IndexCollection.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);

        MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(0));
    }

    @Test
    void shouldRemoveCollection() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAddSuggestionToPublicCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems.items[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems.items[0].comment", is("Example comment")));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldNotAllowToAddSuggestionForNonExistingItem() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(false);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);

    }

    @Test
    void shouldGenerateMessageToUserWhileAddingSuggestionToCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
                .andExpect(jsonPath("messages[0].content", is("There is a new suggestion for your collection: Simple collection")));
        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }



    @Test
    void shouldChangeSuggestionStatusToApproved() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems.items[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems.items[0].suggested", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        CollectionDto collectionDto = mapper.readValue(retrievedCollection, CollectionDto.class);

        //when
        CollectionSuggestionStatusActionDto suggestionStatusActionDto = new CollectionSuggestionStatusActionDto();
        suggestionStatusActionDto.setAction(CollectionSuggestionStatusActionDto.ACTION.APPROVE);

        String suggestionPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestionStatusActionDto);

        mvc.perform(post("/api/collections/{id}/suggestions/{suggestionId}", createdCollection.getId(), collectionDto.getCollectionItems().getItems().get(0).getId())
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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems.items[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems.items[0].suggested", is(Boolean.valueOf("false"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldChangeSuggestionStatusToRejected() throws Exception {
        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("collectionItems.items[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("collectionItems.items[0].suggested", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        CollectionDto collectionDto = mapper.readValue(retrievedCollection, CollectionDto.class);

        //when
        CollectionSuggestionStatusActionDto suggestionStatusActionDto = new CollectionSuggestionStatusActionDto();
        suggestionStatusActionDto.setAction(CollectionSuggestionStatusActionDto.ACTION.REJECT);

        String suggestionPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestionStatusActionDto);

        mvc.perform(post("/api/collections/{id}/suggestions/{suggestionId}", createdCollection.getId(), collectionDto.getCollectionItems().getItems().get(0).getId())
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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }
}