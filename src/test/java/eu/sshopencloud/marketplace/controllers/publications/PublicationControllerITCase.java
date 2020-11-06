package eu.sshopencloud.marketplace.controllers.publications;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class PublicationControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String MODERATOR_JWT;

    @Before
    public void init()
            throws Exception {
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
    }

    @Test
    public void shouldReturnPublications() throws Exception {

        mvc.perform(get("/api/publications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void shouldCreateSimplePublicationAsDraft() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test simple publication");
        publication.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        publication.setContributors(contributors);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("publication-type");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("Pre-Print");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("publication-type");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("year");
        property2.setType(propertyType2);
        property2.setValue("2010");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        publication.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        publication.setDateCreated(dateCreated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(publication);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/publications?draft=true")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status", is("draft")))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is("Test simple publication")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("contributors[0].actor.id", is(3)))
                .andExpect(jsonPath("contributors[0].role.label", is("Author")))
                .andExpect(jsonPath("properties[0].concept.label", is("Pre-Print")))
                .andExpect(jsonPath("properties[1].value", is("2010")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("olderVersions", hasSize(0)))
                .andExpect(jsonPath("newerVersions", hasSize(0)));
    }

    @Test
    public void shouldCreateSimplePublicationAsDraftAndRemoveIt() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test simple publication");
        publication.setDescription("Lorem ipsum");
        ItemContributorId contributor = new ItemContributorId();
        ActorId actor = new ActorId();
        actor.setId(3l);
        contributor.setActor(actor);
        ActorRoleId role = new ActorRoleId();
        role.setCode("author");
        contributor.setRole(role);
        List<ItemContributorId> contributors = new ArrayList<ItemContributorId>();
        contributors.add(contributor);
        publication.setContributors(contributors);
        PropertyCore property1 = new PropertyCore();
        PropertyTypeId propertyType1 = new PropertyTypeId();
        propertyType1.setCode("publication-type");
        property1.setType(propertyType1);
        ConceptId concept1 = new ConceptId();
        concept1.setCode("Pre-Print");
        VocabularyId vocabulary1 = new VocabularyId();
        vocabulary1.setCode("publication-type");
        concept1.setVocabulary(vocabulary1);
        property1.setConcept(concept1);
        PropertyCore property2 = new PropertyCore();
        PropertyTypeId propertyType2 = new PropertyTypeId();
        propertyType2.setCode("year");
        property2.setType(propertyType2);
        property2.setValue("2010");
        List<PropertyCore> properties = new ArrayList<PropertyCore>();
        properties.add(property1);
        properties.add(property2);
        publication.setProperties(properties);
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        publication.setDateCreated(dateCreated);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(publication);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/publications?draft=true")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String publicationPersistentId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, PublicationDto.class).getPersistentId();

        mvc.perform(delete("/api/publications/{id}?draft=true", publicationPersistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
            .andExpect(status().isOk());
    }

}
