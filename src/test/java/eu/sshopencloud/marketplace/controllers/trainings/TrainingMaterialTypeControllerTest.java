package eu.sshopencloud.marketplace.controllers.trainings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class TrainingMaterialTypeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnAllTrainingMaterialTypes() throws Exception {

        mvc.perform(get("/api/training-material-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].code", is("paper")))
                .andExpect(jsonPath("$[1].code", is("tutorial")))
                .andExpect(jsonPath("$[2].code", is("online-course")))
                .andExpect(jsonPath("$[3].code", is("webinar")))
                .andExpect(jsonPath("$[4].code", is("blog")));
    }

}
