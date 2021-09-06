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
}
