package eu.sshopencloud.marketplace.controllers.search;

import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.controllers.collections.CollectionControllerTest;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains tests for {@link SearchController} related with {@link eu.sshopencloud.marketplace.model.collections.Collection} entities
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Transactional
class CollectionsSearchControllerITCase extends CollectionControllerTest {

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;

    @BeforeEach
    void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
    }

    @Test
    void shouldReturnOnlyPubicCollectionsForAnonymousUser() throws Exception {

        //given
        //create public collection
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Public collection");
        collection.setDescription("Public collection description");
        collection.setVisible(true);

        CollectionDto publicCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //create private collection
        collection = new CollectionCreationDto();
        collection.setTitle("Private collection");
        collection.setDescription("Private collection description");
        collection.setVisible(false);

        CollectionDto privateCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        mvc.perform(get("/api/collection-search?q=*")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(1)))
                .andExpect(jsonPath("collections[0].title", is("Public collection")))
                .andExpect(jsonPath("collections[0].description", is("Public collection description")))
                .andExpect(jsonPath("collections[0].itemsCount", is(0)))
                .andExpect(jsonPath("collections[0].createdAt").exists())
                .andExpect(jsonPath("collections[0].updatedAt").exists());

        //cleanup
        removeCollection(publicCollection.getId(), CONTRIBUTOR_JWT);
        removeCollection(privateCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReturnOnlyPubicCollectionsAndUserOwnedPrivateCollections() throws Exception {

        //given
        //create public collection
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Public collection");
        collection.setDescription("Public collection description");
        collection.setVisible(true);

        CollectionDto publicCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //create private collection
        collection = new CollectionCreationDto();
        collection.setTitle("Private collection");
        collection.setDescription("Private collection description");
        collection.setVisible(false);

        CollectionDto privateCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //create private collection owned by another user
        collection = new CollectionCreationDto();
        collection.setTitle("Moderator's Private collection");
        collection.setDescription("Moderator's Private collection description");
        collection.setVisible(false);

        CollectionDto moderatorsPrivateCollection = createCollection(collection, MODERATOR_JWT);

        //then
        mvc.perform(get("/api/collection-search?q=*")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("collections", Matchers.hasSize(2)))
                .andExpect(jsonPath("collections[0].title", is("Public collection")))
                .andExpect(jsonPath("collections[0].description", is("Public collection description")))
                .andExpect(jsonPath("collections[1].title", is("Private collection")))
                .andExpect(jsonPath("collections[1].description", is("Private collection description")))
                .andExpect(jsonPath("collections[1].itemsCount", is(0)))
                .andExpect(jsonPath("collections[1].createdAt").exists())
                .andExpect(jsonPath("collections[1].updatedAt").exists());

        //cleanup
        removeCollection(publicCollection.getId(), CONTRIBUTOR_JWT);
        removeCollection(privateCollection.getId(), CONTRIBUTOR_JWT);
        removeCollection(moderatorsPrivateCollection.getId(), MODERATOR_JWT);

    }

}
