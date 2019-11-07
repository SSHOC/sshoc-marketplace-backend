package eu.sshopencloud.marketplace.controllers.licenses;

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
public class LicenseTypeControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnAllLicenseTypes() throws Exception {

        mvc.perform(get("/api/license-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].code", is("public-domain")))
                .andExpect(jsonPath("$[1].code", is("permissive")))
                .andExpect(jsonPath("$[2].code", is("copyleft")))
                .andExpect(jsonPath("$[3].code", is("free")))
                .andExpect(jsonPath("$[4].code", is("open-source")))
                .andExpect(jsonPath("$[5].code", is("creative-commons")))
                .andExpect(jsonPath("$[6].code", is("freeware")))
                .andExpect(jsonPath("$[7].code", is("shareware")))
                .andExpect(jsonPath("$[8].code", is("closed-source")))
                .andExpect(jsonPath("$[9].code", is("commercial")));
    }

}
