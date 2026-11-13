package eu.sshopencloud.marketplace.controllers.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionRecommendedDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionSuggestionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionSuggestionStatusActionDto;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationContributorDto;
import eu.sshopencloud.marketplace.model.search.IndexCollection;
import eu.sshopencloud.marketplace.util.MediaTestUploadUtils;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.collections.Collection;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;


import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static String CONTRIBUTOR_JWT;
    private static String IMPORTER_JWT;
    private static String MODERATOR_JWT;
    private static String ADMINISTRATOR_JWT;
    @Autowired
    private SolrClient solrClient;

    @Autowired
    private EntityManager entityManager;


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
                .andExpect(jsonPath("collectionItems.items", Matchers.hasSize(0)))
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))))
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

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        // This is important because without it we simply get the in-memory cached
        // version and don't actually hit the DB so any key constraints etc. won't
        // have been applied yet so won't have triggered exceptions
        entityManager.flush();
        entityManager.clear();

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
    void shouldNotAllowToModifyExistingCollectionCreatedByAnotherUser() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when
        collection.setTitle("Modified collection title");
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", IMPORTER_JWT))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowUpdateToAddThumbnail() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when
        
        // upload a media file to get a valid mediaId
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);

        // Create a thumbnail using the valid mediaId
        ItemMediaCore thumbnail = new ItemMediaCore();
        thumbnail.setInfo(new MediaDetailsId(seriouscatId));

        // use the same concept as in the VocabularyControllerITCase test, so that we know the concept exists in the database
        thumbnail.setConcept(new ConceptId("Qhull", new VocabularyId("software-license"), null));
        collection.setThumbnail(thumbnail);

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        // we check that the values are as expected in the PUT response, but as the thumbnail
        // isn't part of the response we can't check it here, so we check it in the GET response below
        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        // This is important because without it we simply get the in-memory cached
        // version and don't actually hit the DB so any key constraints etc. won't
        // have been applied yet so won't have triggered exceptions
        entityManager.flush();
        entityManager.clear();

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("thumbnail.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("thumbnail.concept.code", is("Qhull")))
                .andExpect(jsonPath("thumbnail.concept.vocabulary.code", is("software-license")));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowUpdateToShuffleContributorOrder() throws Exception {
        // given - Step 1: Create a brand new, clean payload for initial collection generation
        CollectionCreationDto creationDto = new CollectionCreationDto();
        creationDto.setTitle("Contributor Reordering Test");
        creationDto.setDescription("Testing that shuffling composite key entities does not cause Hibernate clashes");
        creationDto.setVisible(true);

        // First Contributor: Actor 2 as Author
        CollectionCreationContributorDto initialC1 = new CollectionCreationContributorDto();
        initialC1.setActor(new ActorId(2L));
        initialC1.setRole(new ActorRoleId("author"));
        creationDto.getContributors().add(initialC1);        

        // Second Contributor: Actor 1 as Contributor
        CollectionCreationContributorDto initialC2 = new CollectionCreationContributorDto();
        initialC2.setActor(new ActorId(1L));
        initialC2.setRole(new ActorRoleId("contributor"));
        creationDto.getContributors().add(initialC2);


        // create the collection with the initial contributor order: [Actor 2 as Author, Actor 1 as Contributor]
        CollectionDto createdCollectionDto = createCollection(creationDto, CONTRIBUTOR_JWT);

        // Verify initial state order holds as expected
        mvc.perform(get("/api/collections/{id}", createdCollectionDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors[0].role.code", is("author")))
                .andExpect(jsonPath("contributors[1].role.code", is("contributor")));


        creationDto.getContributors().clear(); // Clear the initial contributors list to prepare for update

        // put them in the list in the reverse order
        creationDto.getContributors().add(initialC2); // Actor 1 as Contributor
        creationDto.getContributors().add(initialC1); // Actor 2 as Author

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(creationDto);

        // then - Perform the PUT update request and check the updated order
        mvc.perform(put("/api/collections/{id}", createdCollectionDto.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors", hasSize(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("contributor")))
                .andExpect(jsonPath("contributors[1].role.code", is("author")));

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        // This is important because without it we simply get the in-memory cached
        // version and don't actually hit the DB so any key constraints etc. won't
        // have been applied yet so won't have triggered exceptions
        entityManager.flush();
        entityManager.clear();

        // Verify persistence holds the shuffled sequence using a GET query
        mvc.perform(get("/api/collections/{id}", createdCollectionDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors", hasSize(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("contributor")))
                .andExpect(jsonPath("contributors[1].role.code", is("author")));

        // cleanup
        removeCollection(createdCollectionDto.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowUpdateToChangeThumbnail() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        //when
        
        // upload a media file to get a valid mediaId
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);
        // Create a thumbnail using the valid mediaId
        ItemMediaCore thumbnail = new ItemMediaCore();
        thumbnail.setInfo(new MediaDetailsId(seriouscatId));
        thumbnail.setCaption("Serious Cat");
        collection.setThumbnail(thumbnail);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("thumbnail.mediaId", is(seriouscatId.toString())));

        UUID grumpcatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "grumpycat.png", CONTRIBUTOR_JWT);

        ItemMediaCore updatedThumbnail = new ItemMediaCore();
        updatedThumbnail.setInfo(new MediaDetailsId(grumpcatId));
        updatedThumbnail.setCaption("Grumpy Cat");
        collection.setThumbnail(updatedThumbnail);

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        // we check that the values are as expected in the PUT response, but as the thumbnail
        // isn't part of the response we can't check it here, so we check it in the GET response below
        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        // This is important because without it we simply get the in-memory cached
        // version and don't actually hit the DB so any key constraints etc. won't
        // have been applied yet so won't have triggered exceptions
        entityManager.flush();
        entityManager.clear();

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("thumbnail.mediaId", is(grumpcatId.toString())));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowUpdateToRemoveThumbnail() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        //when
        
        // upload a media file to get a valid mediaId
        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", CONTRIBUTOR_JWT);
        // Create a thumbnail using the valid mediaId
        ItemMediaCore thumbnail = new ItemMediaCore();
        thumbnail.setInfo(new MediaDetailsId(seriouscatId));
        thumbnail.setCaption("Serious Cat");
        collection.setThumbnail(thumbnail);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        // we need to flush here so that the thumbnail is created otherwise
        // when we flush after removing it we get an error because hibernate
        // handles orphan removals before inserts which in this case means trying
        // to remove the thumbnail before we've had a chance to actually add it.
        // (this can only happen in the test — in production the POST and PUT are
        // separate transactions, so the thumbnail row always exists by the time
        // a removal happens)
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("thumbnail.mediaId", is(seriouscatId.toString())));

        collection.setThumbnail(null); //remove thumbnail

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        // we check that the values are as expected in the PUT response, but as the thumbnail
        // isn't part of the response we can't check it here, so we check it in the GET response below
        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))));

        entityManager.flush();
        entityManager.clear();

        // we want to check that the thumbnail has actually been removed from the DB
        // with the orphanRemoval=true property. We do this by explicitly checking
        // that there are no CollectionThumbnails present for this collection we created
        Long count = entityManager.createQuery(
                "select count(t) from CollectionThumbnail t where t.collection.id = :id", Long.class)
                        .setParameter("id", createdCollection.getId())
                        .getSingleResult();
        assertEquals(0, count, "thumbnail row should have been deleted by orphan removal");

        // then double check that the GET works properly now that the thumbnail has
        // gone and that there is no thumbnail in the response
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("thumbnail").doesNotExist());

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowUpdateToSetContributors() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        CollectionCreationContributorDto  contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(1L));
        contributor.setRole(new ActorRoleId("funder"));
        collection.getContributors().add(contributor);

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("contributors[0].role.code", is("funder")))
                .andExpect(jsonPath("contributors[0].role.label", is("Funder")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)));

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        // This is important because without it we simply get the in-memory cached
        // version and don't actually hit the DB so any key constraints etc. won't
        // have been applied yet so won't have triggered exceptions
        entityManager.flush();
        entityManager.clear();

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Simple collection")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andExpect(jsonPath("contributors[0].role.code", is("funder")))
                .andExpect(jsonPath("contributors[0].role.label", is("Funder")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowToModifyExistingCollectionCreatedByAnotherUserByModerator() throws Exception {

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
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Modified collection title")))
                .andExpect(jsonPath("description", is("Simple collection description")))
                .andExpect(jsonPath("visible", is(Boolean.valueOf("true"))))
                .andReturn().getResponse().getContentAsString();

        entityManager.flush();
        entityManager.clear();

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

        entityManager.flush();
        entityManager.clear();

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

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        // This is important because without it we simply get the in-memory cached
        // version and don't actually hit the DB so any key constraints etc. won't
        // have been applied yet so won't have triggered exceptions
        entityManager.flush();
        entityManager.clear();

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

        mvc.perform(post("/api/collections/{id}/suggestions/{suggestionId}", createdCollection.getId(), collectionDto.getCollectionItems().getItems().getFirst().getId())
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

        mvc.perform(post("/api/collections/{id}/suggestions/{suggestionId}", createdCollection.getId(), collectionDto.getCollectionItems().getItems().getFirst().getId())
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

    @Test
    void shouldAllowUpdateToRemoveContributor() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(1L));
        contributor.setRole(new ActorRoleId("funder"));
        collection.getContributors().add(contributor);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - update with an empty list of contributors (equivalent to omitting
        // the field entirely, since both result in an empty incoming list)
        collection.getContributors().clear();
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors", hasSize(0)));

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        entityManager.flush();
        entityManager.clear();

        //then - the contributor row should have been deleted from the database.
        // this is the assertion that exercises orphanRemoval -- without it the row
        // would survive with its collection_id still set
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors", hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowUpdateToReplaceContributor() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(1L));
        contributor.setRole(new ActorRoleId("funder"));
        collection.getContributors().add(contributor);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - swap the contributor for a different actor/role combination
        contributor.setActor(new ActorId(2L));
        contributor.setRole(new ActorRoleId("author"));

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")));

        entityManager.flush();
        entityManager.clear();

        // then - double check that this change was persisted to the database
        // and not just reflected in the in-memory entity
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("contributors", hasSize(1)))
                .andExpect(jsonPath("contributors[0].actor.id", is(2)))
                .andExpect(jsonPath("contributors[0].role.code", is("author")));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldNotAllowToUpdateCollectionWithUnknownMediaId() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - update with a thumbnail referencing a mediaId that does not exist
        ItemMediaCore thumbnail = new ItemMediaCore();
        thumbnail.setInfo(new MediaDetailsId(UUID.randomUUID()));
        collection.setThumbnail(thumbnail);

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("thumbnail.info.mediaId")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", is("The media with the given mediaId does not exist")));

        entityManager.flush();
        entityManager.clear();

        //then - the update should have been rolled back so the collection should
        // still have no thumbnail
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("thumbnail").doesNotExist());

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldNotAllowToUpdateCollectionWithUnknownItem() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - update with a contained item that does not exist
        collection.getContainedItems().add("999999");

        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest());

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldRemoveAllItemsFromCollection() throws Exception {

        //given - a collection containing an item
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);
        collection.getContainedItems().add("vHQEhe");

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collectionItems.items", hasSize(1)));

        //when - update the collection without sending any contained items
        // (an empty list here is equivalent to omitting the field entirely)
        collection.getContainedItems().clear();
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                // this may look odd, but the PUT response never includes the collection
                // items (the response is intentionally kept small)
                .andExpect(jsonPath("collectionItems.items").doesNotExist());

        entityManager.flush();
        entityManager.clear();

        //then
        mvc.perform(get("/api/collections/{id}", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collectionItems.items", hasSize(0)));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }


    @Test
    void shouldReuseExistingCollectionItemsOnUpdate() throws Exception {

        //given - a collection containing an item
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);
        collection.getContainedItems().add("vHQEhe");

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        // capture the identity of the underlying CollectionItem row
        entityManager.flush();
        entityManager.clear();
        Collection before = entityManager.find(Collection.class, createdCollection.getId());
        assertEquals(1, before.getCollectionItems().size());
        Long collectionItemIdBefore = before.getCollectionItems().get(0).getId();

        //when - update the collection keeping the same item in it
        collection.setTitle("Updated collection");
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        //then - the same CollectionItem row should have been re-used rather than
        // deleted and recreated. this is what preserves any comments and suggestion
        // flags attached to the item
        Collection after = entityManager.find(Collection.class, createdCollection.getId());
        assertEquals(1, after.getCollectionItems().size());
        assertEquals(collectionItemIdBefore, after.getCollectionItems().get(0).getId(),
                "existing CollectionItem should be re-used on update so comments/suggestion flags are not lost");

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnNotRecommendedForNewCollection() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then - the GET endpoint is public, so no Authorization header is needed
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowModeratorToSetCollectionAsRecommended() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when
        CollectionRecommendedDto recommendedDto = new CollectionRecommendedDto(true);
        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(recommendedDto);

        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        // Flush the SQL and clear the persistence context so that
        // we can read the updated entity from the database in the next GET request.
        entityManager.flush();
        entityManager.clear();

        //then - the flag should have been persisted to the database
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldAllowModeratorToUnsetCollectionRecommendedStatus() throws Exception {

        //given - a collection which a moderator has marked as recommended
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        CollectionRecommendedDto recommendedDto = new CollectionRecommendedDto(true);
        String recommendedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(recommendedDto);

        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content(recommendedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        entityManager.flush();
        entityManager.clear();

        //then
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        //when - the moderator then unsets the flag
        CollectionRecommendedDto unrecommendedDto = new CollectionRecommendedDto(false);
        String unrecommendedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(unrecommendedDto);

        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content(unrecommendedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))));

        entityManager.flush();
        entityManager.clear();

        //then
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldNotAllowContributorToSetCollectionRecommendedStatus() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - a contributor does not have the MODERATOR authority, so the request
        // is rejected by the security filter chain. The 403 response has no JSON body,
        // so we only assert the status code here.
        CollectionRecommendedDto recommendedDto = new CollectionRecommendedDto(true);
        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(recommendedDto);

        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isForbidden());

        entityManager.flush();
        entityManager.clear();

        //then - the flag should still be false
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldNotAllowNonAuthenticatedUserToSetCollectionRecommendedStatus() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - no Authorization header is sent with the PUT
        CollectionRecommendedDto recommendedDto = new CollectionRecommendedDto(true);
        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(recommendedDto);

        //then
        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnNotFoundWhenReadingRecommendedStatusForNonExistingCollection() throws Exception {

        //then
        mvc.perform(get("/api/collections/{collectionId}/recommended", 999999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenSettingRecommendedStatusForNonExistingCollection() throws Exception {

        //given - the PUT requires the MODERATOR authority, so we authenticate as a
        // moderator in order to reach the controller and get the 404 from the service
        CollectionRecommendedDto recommendedDto = new CollectionRecommendedDto(true);
        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(recommendedDto);

        //then
        mvc.perform(put("/api/collections/{collectionId}/recommended", 999999)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreserveRecommendedStatusWhenCollectionIsUpdated() throws Exception {

        //given - a contributor creates a collection which a moderator marks as recommended
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        CollectionRecommendedDto recommendedDto = new CollectionRecommendedDto(true);
        String recommendedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(recommendedDto);

        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content(recommendedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        //when - the owner updates the collection. The update payload contains no
        // recommended field, as the flag can no longer be set via this endpoint,
        // so the update must leave the existing flag value untouched.
        collection.setTitle("Updated collection");
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is("Updated collection")))
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        entityManager.flush();
        entityManager.clear();

        //then - the flag should still be set after the update
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("true"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnBadRequestWhenRecommendedFlagIsMissingFromPutPayload() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when - the PUT body does not contain the recommended field
        mvc.perform(put("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("recommended")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", is("The recommended flag must be provided")));

        entityManager.flush();
        entityManager.clear();

        //then - the flag should be unchanged
        mvc.perform(get("/api/collections/{collectionId}/recommended", createdCollection.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }
}
