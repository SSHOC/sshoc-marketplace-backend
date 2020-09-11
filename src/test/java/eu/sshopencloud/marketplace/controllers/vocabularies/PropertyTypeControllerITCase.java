package eu.sshopencloud.marketplace.controllers.vocabularies;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeReorder;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypesReordering;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
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
                .andExpect(jsonPath("$.hits", is(18)))
                .andExpect(jsonPath("$.count", is(10)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.perpage", is(10)))
                .andExpect(jsonPath("$.pages", is(2)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(10)))
                .andExpect(jsonPath("$.propertyTypes[*].code", contains(
                        "object-type", "language", "activity", "technique", "material", "object-format",
                        "keyword", "tadirah-goals", "thumbnail", "repository-url"
                )))
                .andExpect(jsonPath("$.propertyTypes[5].label", is("Object format")))
                .andExpect(jsonPath("$.propertyTypes[1].allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.propertyTypes[1].allowedVocabularies[*].code", contains("iso-639-3", "iso-639-3-v2")))
                .andExpect(jsonPath("$.propertyTypes[7].ord", is(8)));

        mvc.perform(
                get("/api/property-types")
                        .param("page", "2")
                        .param("perpage", "10")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hits", is(18)))
                .andExpect(jsonPath("$.count", is(8)))
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.perpage", is(10)))
                .andExpect(jsonPath("$.pages", is(2)))
                .andExpect(jsonPath("$.propertyTypes", hasSize(8)))
                .andExpect(jsonPath("$.propertyTypes[*].code", contains(
                        "license-type", "web-usable", "tool-family", "tadirah-methods", "methodica-link",
                        "cover-image", "media", "see-also"
                )));
    }

    @Test
    public void shouldCreatePropertyTypeWithConceptValueType() throws Exception {
        PropertyTypeCore propertyTypeData = PropertyTypeCore.builder()
                .label("New property type")
                .type(PropertyTypeClass.CONCEPT)
                .allowedVocabularies(Arrays.asList("nemo-activity-type", "iana-mime-type"))
                .build();

        mvc.perform(
                post("/api/property-types/{code}", "new-property-type")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(propertyTypeData))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("new-property-type")))
                .andExpect(jsonPath("$.label", is("New property type")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(19)))
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
                .andExpect(jsonPath("$.ord", is(19)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("nemo-activity-type", "iana-mime-type")));
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
                        .content(mapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("language")))
                .andExpect(jsonPath("$.label", is("Language code")))
                .andExpect(jsonPath("$.type", is("concept")))
                .andExpect(jsonPath("$.ord", is(2)))
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
                .andExpect(jsonPath("$.ord", is(2)))
                .andExpect(jsonPath("$.allowedVocabularies", hasSize(2)))
                .andExpect(jsonPath("$.allowedVocabularies[*].code", containsInAnyOrder("iso-639-3-v2", "software-license")));
    }

    @Test
    public void shouldDeletePropertyType() throws Exception {
        assertPropertyTypeOrder("activity", 3);
        assertPropertyTypeOrder("technique", 4);
        assertPropertyTypeOrder("web-usable", 12);
        assertPropertyTypeOrder("tool-family", 13);
        assertPropertyTypeOrder("media", 17);
        assertPropertyTypeOrder("license-type", 11);

        mvc.perform(
                delete("/api/property-types/{code}", "technique")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());

        mvc.perform(
                get("/api/property-types/{code}", "technique")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());


        assertPropertyTypeOrder("activity", 3);
        assertPropertyTypeOrder("web-usable", 11);
        assertPropertyTypeOrder("tool-family", 12);
        assertPropertyTypeOrder("media", 16);
        assertPropertyTypeOrder("license-type", 10);
    }

    @Test
    public void shouldProperlyReorderPropertyTypes() throws Exception {
        assertPropertyTypeOrder("object-format", 6);
        assertPropertyTypeOrder("keyword", 7);
        assertPropertyTypeOrder("tadirah-goals", 8);
        assertPropertyTypeOrder("thumbnail", 9);
        assertPropertyTypeOrder("repository-url", 10);
        assertPropertyTypeOrder("license-type", 11);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 7),
                        new PropertyTypeReorder("thumbnail", 7),
                        new PropertyTypeReorder("keyword", 10),
                        new PropertyTypeReorder("tadirah-goals", 9)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk());

        assertPropertyTypeOrder("object-format", 6);
        assertPropertyTypeOrder("thumbnail", 7);
        assertPropertyTypeOrder("repository-url", 8);
        assertPropertyTypeOrder("tadirah-goals", 9);
        assertPropertyTypeOrder("keyword", 10);
        assertPropertyTypeOrder("license-type", 11);
    }

    @Test
    public void shouldNotReorderWithOrdBelowBounds() throws Exception {
        assertPropertyTypeOrder("object-format", 6);
        assertPropertyTypeOrder("keyword", 7);
        assertPropertyTypeOrder("tadirah-goals", 8);
        assertPropertyTypeOrder("thumbnail", 9);
        assertPropertyTypeOrder("repository-url", 10);
        assertPropertyTypeOrder("license-type", 11);

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
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotReorderWithNotExistentPropertyType() throws Exception {
        assertPropertyTypeOrder("object-format", 6);
        assertPropertyTypeOrder("keyword", 7);
        assertPropertyTypeOrder("tadirah-goals", 8);
        assertPropertyTypeOrder("thumbnail", 9);
        assertPropertyTypeOrder("repository-url", 10);
        assertPropertyTypeOrder("license-type", 11);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 7),
                        new PropertyTypeReorder("thumbnail", 7),
                        new PropertyTypeReorder("tadi-goals", 9),
                        new PropertyTypeReorder("keyword", 10)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotReorderWithOrdOutOfBounds() throws Exception {
        assertPropertyTypeOrder("object-format", 6);
        assertPropertyTypeOrder("keyword", 7);
        assertPropertyTypeOrder("tadirah-goals", 8);
        assertPropertyTypeOrder("thumbnail", 9);
        assertPropertyTypeOrder("repository-url", 10);
        assertPropertyTypeOrder("license-type", 11);

        PropertyTypesReordering request = new PropertyTypesReordering(
                Arrays.asList(
                        new PropertyTypeReorder("repository-url", 7),
                        new PropertyTypeReorder("thumbnail", 7),
                        new PropertyTypeReorder("tadirah-goals", 30),
                        new PropertyTypeReorder("keyword", 10)
                )
        );

        mvc.perform(
                post("/api/property-types/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
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