package eu.sshopencloud.marketplace.controllers.search;

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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
public class SearchControllerITCase {

    @Autowired
    private MockMvc mvc;

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
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
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
    public void shouldReturnItemsByKeywordPhraseAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=\"topic modeling\"&categories=tool-or-service,training-material")
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
                .andExpect(jsonPath("items[1].id", is(2)))
                .andExpect(jsonPath("items[1].persistentId", is("DstBL5")))
                .andExpect(jsonPath("items[1].label", is("Stata")))
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
                .andExpect(jsonPath("types.activity.count", is(7)))
                .andExpect(jsonPath("types.activity.checked", is(true)));
    }
}
