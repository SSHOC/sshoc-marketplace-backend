package eu.sshopencloud.marketplace.controllers.vocabularies;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeReorder;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypesReordering;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PropertyTypeControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;

    @Before
    public void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }


    @Test
    public void shouldReturnPropertyTypes() throws Exception {
        mvc.perform(
                get("/api/property-types")
                        .param("page", "1")
                        .param("perpage", "10")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hits", is(27)))
                .andExpect(jsonPath("$.count", is(10)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.perpage", is(10)))
                .andExpect(jsonPath("$.pages", is(3)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(10)))
                .andExpect(jsonPath("$.propertyTypes[*].code", contains(
                        "language", "activity", "technique", "material", "object-format",
                        "keyword", "tadirah-goals", "thumbnail", "repository-url", "license"
                )))
                .andExpect(jsonPath("$.propertyTypes[5].label", is("Keyword")))
                .andExpect(jsonPath("$.propertyTypes[0].allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.propertyTypes[0].allowedVocabularies[*].code", contains("iso-639-3", "iso-639-3-v2")))
                .andExpect(jsonPath("$.propertyTypes[7].ord", is(8)));

        mvc.perform(
                get("/api/property-types")
                        .param("page", "3")
                        .param("perpage", "10")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hits", is(27)))
                .andExpect(jsonPath("$.count", is(7)))
                .andExpect(jsonPath("$.page", is(3)))
                .andExpect(jsonPath("$.perpage", is(10)))
                .andExpect(jsonPath("$.pages", is(3)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(7)))
                .andExpect(jsonPath("$.propertyTypes[*].code", contains(
                        "issue", "pages", "year", "timestamp", "publication-type", "doi", "deprecated-at-source"
                )));
    }

    @Test
    public void shouldCreatePropertyTypeWithConceptValueType() throws Exception {
        PropertyTypeCore propertyTypeData = PropertyTypeCore.builder()
                .code("new-property-type")
                .label("New property type")
                .type(PropertyTypeClass.CONCEPT)
                .allowedVocabularies(Arrays.asList("nemo-activity-type", "iana-mime-type"))
                .build();

        mvc.perform(
                post("/api/property-types")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(propertyTypeData))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("new-property-type")))
                .andExpect(jsonPath("$.label", is("New property type")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(28)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("nemo-activity-type", "iana-mime-type")));

        mvc.perform(
                get("/api/property-types/{code}", "new-property-type")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("new-property-type")))
                .andExpect(jsonPath("$.label", is("New property type")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(28)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("nemo-activity-type", "iana-mime-type")));
    }

    @Test
    public void shouldCreatePropertyTypeAtPosition() throws Exception {
        assertPropertyTypeOrder("language", 1);
        assertPropertyTypeOrder("activity", 2);
        assertPropertyTypeOrder("technique", 3);
        assertPropertyTypeOrder("material", 4);
        assertPropertyTypeOrder("object-format", 5);

        PropertyTypeCore propertyTypeData = PropertyTypeCore.builder()
                .code("github")
                .label("GitHub")
                .type(PropertyTypeClass.URL)
                .ord(3)
                .build();

        mvc.perform(
                post("/api/property-types")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(propertyTypeData))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("github")))
                .andExpect(jsonPath("$.label", is("GitHub")))
                .andExpect(jsonPath("$.type", is("url")))
                .andExpect(jsonPath("$.ord", is(3)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(0)));

        mvc.perform(
                get("/api/property-types/{code}", "github")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("github")))
                .andExpect(jsonPath("$.label", is("GitHub")))
                .andExpect(jsonPath("$.type", is("url")))
                .andExpect(jsonPath("$.ord", is(3)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(0)));

        assertPropertyTypeOrder("language", 1);
        assertPropertyTypeOrder("activity", 2);
        assertPropertyTypeOrder("github", 3);
        assertPropertyTypeOrder("technique", 4);
        assertPropertyTypeOrder("material", 5);
        assertPropertyTypeOrder("object-format", 6);
    }

    @Test
    public void shouldNotRetrieveNonExistentPropertyType() throws Exception {
        mvc.perform(
                get("/api/property-types/{code}", "not-a-property-type")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdatePropertyType() throws Exception {
        PropertyTypeCore request = PropertyTypeCore.builder()
                .label("Language code")
                .type(PropertyTypeClass.CONCEPT)
                .allowedVocabularies(Arrays.asList("iso-639-3-v2", "software-license"))
                .build();

        mvc.perform(
                put("/api/property-types/{code}", "language")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("language")))
                .andExpect(jsonPath("$.label", is("Language code")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(1)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("iso-639-3-v2", "software-license")));

        mvc.perform(
                get("/api/property-types/{code}", "language")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("language")))
                .andExpect(jsonPath("$.label", is("Language code")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(1)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("iso-639-3-v2", "software-license")));
    }

    @Test
    public void shouldUpdatePropertyTypeWithReorder() throws Exception {
        assertPropertyTypeOrder("language", 1);
        assertPropertyTypeOrder("activity", 2);
        assertPropertyTypeOrder("technique", 3);
        assertPropertyTypeOrder("material", 4);
        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);

        PropertyTypeCore request = PropertyTypeCore.builder()
                .label("Object-Format")
                .type(PropertyTypeClass.CONCEPT)
                .allowedVocabularies(List.of("iana-mime-type"))
                .ord(2)
                .build();

        mvc.perform(
                put("/api/property-types/{code}", "object-format")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("object-format")))
                .andExpect(jsonPath("$.label", is("Object-Format")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(2)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(1)))
                .andExpect(jsonPath("$.allowedVocabularies[0].code", is("iana-mime-type")));

        mvc.perform(
                get("/api/property-types/{code}", "object-format")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("object-format")))
                .andExpect(jsonPath("$.label", is("Object-Format")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(2)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(1)))
                .andExpect(jsonPath("$.allowedVocabularies[0].code", is("iana-mime-type")));

        assertPropertyTypeOrder("language", 1);
        assertPropertyTypeOrder("object-format", 2);
        assertPropertyTypeOrder("activity", 3);
        assertPropertyTypeOrder("technique", 4);
        assertPropertyTypeOrder("material", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);
    }

    @Test
    public void shouldDeletePropertyTypeSafely() throws Exception {
        assertPropertyTypeOrder("activity", 2);
        assertPropertyTypeOrder("technique", 3);
        assertPropertyTypeOrder("web-usable", 11);
        assertPropertyTypeOrder("tool-family", 12);
        assertPropertyTypeOrder("media", 16);

        mvc.perform(
                delete("/api/property-types/{code}", "technique")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/property-types/{code}", "technique")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());


        assertPropertyTypeOrder("activity", 2);
        assertPropertyTypeOrder("web-usable", 10);
        assertPropertyTypeOrder("tool-family", 11);
        assertPropertyTypeOrder("media", 15);
        assertPropertyTypeOrder("license", 9);
    }

    @Test
    public void shouldNotDeletePropertyTypeInUse() throws Exception {
        mvc.perform(
                delete("/api/property-types/{code}", "activity")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/property-types/{code}", "activity"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldDeletePropertyTypeWithForce() throws Exception {
        mvc.perform(
                delete("/api/property-types/{code}", "activity")
                        .param("force", "true")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(get("/api/property-types/{code}", "activity"))
                .andExpect(status().isNotFound());
    }

    @Ignore(value = "hidden properties have to be always rendered")
    @Test
    public void shouldRetrieveHiddenPropertyForModeratorsOnly() throws Exception {
        String code = "http-status";
        PropertyTypeCore propertyType = PropertyTypeCore.builder()
                .code(code)
                .label("HTTP resource status code")
                .type(PropertyTypeClass.INT)
                .groupName("availability")
                .hidden(true)
                .build();

        String payload = mapper.writeValueAsString(propertyType);

        mvc.perform(
                post("/api/property-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("http-status")))
                .andExpect(jsonPath("label", is("HTTP resource status code")))
                .andExpect(jsonPath("type", is("int")))
                .andExpect(jsonPath("groupName", is("availability")))
                .andExpect(jsonPath("hidden", is(true)));

        mvc.perform(get("/api/property-types/{code}", code))
                .andExpect(status().isNotFound());

        mvc.perform(
                get("/api/property-types/{code}", code)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isNotFound());

        mvc.perform(
                get("/api/property-types/{code}", code)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("http-status")))
                .andExpect(jsonPath("label", is("HTTP resource status code")))
                .andExpect(jsonPath("type", is("int")))
                .andExpect(jsonPath("groupName", is("availability")))
                .andExpect(jsonPath("hidden", is(true)));

        mvc.perform(
                get("/api/property-types/{code}", code)
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/property-types")
                        .param("q", "http resource")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", is(0)))
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.pages", is(0)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(0)));

        mvc.perform(
                get("/api/property-types")
                        .param("q", "http resource")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", is(0)))
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.pages", is(0)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(0)));

        mvc.perform(
                get("/api/property-types")
                        .param("q", "http resource")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", is(1)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.pages", is(1)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(1)))
                .andExpect(jsonPath("$.propertyTypes[0].code", is(code)))
                .andExpect(jsonPath("$.propertyTypes[0].hidden", is(true)));

        mvc.perform(
                get("/api/property-types")
                        .param("q", "http resource")
                        .header("Authorization", ADMINISTRATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", is(1)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.pages", is(1)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(1)))
                .andExpect(jsonPath("$.propertyTypes[0].code", is(code)))
                .andExpect(jsonPath("$.propertyTypes[0].hidden", is(true)));
    }

    @Test
    public void shouldCreatePropertyWithGroupName() throws Exception {
        PropertyTypeCore propertyType = PropertyTypeCore.builder()
                .code("grouped-property")
                .label("This property is grouped")
                .type(PropertyTypeClass.STRING)
                .groupName("group")
                .hidden(false)
                .build();

        String payload = mapper.writeValueAsString(propertyType);

        mvc.perform(
                post("/api/property-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("grouped-property")))
                .andExpect(jsonPath("groupName", is("group")));
    }

    @Test
    public void shouldUpdatePropertyGroupName() throws Exception {
        PropertyTypeCore propertyType = PropertyTypeCore.builder()
                .code("media")
                .label("Media")
                .type(PropertyTypeClass.URL)
                .groupName("browsable")
                .hidden(false)
                .ord(16)
                .build();

        String payload = mapper.writeValueAsString(propertyType);

        mvc.perform(
                put("/api/property-types/{code}", "media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("media")))
                .andExpect(jsonPath("groupName", is("browsable")));

        mvc.perform(get("/api/property-types/{code}", "media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code", is("media")))
                .andExpect(jsonPath("label", is("Media")))
                .andExpect(jsonPath("type", is("url")))
                .andExpect(jsonPath("groupName", is("browsable")))
                .andExpect(jsonPath("hidden", is(false)))
                .andExpect(jsonPath("ord", is(16)));
    }

    @Test
    public void shouldProperlyReorderPropertyTypes() throws Exception {
        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);
        assertPropertyTypeOrder("thumbnail", 8);
        assertPropertyTypeOrder("repository-url", 9);
        assertPropertyTypeOrder("license", 10);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 6),
                        new PropertyTypeReorder("thumbnail", 6),
                        new PropertyTypeReorder("keyword", 9),
                        new PropertyTypeReorder("tadirah-goals", 8)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk());

        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("thumbnail", 6);
        assertPropertyTypeOrder("repository-url", 7);
        assertPropertyTypeOrder("tadirah-goals", 8);
        assertPropertyTypeOrder("keyword", 9);
        assertPropertyTypeOrder("license", 10);
    }

    @Test
    public void shouldNotReorderWithOrdBelowBounds() throws Exception {
        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);
        assertPropertyTypeOrder("thumbnail", 8);
        assertPropertyTypeOrder("repository-url", 9);
        assertPropertyTypeOrder("license", 10);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 6),
                        new PropertyTypeReorder("thumbnail", 6),
                        new PropertyTypeReorder("tadirah-goals", 0),
                        new PropertyTypeReorder("keyword", 9)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotReorderUnauthorized() throws Exception {
        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);
        assertPropertyTypeOrder("thumbnail", 8);
        assertPropertyTypeOrder("repository-url", 9);
        assertPropertyTypeOrder("license", 10);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 7),
                        new PropertyTypeReorder("thumbnail", 7),
                        new PropertyTypeReorder("tadirah-goals", 0),
                        new PropertyTypeReorder("keyword", 10)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotReorderWithNotExistentPropertyType() throws Exception {
        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);
        assertPropertyTypeOrder("thumbnail", 8);
        assertPropertyTypeOrder("repository-url", 9);
        assertPropertyTypeOrder("license", 10);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 6),
                        new PropertyTypeReorder("thumbnail", 6),
                        new PropertyTypeReorder("tadi-goals", 8),
                        new PropertyTypeReorder("keyword", 9)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotReorderWithOrdOutOfBounds() throws Exception {
        assertPropertyTypeOrder("object-format", 5);
        assertPropertyTypeOrder("keyword", 6);
        assertPropertyTypeOrder("tadirah-goals", 7);
        assertPropertyTypeOrder("thumbnail", 8);
        assertPropertyTypeOrder("repository-url", 9);
        assertPropertyTypeOrder("license", 10);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 6),
                        new PropertyTypeReorder("thumbnail", 6),
                        new PropertyTypeReorder("tadirah-goals", 30),
                        new PropertyTypeReorder("keyword", 9)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", MODERATOR_JWT)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    private void assertPropertyTypeOrder(String propertyTypeCode, int ord) throws Exception {
        mvc.perform(
                get("/api/property-types/{code}", propertyTypeCode)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ord", is(ord)));
    }
}