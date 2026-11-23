package eu.sshopencloud.marketplace.controllers.skg;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
class PersonsControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Test
    void shouldReturnListOfPredefinedPersons() throws Exception {

        //when
        mvc.perform(get("/skg/persons")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['@context']", hasSize(3)))
                .andExpect(jsonPath("$['@context'][0]",
                        is("https://w3id.org/skg-if/context/skg-if.json")))
                .andExpect(jsonPath("$['@context'][1]",
                        is("https://w3id.org/skg-if/context/1.0.0/skg-if-api.json")))
                .andExpect(jsonPath("$['@context'][2]['@base']").exists())

                .andExpect(jsonPath("$['meta']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=1&page_size=10")))
                .andExpect(jsonPath("$['meta']['entity_type']", is("search_result_page")))
                .andExpect(jsonPath("$['meta']['prev_page']").doesNotExist())
                .andExpect(jsonPath("$['meta']['next_page']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=2&page_size=10")))
                .andExpect(jsonPath("$['meta']['next_page']['entity_type']", is("search_result_page")))
                .andExpect(jsonPath("$['meta']['last_page']").doesNotExist())
                .andExpect(jsonPath("$['meta']['part_of']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons")))
                .andExpect(jsonPath("$['meta']['part_of']['entity_type']", is("search_result")))
                .andExpect(jsonPath("$['meta']['part_of']['total_items']", is(5)))
                .andExpect(jsonPath("$['meta']['part_of']['first_page']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=1&page_size=10")))
                .andExpect(jsonPath("$['meta']['part_of']['first_page']['entity_type']", is("search_result_page")))
                .andExpect(jsonPath("$['meta']['part_of']['last_page']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=1&page_size=10")))
                .andExpect(jsonPath("$['meta']['part_of']['last_page']['entity_type']", is("search_result_page")))

                .andExpect(jsonPath("$['@graph']", hasSize(5)))

                .andExpect(jsonPath("$['@graph'][0]['local_identifier']", is("1")))
                .andExpect(jsonPath("$['@graph'][0]['entity_type']", is("person")))
                .andExpect(jsonPath("$['@graph'][0]['name']", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("$['@graph'][0]['website']", is("https://www.oeaw.ac.at/")))
                .andExpect(jsonPath("$['@graph'][0]['identifiers']", hasSize(0)))
                .andExpect(jsonPath("$['@graph'][0]['affiliations']", hasSize(0)))

                .andExpect(jsonPath("$['@graph'][1]['local_identifier']", is("4")))
                .andExpect(jsonPath("$['@graph'][1]['entity_type']", is("person")))
                .andExpect(jsonPath("$['@graph'][1]['name']", is("CESSDA")))
                .andExpect(jsonPath("$['@graph'][1]['website']", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("$['@graph'][1]['affiliations']", hasSize(0)))

                .andExpect(jsonPath("$['@graph'][2]['local_identifier']", is("2")))
                .andExpect(jsonPath("$['@graph'][2]['entity_type']", is("person")))
                .andExpect(jsonPath("$['@graph'][2]['name']", is("Gertrude Stein")))
                .andExpect(jsonPath("$['@graph'][2]['website']").doesNotExist())
                .andExpect(jsonPath("$['@graph'][2]['affiliations']", hasSize(0)))

                .andExpect(jsonPath("$['@graph'][3]['local_identifier']", is("5")))
                .andExpect(jsonPath("$['@graph'][3]['entity_type']", is("person")))
                .andExpect(jsonPath("$['@graph'][3]['name']", is("John Smith")))
                .andExpect(jsonPath("$['@graph'][3]['website']", is("https://example.com/")))
                .andExpect(jsonPath("$['@graph'][3]['affiliations']", hasSize(1)))
                .andExpect(jsonPath("$['@graph'][3]['affiliations'][0]['affiliation']", containsString("/actors/3")))

                .andExpect(jsonPath("$['@graph'][4]['local_identifier']", is("3")))
                .andExpect(jsonPath("$['@graph'][4]['entity_type']", is("person")))
                .andExpect(jsonPath("$['@graph'][4]['name']", is("SSHOC project consortium")))
                .andExpect(jsonPath("$['@graph'][4]['website']", is("https://sshopencloud.eu/")))
                .andExpect(jsonPath("$['@graph'][4]['affiliations']", hasSize(0)));
    }

    @Test
    void shouldReturnSecondPageOfPersons() throws Exception {

        //when
        mvc.perform(get("/skg/persons")
                        .param("page", "2")
                        .param("page_size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['meta']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=2&page_size=2")))
                .andExpect(jsonPath("$['meta']['prev_page']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=1&page_size=2")))
                .andExpect(jsonPath("$['meta']['next_page']['local_identifier']", is("https://marketplace-api.sshopencloud.eu/skg/persons?page=3&page_size=2")))
                .andExpect(jsonPath("$['meta']['part_of']['total_items']", is(5)))
                .andExpect(jsonPath("$['@graph']", hasSize(2)))
                .andExpect(jsonPath("$['@graph'][0]['name']", is("Gertrude Stein")))
                .andExpect(jsonPath("$['@graph'][1]['name']", is("John Smith")));
    }

    @Test
    void shouldReturnErrorWhenPageSizeTooLarge() throws Exception {

        //when
        mvc.perform(get("/skg/persons")
                        .param("page", "1")
                        .param("page_size", "1000000")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

}