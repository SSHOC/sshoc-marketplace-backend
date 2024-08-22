package eu.sshopencloud.marketplace.controllers.vocabularies;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.domain.media.MediaTestUtils;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.items.MediaDetailsId;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.util.MediaTestUploadUtils;
import eu.sshopencloud.marketplace.util.VocabularyTestUploadUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class ConceptControllerITCase {

//    @RegisterExtension
//    public static WireMockExtension wireMockExtension = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();
//    @Rule
//    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String contributorJwt;
    private String systemImporterJwt;
    private String moderatorJwt;
    private String administratorJwt;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
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
                                .header("Authorization", moderatorJwt)
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
    public void shouldDeleteAndCreateConcept() throws Exception {
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

        // create new concept

        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("publication-type")))
                .andExpect(jsonPath("$.conceptResults.hits", is(4)))
                .andExpect(jsonPath("$.conceptResults.count", is(4)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(4)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Journal", "Conference", "Article", "Pre-Print")
                        )
                );

        RelatedConceptCore relatedConceptJournal = RelatedConceptCore.builder()
                .code("Journal")
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://purl.org/ontology/bibo/Journal")
                .relation(new ConceptRelationId("narrower"))
                .build();
        RelatedConceptCore relatedConceptConference = RelatedConceptCore.builder()
                .uri("http://purl.org/ontology/bibo/Conference")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("New Candidate")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptJournal, relatedConceptConference))
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
                .andExpect(jsonPath("$.code", is("New Candidate")))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/New Candidate")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Journal")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Conference")));


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
                                containsInRelativeOrder("Journal", "Conference", "Article", "Pre-Print", "New Candidate")
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[3].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(true)));
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


    @Test
    public void shouldNotCreateNewCandidateConceptForClosedVocabulary() throws Exception {

        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile uploadedVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, vocabularyStream
        );

        mvc.perform(
                        VocabularyTestUploadUtils.vocabularyUpload(HttpMethod.POST, uploadedVocabulary, "/api/vocabularies")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")))
                .andExpect(jsonPath("$.closed", is(false)));

        mvc.perform(
                        get("/api/vocabularies/{code}", "iana-mime-type-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.closed", is(false)))
                .andExpect(jsonPath("$.conceptResults.hits", is(3)))
                .andExpect(jsonPath("$.conceptResults.count", is(3)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(3)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInAnyOrder("image/tif", "application/pdff", "video/mpeg4")
                        )
                );

        mvc.perform(
                        put("/api/vocabularies/{code}/close", "iana-mime-type-test")
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")))
                .andExpect(jsonPath("$.closed", is(true)));


        String vocabularyCode = "iana-mime-type-test";

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
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(conceptCore))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    public void shouldQueryConceptWithURLAsCode() throws Exception {
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

        RelatedConceptCore relatedConceptConference = RelatedConceptCore.builder()
                .uri("http://purl.org/ontology/bibo/Conference")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("https%3A%2F%2Fcreativecommons.org%2Flicenses%2Fby%2F4.0%2F")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptConference))
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
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/https%3A%2F%2Fcreativecommons.org%2Flicenses%2Fby%2F4.0%2F")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Conference")));


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
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));

    }


    @Test
    public void shouldMergeConcepts() throws Exception {
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
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(5)));

        ConceptCore conceptCore = ConceptCore.builder()
                .code("code")
                .label("New candidate concept")
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
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/code")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(0)));


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
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));


        ConceptCore mergeConceptCore = ConceptCore.builder()
                .code("Merge")
                .label("New merge")
                .build();

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(mergeConceptCore))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(mergeConceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(mergeConceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/Merge")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(0)));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("publication-type")))
                .andExpect(jsonPath("$.conceptResults.hits", is(7)))
                .andExpect(jsonPath("$.conceptResults.count", is(7)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(7)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode(), mergeConceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts/{code}/merge", vocabularyCode, conceptCore.getCode())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergeConceptCore.getCode())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(conceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/code")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(0)));


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
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));

    }


    @Test
    public void shouldNotMergeConceptsWithDifferentVocabularies() throws Exception {
        String vocabularyCode = "publication-type";
        String mergeVocabularyCode = "nemo-activity-type";

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

        RelatedConceptCore relatedConceptConference = RelatedConceptCore.builder()
                .uri("http://purl.org/ontology/bibo/Conference")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        RelatedConceptCore relatedConceptJournal = RelatedConceptCore.builder()
                .code("Journal")
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://purl.org/ontology/bibo/Journal")
                .relation(new ConceptRelationId("narrower"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("code")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptConference, relatedConceptJournal))
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
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/code")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Conference")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Journal")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")));


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
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));


        ConceptCore mergeConceptCore = ConceptCore.builder()
                .code("Merge")
                .label("New merge")
                .build();

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts", mergeVocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(mergeConceptCore))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(mergeConceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(mergeConceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/Merge")));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", mergeVocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(mergeVocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(165)))
                .andExpect(jsonPath("$.conceptResults.count", is(20)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(20)));

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts/{code}/merge", vocabularyCode, conceptCore.getCode())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergeConceptCore.getCode())
                )
                .andExpect(status().isNotFound());

    }


    @Test
    public void shouldMergeConceptsWithMediaItemReassigned() throws Exception {
        String vocabularyCode = "software-license";

        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("perpage", "50")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(vocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(402)))
                .andExpect(jsonPath("$.conceptResults.count", is(50)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(50)));

        RelatedConceptCore relatedConceptOBSD = RelatedConceptCore.builder()
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://spdx.org/licenses/AAL")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        RelatedConceptCore relatedConceptAAL = RelatedConceptCore.builder()
                .code("0BSD")
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://spdx.org/licenses/0BSD")
                .relation(new ConceptRelationId("narrower"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("code")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptOBSD, relatedConceptAAL))
                .build();

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", administratorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(conceptCore))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://spdx.org/licenses/code")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("AAL")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("0BSD")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(vocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(403)))
                .andExpect(jsonPath("$.conceptResults.count", is(20)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(20)));


        ConceptCore mergeConceptCore = ConceptCore.builder()
                .code("Merge")
                .label("New merge")
                .build();

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", administratorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(mergeConceptCore))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(mergeConceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(mergeConceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://spdx.org/licenses/Merge")));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("perpage", "50")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(vocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(404)))
                .andExpect(jsonPath("$.conceptResults.count", is(50)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(50)));


        UUID seriouscatId = MediaTestUploadUtils.uploadMedia(mvc, mapper, "seriouscat.jpg", contributorJwt);

        ItemMediaCore seriouscat = new ItemMediaCore(new MediaDetailsId(seriouscatId), "Serious Cat", new ConceptId(mergeConceptCore.getCode(), new VocabularyId(vocabularyCode), null));

        mvc.perform(get("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("datasets", hasSize(3)))
                .andExpect(jsonPath("datasets[0].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("datasets[1].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("datasets[2].persistentId", is("dU0BZc")));

        DatasetCore dataset = new DatasetCore();
        dataset.setLabel("A dataset of cats");
        dataset.setDescription("This dataset contains cats");
        dataset.setMedia(List.of(seriouscat));

        String payload = mapper.writeValueAsString(dataset);

        mvc.perform(
                        post("/api/datasets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                                .header("Authorization", administratorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("media", hasSize(1)))
                .andExpect(jsonPath("media[0].info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("media[0].info.category", is("image")))
                .andExpect(jsonPath("media[0].info.filename", is("seriouscat.jpg")))
                .andExpect(jsonPath("media[0].info.mimeType", is("image/jpeg")))
                .andExpect(jsonPath("media[0].info.hasThumbnail", is(true)))
                .andExpect(jsonPath("media[0].caption", is("Serious Cat")))
                .andExpect(jsonPath("media[0].concept.code", is(mergeConceptCore.getCode())))
                .andReturn().getResponse().getContentAsString();

        assertFalse(MediaTestUtils.isMediaTemporary(entityManager, seriouscatId));

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts/{code}/merge", vocabularyCode, conceptCore.getCode())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", administratorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergeConceptCore.getCode())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(conceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://spdx.org/licenses/code")))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("AAL")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("0BSD")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")));


        mvc.perform(get("/api/datasets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("datasets", hasSize(4)))
                .andExpect(jsonPath("datasets[0].persistentId", notNullValue()))
                .andExpect(jsonPath("datasets[0].media", hasSize(1)))
                .andExpect(jsonPath("datasets[0].media[0].info.mediaId", is(seriouscatId.toString())))
                .andExpect(jsonPath("datasets[0].media[0].concept.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("datasets[1].persistentId", is("dmbq4v")))
                .andExpect(jsonPath("datasets[2].persistentId", is("OdKfPc")))
                .andExpect(jsonPath("datasets[3].persistentId", is("dU0BZc")));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("perpage", "50")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(vocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(403)))
                .andExpect(jsonPath("$.conceptResults.count", is(50)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(50)));

    }


    @Test
    public void shouldMergeConceptsWithPropertiesReassigned() throws Exception {
        String vocabularyCode = "nemo-activity-type";
        String conceptCode = "ActivityType-Developing";
        String mergeConceptCode = "ActivityType-Seeking";
        String datasetPersistentId = "OdKfPc";

        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(vocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(164)))
                .andExpect(jsonPath("$.conceptResults.count", is(20)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(20)));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}/concepts/{code}", vocabularyCode, conceptCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("4.4.7.1 Developing")))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Developing")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(2)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Programming")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-Web-Developing")));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}/concepts/{code}", vocabularyCode, mergeConceptCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(mergeConceptCode)))
                .andExpect(jsonPath("$.label", is("Seeking")))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Seeking")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(8)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Browsing")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-Data_Mining")));


        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .header("Authorization", administratorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.code", is(mergeConceptCode)))
                .andExpect(jsonPath("properties[0].concept.vocabulary.code", is(vocabularyCode)));


        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts/{code}/merge", vocabularyCode, conceptCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergeConceptCode)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCode)))
                .andExpect(jsonPath("$.label", is("4.4.7.1 Developing")))
                .andExpect(jsonPath("$.uri", is("http://dcu.gr/ontologies/scholarlyontology/instances/ActivityType-Developing")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(10)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("ActivityType-Programming")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("ActivityType-Web-Developing")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("ActivityType-Browsing")))
                .andExpect(jsonPath("$.relatedConcepts[3].code", is("ActivityType-Data_Mining")));

        mvc.perform(
                        get("/api/datasets/{id}", datasetPersistentId)
                                .header("Authorization", administratorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(datasetPersistentId)))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].concept.code", is(conceptCode)))
                .andExpect(jsonPath("properties[0].concept.vocabulary.code", is(vocabularyCode)));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(vocabularyCode)))
                .andExpect(jsonPath("$.conceptResults.hits", is(163)))
                .andExpect(jsonPath("$.conceptResults.count", is(20)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(20)));

    }

    @Test
    public void shouldMergeConceptsWithConceptRelatedConceptReassigned() throws Exception {
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

        RelatedConceptCore relatedConceptConference = RelatedConceptCore.builder()
                .uri("http://purl.org/ontology/bibo/Conference")
                .relation(new ConceptRelationId("sameAs"))
                .build();

        RelatedConceptCore relatedConceptJournal = RelatedConceptCore.builder()
                .code("Journal")
                .vocabulary(new VocabularyId(vocabularyCode))
                .uri("http://purl.org/ontology/bibo/Journal")
                .relation(new ConceptRelationId("narrower"))
                .build();

        ConceptCore conceptCore = ConceptCore.builder()
                .code("code")
                .label("New candidate concept")
                .relatedConcepts(List.of(relatedConceptConference, relatedConceptJournal))
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
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is("New candidate concept")))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/code")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(2)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Conference")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Journal")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")));


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
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));


        RelatedConceptCore relatedConceptBook = RelatedConceptCore.builder()
                .code("Book")
                .vocabulary(new VocabularyId(vocabularyCode))
                .relation(new ConceptRelationId("related"))
                .build();

        relatedConceptJournal.setRelation(new ConceptRelationId("broader"));

        ConceptCore mergeConceptCore = ConceptCore.builder()
                .code("Merge")
                .label("New merge")
                .relatedConcepts(List.of(relatedConceptJournal, relatedConceptBook))
                .build();

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(mergeConceptCore))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(mergeConceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(mergeConceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/Merge")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(2)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Journal")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("broader")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Book")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("related")));


        mvc.perform(
                        get("/api/vocabularies/{vocabulary-code}", vocabularyCode)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("publication-type")))
                .andExpect(jsonPath("$.conceptResults.hits", is(7)))
                .andExpect(jsonPath("$.conceptResults.count", is(7)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(7)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode(), mergeConceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));

        mvc.perform(
                        post("/api/vocabularies/{vocabulary-code}/concepts/{code}/merge", vocabularyCode, conceptCore.getCode())
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("with", mergeConceptCore.getCode())
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is(conceptCore.getCode())))
                .andExpect(jsonPath("$.label", is(conceptCore.getLabel())))
                .andExpect(jsonPath("$.candidate", is(true)))
                .andExpect(jsonPath("$.uri", is("http://purl.org/ontology/bibo/code")))
                .andExpect(jsonPath("$.relatedConcepts", hasSize(3)))
                .andExpect(jsonPath("$.relatedConcepts[0].code", is("Conference")))
                .andExpect(jsonPath("$.relatedConcepts[0].relation.code", is("sameAs")))
                .andExpect(jsonPath("$.relatedConcepts[1].code", is("Journal")))
                .andExpect(jsonPath("$.relatedConcepts[1].relation.code", is("narrower")))
                .andExpect(jsonPath("$.relatedConcepts[2].code", is("Book")))
                .andExpect(jsonPath("$.relatedConcepts[2].relation.code", is("related")));

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
                                containsInRelativeOrder("Journal", "Book", "Conference", "Article", "Pre-Print", conceptCore.getCode())
                        )
                )
                .andExpect(jsonPath("$.conceptResults.concepts[4].candidate", is(false)))
                .andExpect(jsonPath("$.conceptResults.concepts[5].candidate", is(true)));

    }

}
