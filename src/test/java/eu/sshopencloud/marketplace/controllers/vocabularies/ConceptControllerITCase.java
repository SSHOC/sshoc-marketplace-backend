package eu.sshopencloud.marketplace.controllers.vocabularies;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptCore;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptRelationId;
import eu.sshopencloud.marketplace.dto.vocabularies.RelatedConceptCore;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
                        .contentType(MediaType.APPLICATION_JSON)
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

        RelatedConceptCore relatedConceptCreation = RelatedConceptCore.builder()
                .code("Creation")
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://tadirah.dariah.eu/researchactivity/instances/Creation")
                .relation(new ConceptRelationId("narrower"))
                .build();
        RelatedConceptCore relatedConceptEnrichment = RelatedConceptCore.builder()
                .code("Enrichment")
                .vocabulary(new VocabularyId(vocabularyCode))
                .relation(new ConceptRelationId("related"))
                .build();
        RelatedConceptCore relatedConceptAnalysis = RelatedConceptCore.builder()
                .uri("http://tadirah.dariah.eu/researchactivity/instances/Analysis")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("New Candidate")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptCreation, relatedConceptEnrichment, relatedConceptAnalysis))
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
                .andExpect(jsonPath("$.uri", is("http://tadirah.dariah.eu/researchactivity/instances/New Candidate")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Creation")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Enrichment")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("Analysis")));


        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
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

    @Test
    public void shouldUpdateConcept() throws Exception {
        String vocabularyCode = "nemo-activity-type";
        String conceptCode = "ActivityType-Printing";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("4.4.1 Printing")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Printing")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Producing")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("broader")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-3D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("ActivityType-2D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[2].relation.code", is("narrower")));

        RelatedConceptCore relatedConceptAnalyzing = RelatedConceptCore.builder()
                .uri("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Analyzing")
                .relation(new ConceptRelationId("sameAs"))
                .build();
        RelatedConceptCore relatedConcept3DPrinting = RelatedConceptCore.builder()
                .code("ActivityType-3D_Printing")
                .vocabulary(new VocabularyId(vocabularyCode))
                .relation(new ConceptRelationId("narrower"))
                .build();
        RelatedConceptCore relatedConcept2DPrinting = RelatedConceptCore.builder()
                .code("ActivityType-2D_Printing")
                .vocabulary(new VocabularyId(vocabularyCode))
                .relation(new ConceptRelationId("related"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code(conceptCode)
                .label("Modified label")
                .notation("Modified notation")
                .definition("Modified definition")
                .relatedConcepts(List.of(relatedConceptAnalyzing, relatedConcept3DPrinting, relatedConcept2DPrinting))
                .build();

        mvc.perform(
                put("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(conceptCore))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("Modified label")))
                .andExpect(jsonPath("$.notation", is("Modified notation")))
                .andExpect(jsonPath("$.definition", is("Modified definition")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/" + conceptCode)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Analyzing")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-3D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("ActivityType-2D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[2].relation.code", is("related")));

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("Modified label")))
                .andExpect(jsonPath("$.notation", is("Modified notation")))
                .andExpect(jsonPath("$.definition", is("Modified definition")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/" + conceptCode)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Analyzing")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-3D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("ActivityType-2D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[2].relation.code", is("related")));

    }


    @Test
    public void shouldNotUpdateConceptWithInconsistentUri() throws Exception {
        String vocabularyCode = "nemo-activity-type";
        String conceptCode = "ActivityType-Printing";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("4.4.1 Printing")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Printing")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Producing")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("broader")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-3D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("ActivityType-2D_Printing")))
                .andExpect(jsonPath("$.relatedConcepts[2].relation.code", is("narrower")));

        ConceptCore conceptCore = ConceptCore.builder()
                .code(conceptCode)
                .label("Modified label")
                .notation("Modified notation")
                .definition("Modified definition")
                .uri("http://example.com/")
                .build();

        mvc.perform(
                put("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(conceptCore))
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("errors[0].field", is("uri")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));

    }


    @Test
    public void shouldCommitCandidateConcept() throws Exception {
        String vocabularyCode = "tadirah-research-activity";
        String conceptCode = "New";

        ConceptCore conceptCore = ConceptCore.builder()
                .code(conceptCode)
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
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://tadirah.dariah.eu/researchactivity/instances/New")));

        mvc.perform(
                put("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}/commit", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://tadirah.dariah.eu/researchactivity/instances/New")));

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://tadirah.dariah.eu/researchactivity/instances/New")));

    }

    @Test
    public void shouldDeleteConcept() throws Exception {
        String vocabularyCode = "tadirah-research-activity";
        String conceptCode = "Enrichment";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("3 Enrichment")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://tadirah.dariah.eu/researchactivity/instances/Enrichment")));

        mvc.perform(
                delete("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
        )
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldNotDeleteConceptInUse() throws Exception {
        String vocabularyCode = "tadirah-activity";
        String conceptCode = "25";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("Analysis")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("https://sshoc.poolparty.biz/Vocabularies/tadirah-activities/25")));

        mvc.perform(
                delete("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isBadRequest());

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("Analysis")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("https://sshoc.poolparty.biz/Vocabularies/tadirah-activities/25")));
    }

    @Test
    public void shouldDeleteConceptWithForce() throws Exception {
        String vocabularyCode = "tadirah-activity";
        String conceptCode = "25";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("Analysis")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("https://sshoc.poolparty.biz/Vocabularies/tadirah-activities/25")));

        mvc.perform(
                delete("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .param("force", "true")
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
        )
                .andExpect(status().isNotFound());

    }

}
