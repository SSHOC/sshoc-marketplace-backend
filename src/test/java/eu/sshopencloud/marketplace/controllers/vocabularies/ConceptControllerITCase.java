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
    private String systemImporterJwt;
    private String moderatorJwt;
    private String administratorJwt;

    @Before
    public void init() throws Exception {
        contributorJwt = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        systemImporterJwt = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        moderatorJwt = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        administratorJwt = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    public void shouldCreateNewCandidateConcept() throws Exception {
        String vocabularyCode = "publication-type";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("publication-type")))
                .andExpect(jsonPath("$.conceptResults.hits", is(5)))
                .andExpect(jsonPath("$.conceptResults.count", is(5)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(5)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print")
                        )
                );

        RelatedConceptCore relatedConceptJournal = RelatedConceptCore.builder()
                .code("Journal")
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://purl.org/ontology/bibo/Journal")
                .relation(new ConceptRelationId("narrower"))
                .build();
        RelatedConceptCore relatedConceptBook = RelatedConceptCore.builder()
                .code("Book")
                .vocabulary(new VocabularyId(vocabularyCode))
                .relation(new ConceptRelationId("related"))
                .build();
        RelatedConceptCore relatedConceptConference = RelatedConceptCore.builder()
                .uri("http://purl.org/ontology/bibo/Conference")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("New Candidate")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptJournal, relatedConceptBook, relatedConceptConference))
                .build();

        mvc.perform(
                post("/api/vocabularies/{vocabulary-code}/concepts", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", systemImporterJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(conceptCore))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("New Candidate")))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/New Candidate")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Journal")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Book")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("Conference")));


        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("publication-type")))
                .andExpect(jsonPath("$.conceptResults.hits", is(6)))
                .andExpect(jsonPath("$.conceptResults.count", is(6)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(6)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", "New Candidate")
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));

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
        String vocabularyCode = "publication-type";
        String conceptCode = "New";

        ConceptCore conceptCore = ConceptCore.builder()
                .code(conceptCode)
                .label("New candidate concept")
                .definition("test")
                .notation("Test")
                .relatedConcepts(null)
                .build();

        mvc.perform(
                post("/api/vocabularies/{vocabulary-code}/concepts", vocabularyCode)
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
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/New")));

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
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/New")));

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/New")));

    }

    @Test
    public void shouldDeleteConcept() throws Exception {
        String vocabularyCode = "publication-type";
        String conceptCode = "Book";

        mvc.perform(
                get("/api/vocabularies/{vocabulary-code}/concepts/{concept-code}", vocabularyCode, conceptCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("Book")))
                .andExpect(jsonPath("$.candidate", is(false)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/Book")));

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
