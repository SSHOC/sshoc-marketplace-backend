package eu.sshopencloud.marketplace.controllers.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import org.apache.solr.client.solrj.SolrClient;
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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Transactional
public class SearchControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SolrClient solrClient;

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;


    @BeforeEach
    public void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
    }

    @Test
    public void shouldReturnAllItems() throws Exception {

        mvc.perform(get("/api/item-search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnItemsByWord() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].lastInfoUpdate", is("2020-08-04T12:29:00Z")))
                .andExpect(jsonPath("items[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("items[1].id", is(7)))
                .andExpect(jsonPath("items[1].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("items[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("items[2].id", is(4)))
                .andExpect(jsonPath("items[2].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("items[2].label", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("categories.workflow.count", is(0)))
                .andExpect(jsonPath("categories.workflow.checked", is(false)))
                .andExpect(jsonPath("categories.step.count", is(0)))
                .andExpect(jsonPath("categories.step.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
    }

    @Test
    public void shouldSearchForProposedItem() throws Exception {
        String datasetId = "dU0BZc";
        int datasetVersionId = 11;

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("Test datset");
        dataset.setDescription("Lorem ipsum dolor sit test...");

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                        put("/api/datasets/{datasetId}", datasetId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetId)))
                .andExpect(jsonPath("id", not(is(datasetVersionId))))
                .andExpect(jsonPath("category", is("dataset")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(dataset.getLabel())))
                .andExpect(jsonPath("description", is(dataset.getDescription())));

        solrClient.commit(IndexItem.COLLECTION_NAME);

        mvc.perform(
                        get("/api/item-search?q=test&d.status=(suggested OR approved)")
                                .header("Authorization", CONTRIBUTOR_JWT)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].id", not(is(datasetVersionId))))
                .andExpect(jsonPath("items[0].persistentId", is(datasetId)))
                .andExpect(jsonPath("items[0].category", is("dataset")))
                .andExpect(jsonPath("items[0].status", is("suggested")))
                .andExpect(jsonPath("items[0].owner", is("Contributor")))
                .andExpect(jsonPath("items[0].label", is(dataset.getLabel())))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("items[0].description", is(dataset.getDescription())))
                .andExpect(jsonPath("items[1].id", is(datasetVersionId)))
                .andExpect(jsonPath("items[1].persistentId", is(datasetId)))
                .andExpect(jsonPath("items[1].category", is("dataset")))
                .andExpect(jsonPath("items[1].status", is("approved")))
                .andExpect(jsonPath("items[1].owner", is("Contributor")))
                .andExpect(jsonPath("items[1].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(0)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(2)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByPhrase() throws Exception {

        mvc.perform(get("/api/item-search?q=\"dummy text ever\"")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(7)))
                .andExpect(jsonPath("items[0].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("items[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("categories.tool-or-service.count", is(0)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword", anEmptyMap()));
    }

    @Test
    public void shouldReturnItemsByKeyword() throws Exception {

        mvc.perform(get("/api/item-search?q=topic modeling")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].persistentId", is("DstBL5")))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("items[1].id", is(11)))
                .andExpect(jsonPath("items[1].persistentId", is("dU0BZc")))
                .andExpect(jsonPath("items[1].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=topic modeling&categories=tool-or-service,training-material")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].persistentId", is("DstBL5")))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPhrase() throws Exception {

        mvc.perform(get("/api/item-search?q=\"topic modeling\"")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[*].id", containsInAnyOrder(11, 2)))
                .andExpect(jsonPath("items[*].persistentId", containsInAnyOrder("dU0BZc", "DstBL5")))
                .andExpect(jsonPath("items[*].label", containsInAnyOrder("Test dataset with markdown description", "Stata")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsWildcardPhrase() throws Exception {

        mvc.perform(get("/api/item-search?q=(topi* OR \"Introduction to GEPHI\")&advanced=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(4)))
                .andExpect(jsonPath("items[*].id", containsInAnyOrder(7, 2, 11, 8)))
                .andExpect(jsonPath("items[*].persistentId", containsInAnyOrder("WfcKvG", "DstBL5", "dU0BZc", "JmBgWa")))
                .andExpect(jsonPath("items[*].label", containsInAnyOrder("Introduction to GEPHI", "Stata", "Test dataset with markdown description", "Webinar on DH")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['topic'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPhraseAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=\"topic modeling\"&categories=tool-or-service,training-material")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].persistentId", is("DstBL5")))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("items[0].properties[*].type.allowedVocabularies[*].code", containsInAnyOrder("nemo-activity-type", "tadirah-activity")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)));
    }


    @Test
    public void shouldReturnItemsByKeywordPart() throws Exception {

        mvc.perform(get("/api/item-search?q=topic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[*].id", containsInAnyOrder(8, 11, 2)))
                .andExpect(jsonPath("items[*].persistentId", containsInAnyOrder("JmBgWa", "dU0BZc", "DstBL5")))
                .andExpect(jsonPath("items[*].label", containsInAnyOrder(
                        "Webinar on DH", "Test dataset with markdown description", "Stata"
                )))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPartAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=topic&categories=tool-or-service,training-material")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].id", is(8)))
                .andExpect(jsonPath("items[0].persistentId", is("JmBgWa")))
                .andExpect(jsonPath("items[0].label", is("Webinar on DH")))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("items[1].id", is(2)))
                .andExpect(jsonPath("items[1].persistentId", is("DstBL5")))
                .andExpect(jsonPath("items[1].label", is("Stata")))
                .andExpect(jsonPath("items[1].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPartAndFilterBySubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=topic&f.keyword=Lorem ipsum&f.keyword=topic modeling")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[*].id", containsInAnyOrder(2, 11)))
                .andExpect(jsonPath("items[*].persistentId", containsInAnyOrder("DstBL5", "dU0BZc")))
                .andExpect(jsonPath("items[*].label", containsInAnyOrder("Test dataset with markdown description", "Stata")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(true)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(true)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPartAndFilterByCategoriesAndSubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=topic&categories=tool-or-service,training-material&f.keyword=Lorem ipsum&f.keyword=topic modeling")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].persistentId", is("DstBL5")))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(true)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(true)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)));
    }


    @Test
    public void shouldReturnItemsByWordAndSortedByLabel() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&order=label")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("items[1].id", is(4)))
                .andExpect(jsonPath("items[1].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("items[1].label", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("items[2].id", is(7)))
                .andExpect(jsonPath("items[2].persistentId", is("WfcKvG")))
                .andExpect(jsonPath("items[2].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByWordAndFilteredByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&categories=tool-or-service,dataset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(true)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByWordAndFilteredBySubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&f.keyword=social network analysis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(true)));
    }


    @Test
    public void shouldReturnItemsByWordAndFilteredByCategoriesAndSubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&categories=tool-or-service,dataset&f.keyword=social network analysis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].persistentId", is("n21Kfc")))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(true)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(true)));
    }


    @Test
    public void shouldReturnItemsByWordAndFilteredByCategoriesAndSubfiltersWithNonExistentValue() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&categories=tool-or-service,dataset&f.keyword=non_existent_value")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(0)))
                .andExpect(jsonPath("categories.tool-or-service.count", is(0)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(true)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
    }


    @Test
    public void shouldReturnItemsByExpressionOnContributors() throws Exception {

        mvc.perform(get("/api/item-search?d.contributor=(+CESSDE~ -*Academy*)")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                //.andExpect(jsonPath("items[0].id", is(10)))
                .andExpect(jsonPath("items[?(@.id==10)].persistentId", contains("OdKfPc")))
                .andExpect(jsonPath("items[?(@.id==10)].label", contains("Consortium of European Social Science Data Archives")))
                //.andExpect(jsonPath("items[1].id", is(4)))
                .andExpect(jsonPath("items[?(@.id==4)].persistentId", contains("heBAGQ")))
                .andExpect(jsonPath("items[?(@.id==4)].label", contains("Gephi: an open source software for exploring and manipulating networks.")))
                //.andExpect(jsonPath("items[2].id", is(8)))
                .andExpect(jsonPath("items[?(@.id==8)].persistentId", contains("JmBgWa")))
                .andExpect(jsonPath("items[?(@.id==8)].label", contains("Webinar on DH")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(0)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("categories.workflow.count", is(0)))
                .andExpect(jsonPath("categories.workflow.checked", is(false)))
                .andExpect(jsonPath("facets.activity.['Seeking'].count", is(1)))
                .andExpect(jsonPath("facets.activity.['Seeking'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)))
                .andExpect(jsonPath("facets.source.['Programming Historian'].count", is(1)))
                .andExpect(jsonPath("facets.source.['Programming Historian'].checked", is(false)));
    }


    @Test
    public void shouldReturnItemsByExpressionOnContributorsAndExpressionOnLanguage() throws Exception {

        mvc.perform(get("/api/item-search?d.contributor=(+CESSDE~ -*Academy*)&d.language=(en?)")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(4)))
                .andExpect(jsonPath("items[0].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("items[0].label", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(0)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("categories.workflow.count", is(0)))
                .andExpect(jsonPath("categories.workflow.checked", is(false)));
    }


    @Test
    public void shouldReturnAllConcepts() throws Exception {

        mvc.perform(get("/api/concept-search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnConceptsByWordAndFilteredByTypes() throws Exception {

        mvc.perform(get("/api/concept-search?q=software&types=object-type,activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("concepts[0].code", is("83")))
                .andExpect(jsonPath("concepts[0].vocabulary.code", is("tadirah-activity")))
                .andExpect(jsonPath("concepts[0].label", is("Software")))
                .andExpect(jsonPath("concepts[0].candidate", is(false)))
                .andExpect(jsonPath("types.activity.count", is(7)))
                .andExpect(jsonPath("types.activity.checked", is(true)))
                .andExpect(jsonPath("facets.candidate.['false'].count", is(7)))
                .andExpect(jsonPath("facets.candidate.['false'].checked", is(false)));
    }


    @Test
    public void shouldNotCrashWhenSearchingItemsForASlash() throws Exception {
        mvc.perform(
                        get("/api/item-search")
                                .param("q", " / ")
                        // .param("advanced", "true")
                )
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotCrashWhenSearchingConceptsForASlash() throws Exception {
        mvc.perform(
                        get("/api/concept-search")
                                .param("q", "teaching / learning")
                )
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnConceptsWithCandidateFacet() throws Exception {

        mvc.perform(get("/api/concept-search?q=new&f.candidate=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("concepts", hasSize(9)))
                .andExpect(jsonPath("facets.candidate.['false'].count", is(9)))
                .andExpect(jsonPath("facets.candidate.['false'].checked", is(true)));

    }

    @Test
    public void shouldReturnAllActors() throws Exception {

        mvc.perform(get("/api/actor-search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnActorsByWebsite() throws Exception {

        mvc.perform(get("/api/actor-search?q=CESSDA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("actors[0].id", is(4)))
                .andExpect(jsonPath("actors[0].name", is("CESSDA")))
                .andExpect(jsonPath("actors[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("actors[0].externalIds", hasSize(0)))
                .andExpect(jsonPath("actors[0].affiliations", hasSize(0)));
    }


    @Test
    public void shouldReturnActorsByDynamicParametersEmail() throws Exception {

        mvc.perform(get("/api/actor-search?d.email=cessda@cessda.eu")
                        .header("Authorization", MODERATOR_JWT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("actors[0].id", is(4)))
                .andExpect(jsonPath("actors[0].email", is("cessda@cessda.eu")))
                .andExpect(jsonPath("actors[0].name", is("CESSDA")))
                .andExpect(jsonPath("actors[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("actors[0].externalIds", hasSize(0)))
                .andExpect(jsonPath("actors[0].affiliations", hasSize(0)));
    }

    @Test
    public void shouldReturnActorsByEmailExpression() throws Exception {

        mvc.perform(get("/api/actor-search?d.email=(*@*)")
                        .header("Authorization", MODERATOR_JWT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(2)))
                .andExpect(jsonPath("actors[0].id", is(4)))
                .andExpect(jsonPath("actors[0].email", is("cessda@cessda.eu")))
                .andExpect(jsonPath("actors[0].name", is("CESSDA")))
                .andExpect(jsonPath("actors[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("actors[0].externalIds", hasSize(0)))
                .andExpect(jsonPath("actors[0].affiliations", hasSize(0)))
                .andExpect(jsonPath("actors[1].id", is(5)))
                .andExpect(jsonPath("actors[1].email", is("john@example.com")))
                .andExpect(jsonPath("actors[1].name", is("John Smith")))
                .andExpect(jsonPath("actors[1].website", is("https://example.com/")))
                .andExpect(jsonPath("actors[1].externalIds", hasSize(0)))
                .andExpect(jsonPath("actors[1].affiliations", hasSize(1)));
    }

    @Test
    public void shouldReturnAutocompleteSuggestionForItems() throws Exception {

        mvc.perform(get("/api/item-search/autocomplete?q=gep")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("phrase", is("gep")))
                .andExpect(jsonPath("suggestions", hasSize(3)))
                //.andExpect(jsonPath("suggestions[0].phrase", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("suggestions[?(@.phrase=='Gephi: an open source software for exploring and manipulating networks.')].persistentId", contains("heBAGQ")))
                //.andExpect(jsonPath("suggestions[1].phrase", is("Gephi")))
                .andExpect(jsonPath("suggestions[?(@.phrase=='Gephi')].persistentId", contains("n21Kfc")))
                //.andExpect(jsonPath("suggestions[2].phrase", is("Introduction to GEPHI")))
                .andExpect(jsonPath("suggestions[?(@.phrase=='Introduction to GEPHI')].persistentId", contains("WfcKvG")));
    }

    @Test
    public void shouldReturnAutocompleteSuggestionWithCategoryForItems() throws Exception {

        mvc.perform(get("/api/item-search/autocomplete?q=gep&category=training-material")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("phrase", is("gep")))
                .andExpect(jsonPath("suggestions", hasSize(2)))
                .andExpect(jsonPath("suggestions[0].phrase", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("suggestions[0].persistentId", is("heBAGQ")))
                .andExpect(jsonPath("suggestions[1].phrase", is("Introduction to GEPHI")))
                .andExpect(jsonPath("suggestions[1].persistentId", is("WfcKvG")));
    }

    public void shouldReturnActorsByWordsCaseInsensitive() throws Exception {

        mvc.perform(get("/api/actor-search?q=project")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("actors[0].id", is(3)))
                .andExpect(jsonPath("actors[0].name", is("SSHOC project consortium")))
                .andExpect(jsonPath("actors[0].website", is("https://sshopencloud.eu/")))
                .andExpect(jsonPath("actors[0].externalIds", hasSize(0)))
                .andExpect(jsonPath("actors[0].affiliations", hasSize(0)));

        mvc.perform(get("/api/actor-search?q=ProJecT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("actors[0].id", is(3)))
                .andExpect(jsonPath("actors[0].name", is("SSHOC project consortium")))
                .andExpect(jsonPath("actors[0].website", is("https://sshopencloud.eu/")))
                .andExpect(jsonPath("actors[0].externalIds", hasSize(0)))
                .andExpect(jsonPath("actors[0].affiliations", hasSize(0)));
    }

    @Test
    public void shouldReturnItemsWithStepsIncluded() throws Exception {

        mvc.perform(get("/api/item-search?q=model&includeSteps=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("step")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(1)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("categories.workflow.count", is(0)))
                .andExpect(jsonPath("categories.workflow.checked", is(false)))
                .andExpect(jsonPath("categories.step.count", is(1)))
                .andExpect(jsonPath("categories.step.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsWithStepsIncludedAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=&includeSteps=true&categories=step")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(11)))
                .andExpect(jsonPath("items[0].category", is("step")))
                .andExpect(jsonPath("categories.tool-or-service.count", is(3)))
                .andExpect(jsonPath("categories.tool-or-service.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(3)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.publication.count", is(0)))
                .andExpect(jsonPath("categories.publication.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(3)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("categories.workflow.count", is(2)))
                .andExpect(jsonPath("categories.workflow.checked", is(false)))
                .andExpect(jsonPath("categories.step.count", is(11)))
                .andExpect(jsonPath("categories.step.checked", is(true)));
    }

    @Test
    public void shouldReturnAutocompleteWithoutSteps() throws Exception {

        mvc.perform(get("/api/item-search/autocomplete?q=dictionary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("suggestions", hasSize(1)))
                .andExpect(jsonPath("suggestions[0].persistentId", is("tqmbGY")));
    }

}
