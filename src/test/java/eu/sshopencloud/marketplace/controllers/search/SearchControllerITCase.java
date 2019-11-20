package eu.sshopencloud.marketplace.controllers.search;

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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnAllItems() throws Exception {

        mvc.perform(get("/api/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnItemsByWord() throws Exception {

        mvc.perform(get("/api/search?q=gephi")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].name", is("Gephi")))
                .andExpect(jsonPath("items[1].id", is(7)))
                .andExpect(jsonPath("items[1].name", is("Introduction to GEPHI")))
                .andExpect(jsonPath("items[2].id", is(4)))
                .andExpect(jsonPath("items[2].name", is("Gephi: an open source software for exploring and manipulating networks.")));
    }

    @Test
    public void shouldReturnItemsByPhrase() throws Exception {

        mvc.perform(get("/api/search?q=\"dummy text ever\"")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(7)))
                .andExpect(jsonPath("items[0].name", is("Introduction to GEPHI")));
    }

    @Test
    public void shouldReturnItemsByWordAndSortedByName() throws Exception {

        mvc.perform(get("/api/search?q=gephi&order=name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].name", is("Gephi")))
                .andExpect(jsonPath("items[1].id", is(4)))
                .andExpect(jsonPath("items[1].name", is("Gephi: an open source software for exploring and manipulating networks.")))
                .andExpect(jsonPath("items[2].id", is(7)))
                .andExpect(jsonPath("items[2].name", is("Introduction to GEPHI")));
    }

    @Test
    public void shouldReturnItemsByWordAndFilteredByCategory() throws Exception {

        mvc.perform(get("/api/search?q=gephi&categories=tool,dataset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].id", is(1)))
                .andExpect(jsonPath("items[0].name", is("Gephi")))
                .andExpect(jsonPath("categories.tool", is(1)))
                .andExpect(jsonPath("categories.training-material", is(2)));
    }

}
