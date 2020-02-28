package eu.sshopencloud.marketplace.controllers.vocabularies;

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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConceptControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnAllObjectTypeConceptsForTool() throws Exception {

        mvc.perform(get("/api/object-type-concepts/tool")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("tool")))
                .andExpect(jsonPath("$[0].notation", is("Tool")))
                .andExpect(jsonPath("$[0].vocabulary.code", is("object-type")));
    }

    @Test
    public void shouldReturnAllObjectTypeConceptsForTrainingMaterial() throws Exception {

        mvc.perform(get("/api/object-type-concepts/training-material")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("training-material")))
                .andExpect(jsonPath("$[0].notation", is("Training material")))
                .andExpect(jsonPath("$[0].vocabulary.code", is("object-type")));
    }

    @Test
    public void shouldReturnAllObjectTypeConceptsForDataset() throws Exception {

        mvc.perform(get("/api/object-type-concepts/dataset")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("dataset")))
                .andExpect(jsonPath("$[0].notation", is("Dataset")))
                .andExpect(jsonPath("$[0].vocabulary.code", is("object-type")));
    }

}
