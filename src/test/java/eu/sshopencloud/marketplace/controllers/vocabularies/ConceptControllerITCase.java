package eu.sshopencloud.marketplace.controllers.vocabularies;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class ConceptControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String contributorJwt;
    private String moderatorJwt;
    private String administratorJwt;

    @Before
    public void init() throws Exception {
        contributorJwt = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        moderatorJwt = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        administratorJwt = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    public void shouldCreateNewCandidateConcept() throws Exception {
        String vocabularyCode = "tadirah-research-activity";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("tadirah-research-activity")))
                .andExpect(jsonPath("$.conceptResults.hits", is(7)))
                .andExpect(jsonPath("$.conceptResults.count", is(7)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(7)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Capture", "Creation", "Enrichment", "Analysis", "Interpretation", "Storage", "Dissemination")
                        )
                );


        ConceptCore conceptCore = ConceptCore.builder()
                .code("New Candidate")
                .label("New candidate concept")
                .build();

        mvc.perform(
                post("/api/vocabularies/{vocabulary-code}/concepts/", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(conceptCore))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("New Candidate")))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://tadirah.dariah.eu/researchactivity/instances/New Candidate")));
                //.andExpect(jsonPath("$.ord", is(27)))
                //.andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                //.andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("nemo-activity-type", "iana-mime-type")));


        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("tadirah-research-activity")))
                .andExpect(jsonPath("$.conceptResults.hits", is(8)))
                .andExpect(jsonPath("$.conceptResults.count", is(8)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(8)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Capture", "Creation", "Enrichment", "Analysis", "Interpretation", "Storage", "Dissemination", "New Candidate")
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[6].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[7].candidate", is(true)));

    }




}
