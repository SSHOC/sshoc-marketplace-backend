package eu.sshopencloud.marketplace.controllers.vocabularies;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
public class VocabularyControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestEntityManager entityManager;

    private String contributorJwt;
    private String moderatorJwt;

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void init() throws Exception {
        contributorJwt = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        moderatorJwt = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
    }

    @Test
    public void shouldReturnVocabularies() throws Exception {

        mvc.perform(get("/api/vocabularies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hits", is(9)))
                .andExpect(jsonPath("$.count", is(9)))
                .andExpect(
                        jsonPath(
                                "$.vocabularies[*].code",
                                containsInAnyOrder(
                                        "tadirah-research-activity",
                                        "iso-639-3-v2",
                                        "iana-mime-type",
                                        "iso-639-3",
                                        "nemo-activity-type",
                                        "tadirah-activity",
                                        "software-license",
                                        "tadirah-research-technique",
                                        "publication-type"
                                )
                        )
                );
    }

    @Test
    public void shouldNotReturnNonexistentVocabulary() throws Exception {

        mvc.perform(get("/api/vocabularies/non-existent-code")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateNewVocabulary() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile uploadedVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, vocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, uploadedVocabulary, "/api/vocabularies")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")));

        mvc.perform(
                        get("/api/vocabularies/{code}", "iana-mime-type-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.conceptResults.hits", is(3)))
                .andExpect(jsonPath("$.conceptResults.count", is(3)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(3)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInAnyOrder("image/tif", "application/pdff", "video/mpeg4")
                        )
                );
    }

    @Test
    public void shouldCreateNewMultilingualVocabulary() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/sshoc-keyword-test.ttl");

        MockMultipartFile uploadedVocabulary = new MockMultipartFile(
                "ttl", "sshoc-keyword-test.ttl", null, vocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, uploadedVocabulary, "/api/vocabularies")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("sshoc-keyword-test")))
                .andExpect(jsonPath("$.label", is("Keywords from SSHOC MP")));

        mvc.perform(
                        get("/api/vocabularies/{code}", "sshoc-keyword-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("sshoc-keyword-test")))
                .andExpect(jsonPath("$.label", is("Keywords from SSHOC MP")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.conceptResults.hits", is(4)))
                .andExpect(jsonPath("$.conceptResults.count", is(4)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(4)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInAnyOrder("1-grams", "18th-century", "18th-century-literature", "zip")
                        )
                );
    }

    @Test
    public void shouldUpdateVocabularyWithNoAssociatedProperties() throws Exception {
        InputStream newVocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, newVocabularyStream
        );


        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, newVocabulary, "/api/vocabularies")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")));

        InputStream updateVocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-v2-test.ttl");

        MockMultipartFile updatedVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, updateVocabularyStream
        );


        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, updatedVocabulary, "/api/vocabularies/{code}", "iana-mime-type-test")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type v2")));

        mvc.perform(
                        get("/api/vocabularies/{code}", "iana-mime-type-test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type v2")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.conceptResults.hits", is(2)))
                .andExpect(jsonPath("$.conceptResults.count", is(2)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(2)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInAnyOrder("audio/mpeg3", "application/pdff")
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].definition",
                                containsInAnyOrder("MPEG-1 Audio Layer III", "Portable Document File Format (PDF)")
                        )
                );
    }

    @Test
    public void shouldNotUpdateNonexistentVocabulary() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/non-existent-code.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "non-existent-code.ttl", null, vocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, newVocabulary, "/api/vocabularies/{code}", "non-existent-code")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotUpdateVocabularyWithRemovalOfPropertiesInUse() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iso-639-3-test-missing-eng.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "iso-639-3.ttl", null, vocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, newVocabulary, "/api/vocabularies/{code}", "iso-639-3")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/vocabularies/{code}", "iso-639-3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iso-639-3")));
    }

    @Test
    public void shouldUpdateVocabularyWithRemovalOfPropertiesWithForce() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iso-639-3-test-missing-eng.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "iso-639-3.ttl", null, vocabularyStream
        );

        mvc.perform(get("/api/training-materials/{id}", "heBAGQ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.properties[?(@.concept.code == \"eng\")]").exists());

        entityManager.clear();

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, newVocabulary, "/api/vocabularies/{code}", "iso-639-3")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("force", "true")
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk());

        mvc.perform(get("/api/vocabularies/{code}", "iso-639-3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("iso-639-3")));

        mvc.perform(get("/api/training-materials/{id}", "heBAGQ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.properties[?(@.concept.code == \"eng\")]").doesNotExist());
    }

    @Test
    public void shouldPreventVocabularyUpdateOnFilenameMismatch() throws Exception {
        InputStream newVocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, newVocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, newVocabulary, "/api/vocabularies")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")));

        InputStream updateVocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile updatedVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-v2.ttl", null, updateVocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, updatedVocabulary, "/api/vocabularies/iana-mime-type-test")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRemoveVocabularyAndConceptsWithAssociatedPropertiesWithForce() throws Exception {
        mvc.perform(
                        delete("/api/vocabularies/{code}", "iana-mime-type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("force", "true")
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk());

        mvc.perform(
                        get("/api/vocabularies/{code}", "iana-mime-type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        mvc.perform(
                        get("/api/training-materials/{id}", "WfcKvG")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.properties[?(@.concept.code == \"video/mp4\")]").doesNotExist());
    }

    @Test
    public void shouldNotRemoveVocabularyWithPropertiesInUse() throws Exception {
        mvc.perform(
                        delete("/api/vocabularies/{code}", "iana-mime-type")
                                .header("Authorization", moderatorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        mvc.perform(
                        get("/api/vocabularies/{code}", "iana-mime-type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        mvc.perform(
                        get("/api/training-materials/{id}", "WfcKvG")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.properties[?(@.concept.code == \"video/mp4\")]").exists());
    }

    @Test
    public void shouldNotCreateVocabularyUnauthorized() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile vocabularyFile = new MockMultipartFile("ttl", "iana-mime-type-test.ttl", null, vocabularyStream);

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, vocabularyFile, "/api/vocabularies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, vocabularyFile, "/api/vocabularies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", contributorJwt)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotUpdateVocabularyUnauthorized() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile vocabularyFile = new MockMultipartFile("ttl", "iana-mime-type.ttl", null, vocabularyStream);

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, vocabularyFile, "/api/vocabularies/{code}", "iana-mime-type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, vocabularyFile, "/api/vocabularies/{code}", "iana-mime-type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", contributorJwt)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotDeleteVocabularyUnauthorized() throws Exception {
        mvc.perform(delete("/api/vocabularies/iana-mime-type")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(
                        delete("/api/vocabularies/iana-mime-type")
                                .header("Authorization", contributorJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotDeleteNonexistentVocabulary() throws Exception {
        mvc.perform(delete("/api/vocabularies/non-existent-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt))
                .andExpect(status().isNotFound());
    }

    private MockHttpServletRequestBuilder vocabularyUpload(HttpMethod method, MockMultipartFile vocabularyFile,
                                                           String urlTemplate, Object... urlVars) {

        return multipart(urlTemplate, urlVars)
                .file(vocabularyFile)
                .with(request -> {
                    request.setMethod(method.toString());
                    return request;
                });
    }


    @Test
    public void shouldExportVocabulary() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/discipline.ttl");

        MockMultipartFile uploadedVocabulary = new MockMultipartFile(
                "ttl", "discipline.ttl", null, vocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, uploadedVocabulary, "/api/vocabularies")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("discipline")))
                .andExpect(jsonPath("$.label", is("ÖFOS 2012. Austrian Fields of Science and Technology Classification 2012")));

        mvc.perform(
                        get("/api/vocabularies/{code}", "discipline")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("discipline")))
                .andExpect(jsonPath("$.label", is("ÖFOS 2012. Austrian Fields of Science and Technology Classification 2012")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.conceptResults.hits", is(1449)))
                .andExpect(jsonPath("$.conceptResults.count", is(20)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(20)));

        mvc.perform(
                        get("/api/vocabularies/export/{code}", "discipline")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/")))
                .andExpect(content().string(containsString("<https://vocabs.acdh.oeaw.ac.at/oefosdisciplines/Schema> a skos:ConceptScheme;")));
    }

    @Test
    public void shouldUpdateExportedVocabulary() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/sshoc-keyword-test.ttl");

        MockMultipartFile uploadedVocabulary = new MockMultipartFile(
                "ttl", "sshoc-keyword-test.ttl", null, vocabularyStream
        );

        mvc.perform(
                        vocabularyUpload(HttpMethod.POST, uploadedVocabulary, "/api/vocabularies")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("sshoc-keyword-test")))
                .andExpect(jsonPath("$.label", is("Keywords from SSHOC MP")));

        mvc.perform(
                        get("/api/vocabularies/{code}", "sshoc-keyword-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("sshoc-keyword-test")))
                .andExpect(jsonPath("$.label", is("Keywords from SSHOC MP")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.conceptResults.hits", is(4)))
                .andExpect(jsonPath("$.conceptResults.count", is(4)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(4)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInAnyOrder("1-grams", "18th-century", "18th-century-literature", "zip")
                        )
                );


        String response = mvc.perform(
                        get("/api/vocabularies/export/{code}", "sshoc-keyword-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("@prefix : <https://vocabs.dariah.eu/sshoc-keyword-test/> . \r\n" +
                        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" +
                        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n" +
                        "@prefix skos: <http://www.w3.org/2004/02/skos/core#> .\r\n" +
                        "@prefix skosxl: <http://www.w3.org/2008/05/skos-xl#> .\r\n" +
                        "@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n" +
                        "@prefix dc: <http://purl.org/dc/elements/1.1/> .\r\n" +
                        "@prefix dcterms: <http://purl.org/dc/terms/> .\r\n" +
                        "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\r\n" +
                        "@prefix tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/> .\r\n" +
                        "@prefix cycAnnot: <http://sw.cyc.com/CycAnnotations_v1#> .\r\n" +
                        "@prefix csw: <http://semantic-web.at/ontologies/csw.owl#> .\r\n" +
                        "@prefix dbpedia: <http://dbpedia.org/resource/> .\r\n" +
                        "@prefix freebase: <http://rdf.freebase.com/ns/> .\r\n" +
                        "@prefix opencyc: <http://sw.opencyc.org/concept/> .\r\n" +
                        "@prefix cyc: <http://sw.cyc.com/concept/> .\r\n" +
                        "@prefix ctag: <http://commontag.org/ns#> .\r\n" +
                        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n" +
                        "\r\n" +
                        "<https://vocabs.dariah.eu/sshoc-keyword-test/Schema> a skos:ConceptScheme;\r\n" +
                        "  csw:hierarchyRoot true;\r\n" +
                        "  csw:hierarchyRootType skos:ConceptScheme;\r\n" +
                        "  skos:prefLabel \"Keywords from SSHOC MP\"@en;\r\n" +
                        "  dc:description \"All the keywords used in the SSHOC Marketplace: coming from various sources.\"@en,\r\n" +
                        "    \"Alle Keywords, die im SSHOC Marketplace verwendet werden: Sie stammen aus verschiedenen Quellen.\"@de,\r\n" +
                        "    \"Tous les mots-clÃ©s utilisÃ©s sur la place de marchÃ© du SSHOC : provenant de diverses sources.\"@fr;\r\n" +
                        "  dcterms:title \"StichwÃ¶rter aus SSHOC MP\"@de, \"Keywords from SSHOC MP\"@en, \"Mots clÃ©s de la SSHOC MP\"@fr;\r\n" +
                        "  rdfs:comment \"All the keywords used in the SSHOC Marketplace: coming from various sources.\"@en;\r\n" +
                        "  rdfs:label \"StichwÃ¶rter aus SSHOC MP\"@de, \"Keywords from SSHOC MP\"@en, \"Mots clÃ©s de la SSHOC MP\"@fr;\r\n" +
                        "  skos:hasTopConcept \"https://vocabs.dariah.eu/sshoc-keyword-test/1-grams\", \"https://vocabs.dariah.eu/sshoc-keyword-test/18th-century\",\r\n" +
                        "    \"https://vocabs.dariah.eu/sshoc-keyword-test/18th-century-literature\", \"https://vocabs.dariah.eu/sshoc-keyword-test/zip\" .\r\n" +
                        "\r\n" +
                        "<https://vocabs.dariah.eu/sshoc-keyword-test/1-grams> a skos:Concept;\r\n" +
                        "  skos:topConceptOf \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:inScheme \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:prefLabel \"1-grams\"@de, \"1-grams\"@fr, \"1-grams\"@en .\r\n" +
                        "\r\n" +
                        "<https://vocabs.dariah.eu/sshoc-keyword-test/18th-century> a skos:Concept;\r\n" +
                        "  skos:topConceptOf \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:inScheme \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:prefLabel \"18. Jh.\"@de, \"18Ã¨me siÃ¨cle\"@fr, \"18th-century\"@en .\r\n" +
                        "\r\n" +
                        "<https://vocabs.dariah.eu/sshoc-keyword-test/18th-century-literature> a skos:Concept;\r\n" +
                        "  skos:topConceptOf \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:inScheme \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:prefLabel \"18. Jahrhundert lietarture\"@de, \"Lietarture du 18e siÃ¨cle\"@fr, \"18th-century-literature\"@en .\r\n" +
                        "\r\n" +
                        "<https://vocabs.dariah.eu/sshoc-keyword-test/zip> a skos:Concept;\r\n" +
                        "  skos:topConceptOf \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:inScheme \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                        "  skos:prefLabel \"zip\"@de, \"zip\"@fr, \"zip\"@en .\r\n"
                ))
                .andReturn().getResponse().getContentAsString();

        response = response + "\r\n"
                + "<https://vocabs.dariah.eu/sshoc-keyword-test/19th-century-test> a skos:Concept;\r\n" +
                "  skos:topConceptOf \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                "  skos:inScheme \"https://vocabs.dariah.eu/sshoc-keyword-test/Schema\";\r\n" +
                "  skos:prefLabel \"19. Jh.\"@de, \"19Ã¨me siÃ¨cle\"@fr, \"19th-century-test\"@en .\r\n" +
                "\r\n";


        InputStream e = new ByteArrayInputStream(response.getBytes());
        MockMultipartFile exportedFile = new MockMultipartFile("ttl", "sshoc-keyword-test.ttl", null, e);

        mvc.perform(
                        vocabularyUpload(HttpMethod.PUT, exportedFile,
                                "/api/vocabularies/{code}", "sshoc-keyword-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("Authorization", moderatorJwt)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("sshoc-keyword-test")))
                .andExpect(jsonPath("$.label", is("Keywords from SSHOC MP")));

        mvc.perform(
                        get("/api/vocabularies/{code}", "sshoc-keyword-test")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("sshoc-keyword-test")))
                .andExpect(jsonPath("$.label", is("Keywords from SSHOC MP")))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.conceptResults.hits", is(5)))
                .andExpect(jsonPath("$.conceptResults.count", is(5)))
                .andExpect(jsonPath("$.conceptResults.concepts", hasSize(5)))
                .andExpect(
                        jsonPath(
                                "$.conceptResults.concepts[*].code",
                                containsInAnyOrder("1-grams", "18th-century", "18th-century-literature", "zip", "19th-century-test")
                        )
                );

    }
}
