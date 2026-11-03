package eu.sshopencloud.marketplace.controllers.skg;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import lombok.extern.slf4j.Slf4j;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
@Transactional
class ProductsControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String ADMINISTRATOR_JWT;

    @BeforeEach
    void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    void shouldReturnEmptyListOfProducts() throws Exception {
        String persistentId = "notExistingIdentifier";

        mvc.perform(get("/skg/products/{id}", persistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$['@context']", hasSize(3)))
                .andExpect(jsonPath("$['@context'][0]",
                        is("https://w3id.org/skg-if/context/skg-if.json")))
                .andExpect(jsonPath("$['@context'][1]",
                        is("https://w3id.org/skg-if/context/1.0.0/skg-if-api.json")))
                .andExpect(jsonPath("$['@graph']", hasSize(0)));
    }

    @Test
    void shouldReturnOneProducts() throws Exception {

        //given
        ToolDto tool = createTool();

        //when
        mvc.perform(get("/skg/products/{id}", tool.getPersistentId())
                        .contentType(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['@context']", hasSize(3)))
                .andExpect(jsonPath("$['@context'][0]",
                        is("https://w3id.org/skg-if/context/skg-if.json")))
                .andExpect(jsonPath("$['@context'][1]",
                        is("https://w3id.org/skg-if/context/1.0.0/skg-if-api.json")))
                .andExpect(jsonPath("$['@graph']", hasSize(1)))
                .andExpect(jsonPath("$['@graph'][0]['local_identifier']", containsString(tool.getPersistentId())))
                .andExpect(jsonPath("$['@graph'][0]['product_type']", is("research software")))
                .andExpect(jsonPath("$['@graph'][0]['entity_type']", is("product")))
                .andExpect(jsonPath("$['@graph'][0]['titles']['none'][0]", is("Test complex software")))
                .andExpect(jsonPath("$['@graph'][0]['abstracts']['none'][0]", is("Lorem ipsum")))
                .andExpect(jsonPath("$['@graph'][0]['contributions']", hasSize(0)))
                .andExpect(jsonPath("$['@graph'][0]['relatedProducts']", hasSize(0)));

        //cleanup
        removeTool(tool.getPersistentId(), ADMINISTRATOR_JWT);
    }

    private ToolDto createTool() throws Exception {
        ToolCore toolCore = new ToolCore();
        toolCore.setLabel("Test complex software");
        toolCore.setDescription("Lorem ipsum");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(toolCore);

        String createdTool = mvc.perform(post("/api/tools-services")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(createdTool, ToolDto.class);
    }

    protected void removeTool(String persistentId, String callerCredentials) throws Exception {

        mvc.perform(delete("/api/tools-services/{toolId}", persistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", callerCredentials))
                .andExpect(status().isOk());
    }

}