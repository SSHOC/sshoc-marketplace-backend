package eu.sshopencloud.marketplace.controllers.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import org.junit.jupiter.api.MethodOrderer;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains several helper methods used in tests related with collection
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
@Transactional
public abstract class CollectionControllerTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    protected CollectionDto createCollection(CollectionCreationDto dto, String callerCredentials) throws Exception {

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(dto);

        String cratedCollection = mvc.perform(post("/api/collections")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", callerCredentials))
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(cratedCollection, CollectionDto.class);
    }

    protected void removeCollection(long collectionId, String callerCredentials) throws Exception {

        mvc.perform(delete("/api/collections/{collectionId}", collectionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", callerCredentials))
                .andExpect(status().isOk());
    }
}