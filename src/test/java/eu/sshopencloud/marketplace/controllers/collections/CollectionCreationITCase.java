package eu.sshopencloud.marketplace.controllers.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.collections.*;
import eu.sshopencloud.marketplace.model.search.IndexItem;
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

import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains creation related tests for SSOMP Collections
 */
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
class CollectionCreationITCase extends CollectionControllerTest {

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
    void shouldNotAllowToCreateCollectionWithEmptyActorAsContributor() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto  contributor = new CollectionCreationContributorDto();
        contributor.setRole(new ActorRoleId("code"));

        collection.getContributors().add(contributor);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        mvc.perform(post("/api/collections")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("errors[0].field", is("contributors[0].actor")))
            .andExpect(jsonPath("errors[0].code", is("field.required")))
            .andExpect(jsonPath("errors[0].message", is("The field actor is required")));
    }

    @Test
    void shouldNotAllowToCreateCollectionWithActorThatDoesNotExist() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto  contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(10L));
        contributor.setRole(new ActorRoleId("code"));

        collection.getContributors().add(contributor);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].actor")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", is("Such actor does not exist")));
    }

    @Test
    void shouldNotAllowToCreateCollectionWithEmptyRoleInContributor() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto  contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(1L));

        collection.getContributors().add(contributor);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role")))
                .andExpect(jsonPath("errors[0].code", is("field.required")))
                .andExpect(jsonPath("errors[0].message", is("The field role is required")));
    }

    @Test
    void shouldNotAllowToCreateCollectionWithRoleThatDoesNotExist() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto  contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(1L));
        contributor.setRole(new ActorRoleId("notExistingRole"));

        collection.getContributors().add(contributor);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        //then
        mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("contributors[0].role")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", is("Such role does not exist")));
    }


    @Test
    void shouldCreateCollectionWithCorrectActorAndRole() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setVisible(true);

        CollectionCreationContributorDto  contributor = new CollectionCreationContributorDto();
        contributor.setActor(new ActorId(1L));
        contributor.setRole(new ActorRoleId("funder"));

        collection.getContributors().add(contributor);

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
                .andExpect(jsonPath("recommended", is(Boolean.valueOf("false"))))
                .andExpect(jsonPath("contributors[0].role.code", is("funder")))
                .andExpect(jsonPath("contributors[0].role.label", is("Funder")))
                .andExpect(jsonPath("contributors[0].actor.id", is(1)))

                .andReturn().getResponse().getContentAsString();

        //cleanup
        CollectionDto createdCollection = mapper.readValue(cratedCollection, CollectionDto.class);
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReindexItemsContainedInCollectionDuringCollectionCreation() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setContainedItems(List.of("vHQEhe", "WfcKvG"));
        collection.setVisible(true);

        //when
        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //then
        for (String containedItem : collection.getContainedItems()) {
            SolrQuery solrQuery = new SolrQuery("persistent_id:\"" + containedItem + "\"");

            QueryResponse results = solrClient.query(IndexItem.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);
            MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(1));
            MatcherAssert.assertThat(results.getResults().getFirst().get(IndexItem.PERSISTENT_ID_FIELD), is(String.valueOf(containedItem)));
            MatcherAssert.assertThat((Collection<String>) results.getResults().getFirst().get(IndexItem.COLLECTIONS_FIELD), hasItems(createdCollection.getId() + ""));
        }
        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }

    @Test
    void shouldReindexItemsContainedInCollectionDuringCollectionUpdate() throws Exception {

        //given
        CollectionCreationDto collection = new CollectionCreationDto();
        collection.setTitle("Simple collection");
        collection.setDescription("Simple collection description");
        collection.setContainedItems(List.of("vHQEhe", "WfcKvG"));
        collection.setVisible(true);

        CollectionDto createdCollection = createCollection(collection, CONTRIBUTOR_JWT);

        //when
        collection.setContainedItems(List.of("2CwYCU"));
        String modifiedPayload = TestJsonMapper.serializingObjectMapper().writeValueAsString(collection);

        mvc.perform(put("/api/collections/{id}", createdCollection.getId())
                        .content(modifiedPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk());

        //then
        SolrQuery solrQuery = new SolrQuery("persistent_id:\"vHQEhe\"");
        QueryResponse results = solrClient.query(IndexItem.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);
        MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(1));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexItem.PERSISTENT_ID_FIELD), is("vHQEhe"));
        MatcherAssert.assertThat((Collection<String>) results.getResults().getFirst().get(IndexItem.COLLECTIONS_FIELD), Matchers.not(hasItem(createdCollection.getId() + "")));

        solrQuery = new SolrQuery("persistent_id:\"WfcKvG\"");
        results = solrClient.query(IndexItem.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);
        MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(1));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexItem.PERSISTENT_ID_FIELD), is("WfcKvG"));
        MatcherAssert.assertThat((Collection<String>) results.getResults().getFirst().get(IndexItem.COLLECTIONS_FIELD), Matchers.not(hasItem(createdCollection.getId() + "")));


        solrQuery = new SolrQuery("persistent_id:\"2CwYCU\"");
        results = solrClient.query(IndexItem.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);
        MatcherAssert.assertThat(results.getResults(), Matchers.hasSize(1));
        MatcherAssert.assertThat(results.getResults().getFirst().get(IndexItem.PERSISTENT_ID_FIELD), is("2CwYCU"));
        MatcherAssert.assertThat((Collection<String>) results.getResults().getFirst().get(IndexItem.COLLECTIONS_FIELD), hasItem(createdCollection.getId() + ""));

        //cleanup
        removeCollection(createdCollection.getId(), CONTRIBUTOR_JWT);
    }
}
