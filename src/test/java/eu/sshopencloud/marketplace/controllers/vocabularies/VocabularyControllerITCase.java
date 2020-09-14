package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@Transactional
public class VocabularyControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String contributorJwt;
    private String moderatorJwt;

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
                .andExpect(jsonPath("$.hits", is(8)))
                .andExpect(jsonPath("$.count", is(8)))
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
                                        "tadirah-research-technique"
                                )
                        )
                );
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
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type")));

        mvc.perform(
                get("/api/vocabularies/{code}", "iana-mime-type-test")
                        .accept(MediaType.APPLICATION_JSON)
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
    public void shouldUpdateVocabularyWithNoAssociatedProperties() throws Exception {
        InputStream newVocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, newVocabularyStream
        );

        mvc.perform(
                vocabularyUpload(HttpMethod.POST, newVocabulary, "/api/vocabularies")
                        .accept(MediaType.APPLICATION_JSON)
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
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("iana-mime-type-test")))
                .andExpect(jsonPath("$.label", is("IANA mime/type v2")));

        mvc.perform(
                get("/api/vocabularies/{code}", "iana-mime-type-test")
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
    public void shouldPreventVocabularyUpdateOnFilenameMismatch() throws Exception {
        InputStream newVocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile newVocabulary = new MockMultipartFile(
                "ttl", "iana-mime-type-test.ttl", null, newVocabularyStream
        );

        mvc.perform(
                vocabularyUpload(HttpMethod.POST, newVocabulary, "/api/vocabularies")
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
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRemoveVocabularyAndConceptsWithAssociatedProperties() throws Exception {
        mvc.perform(
                delete("/api/vocabularies/{code}", "iana-mime-type")
                        .header("Authorization", moderatorJwt)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/vocabularies/{code}", "iana-mime-type")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());

        mvc.perform(
                get("/api/training-materials/{id}", 7)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.properties[?(@.concept.code == \"video/mp4\")]").doesNotExist()
                );
    }

    @Test
    public void shouldNotCreateVocabularyUnauthorized() throws Exception {
        InputStream vocabularyStream = VocabularyControllerITCase.class
                .getResourceAsStream("/initial-data/vocabularies/iana-mime-type-test.ttl");

        MockMultipartFile vocabularyFile = new MockMultipartFile("ttl", "iana-mime-type-test.ttl", null, vocabularyStream);

        mvc.perform(
                vocabularyUpload(HttpMethod.POST, vocabularyFile, "/api/vocabularies")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());

        mvc.perform(
                vocabularyUpload(HttpMethod.POST, vocabularyFile, "/api/vocabularies")
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
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isForbidden());

        mvc.perform(
                vocabularyUpload(HttpMethod.PUT, vocabularyFile, "/api/vocabularies/{code}", "iana-mime-type")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", contributorJwt)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotDeleteVocabularyUnauthorized() throws Exception {
        mvc.perform(delete("/api/vocabularies/iana-mime-type"))
                .andExpect(status().isForbidden());

        mvc.perform(
                delete("/api/vocabularies/iana-mime-type")
                        .header("Authorization", contributorJwt)
        )
                .andExpect(status().isForbidden());
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
