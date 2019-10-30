package eu.sshopencloud.marketplace.controllers.trainings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TrainingMaterialControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnTools() throws Exception {

        mvc.perform(get("/api/training-materials")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
