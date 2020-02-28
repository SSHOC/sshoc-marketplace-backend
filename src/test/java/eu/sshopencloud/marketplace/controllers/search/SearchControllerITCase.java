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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("items[1].id", is(7)))
                .andExpect(jsonPath("items[1].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("items[2].id", is(4)))
                .andExpect(jsonPath("items[2].label", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Paper.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Paper.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Tutorial.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Tutorial.checked", is(false)))
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
                .andExpect(jsonPath("items[0].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("categories.tool.count", is(0)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Tutorial.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Tutorial.checked", is(false)))
                .andExpect(jsonPath("facets.keyword", anEmptyMap()));
    }

    @Test
    public void shouldReturnItemsByKeyword() throws Exception {

        mvc.perform(get("/api/item-search?q=topic modeling")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("items[1].id", is(11)))
                .andExpect(jsonPath("items[1].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Dataset.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=topic modeling&categories=tool,training-material")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
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
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("items[1].id", is(11)))
                .andExpect(jsonPath("items[1].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Dataset.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPhraseAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=\"topic modeling\"&categories=tool,training-material")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(0)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
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
                .andExpect(jsonPath("items[0].id", is(8)))
                .andExpect(jsonPath("items[0].label", is("Webinar on DH")))
                .andExpect(jsonPath("items[1].id", is(2)))
                .andExpect(jsonPath("items[1].label", is("Stata")))
                .andExpect(jsonPath("items[2].id", is(11)))
                .andExpect(jsonPath("items[2].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Dataset.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Webinar.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Webinar.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(false)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPartAndFilterByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=topic&categories=tool,training-material")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].id", is(8)))
                .andExpect(jsonPath("items[0].label", is("Webinar on DH")))
                .andExpect(jsonPath("items[1].id", is(2)))
                .andExpect(jsonPath("items[1].label", is("Stata")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Webinar.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Webinar.checked", is(false)))
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
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("items[1].id", is(11)))
                .andExpect(jsonPath("items[1].label", is("Test dataset with markdown description")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Dataset.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Webinar.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Webinar.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['Lorem ipsum'].checked", is(true)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].count", is(2)))
                .andExpect(jsonPath("facets.keyword.['topic modeling'].checked", is(true)))
                .andExpect(jsonPath("facets.keyword.topic.count", is(1)))
                .andExpect(jsonPath("facets.keyword.topic.checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByKeywordPartAndFilterByCategoriesAndSubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=topic&categories=tool,training-material&f.keyword=Lorem ipsum&f.keyword=topic modeling")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(2)))
                .andExpect(jsonPath("items[0].label", is("Stata")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(1)))
                .andExpect(jsonPath("categories.training-material.checked", is(true)))
                .andExpect(jsonPath("categories.dataset.count", is(1)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Webinar.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Webinar.checked", is(false)))
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
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("items[1].id", is(4)))
                .andExpect(jsonPath("items[1].label", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("items[2].id", is(7)))
                .andExpect(jsonPath("items[2].label", is("Introduction to GEPHI")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Paper.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Paper.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Tutorial.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Tutorial.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByWordAndFilteredByCategories() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&categories=tool,dataset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(true)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(false)));
    }

    @Test
    public void shouldReturnItemsByWordAndFilteredBySubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&f.object-type=Software&f.keyword=social network analysis")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(false)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Paper.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Paper.checked", is(false)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(true)))
                .andExpect(jsonPath("facets.object-type.Tutorial.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Tutorial.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(true)));
    }


    @Test
    public void shouldReturnItemsByWordAndFilteredByCategoriesAndSubfilters() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&categories=tool,dataset&f.object-type=Software&f.keyword=social network analysis")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].label", is("Gephi")))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(true)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(true)))
                .andExpect(jsonPath("facets.keyword.graph.count", is(1)))
                .andExpect(jsonPath("facets.keyword.graph.checked", is(false)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].count", is(1)))
                .andExpect(jsonPath("facets.keyword.['social network analysis'].checked", is(true)));
    }


    @Test
    public void shouldReturnItemsByWordAndFilteredByCategoriesAndSubfiltersWithNonExistentValue() throws Exception {

        mvc.perform(get("/api/item-search?q=gephi&categories=tool,dataset&f.object-type=Software&f.keyword=non_existent_value")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(0)))
                .andExpect(jsonPath("categories.tool.count", is(1)))
                .andExpect(jsonPath("categories.tool.checked", is(true)))
                .andExpect(jsonPath("categories.training-material.count", is(2)))
                .andExpect(jsonPath("categories.training-material.checked", is(false)))
                .andExpect(jsonPath("categories.dataset.count", is(0)))
                .andExpect(jsonPath("categories.dataset.checked", is(true)))
                .andExpect(jsonPath("facets.object-type.Software.count", is(1)))
                .andExpect(jsonPath("facets.object-type.Software.checked", is(true)))
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
                .andExpect(jsonPath("concepts[0].code", is("software")))
                .andExpect(jsonPath("concepts[0].vocabulary.code", is("object-type")))
                .andExpect(jsonPath("concepts[0].label", is("Software")))
                .andExpect(jsonPath("types.object-type.count", is(1)))
                .andExpect(jsonPath("types.object-type.checked", is(true)));
    }

}
