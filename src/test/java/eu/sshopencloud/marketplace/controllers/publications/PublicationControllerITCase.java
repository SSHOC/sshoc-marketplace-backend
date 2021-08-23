package eu.sshopencloud.marketplace.controllers.publications;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.conf.datetime.ZonedDateTimeDeserializer;
import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorId;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdCore;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdId;
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

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String IMPORTER_JWT;
    private String MODERATOR_JWT;

    @Before
    public void init() throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
    }

    @Test
    public void shouldReturnPublications() throws Exception {

        mvc.perform(get("/api/publications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnPublicationsAndTheProposedOnes() throws Exception {
        PublicationCore publication1 = new PublicationCore();
        publication1.setLabel("Test proposed publication");
        publication1.setDescription("Lorem ipsum dolor");

        String payload1 = mapper.writeValueAsString(publication1);

        String publicationJson1 = mvc.perform(
                post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload1)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(publication1.getLabel())))
                .andExpect(jsonPath("description", is(publication1.getDescription())))
                .andReturn().getResponse().getContentAsString();

        PublicationDto publicationDto1 = mapper.readValue(publicationJson1, PublicationDto.class);
        String publicationId = publicationDto1.getPersistentId();
        int publicationVersionId1 = publicationDto1.getId().intValue();

        PublicationCore publication2 = new PublicationCore();
        publication2.setLabel("Test ingested publication");
        publication2.setDescription("Lorem ipsum dolor sit");

        String payload2 = mapper.writeValueAsString(publication2);

        String publicationJson2 = mvc.perform(
                put("/api/publications/{id}", publicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("ingested")))
                .andExpect(jsonPath("label", is(publication2.getLabel())))
                .andExpect(jsonPath("description", is(publication2.getDescription())))
                .andReturn().getResponse().getContentAsString();

        PublicationDto publicationDto2 = mapper.readValue(publicationJson2, PublicationDto.class);
        int publicationVersionId2 = publicationDto2.getId().intValue();

        mvc.perform(
                get("/api/publications/{id}", publicationId)
                        .param("approved", "false")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(publicationId)))
                .andExpect(jsonPath("id", is(publicationVersionId1)))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(publication1.getLabel())))
                .andExpect(jsonPath("description", is(publication1.getDescription())));

        mvc.perform(
                get("/api/publications")
                        .param("approved", "false")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("publications", hasSize(1)))
                .andExpect(jsonPath("publications[0].persistentId", is(publicationId)))
                .andExpect(jsonPath("publications[0].id", is(publicationVersionId1)))
                .andExpect(jsonPath("publications[0].status", is("suggested")));

        mvc.perform(
                get("/api/publications")
                        .param("approved", "false")
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("publications", hasSize(1)))
                .andExpect(jsonPath("publications[0].persistentId", is(publicationId)))
                .andExpect(jsonPath("publications[0].id", is(publicationVersionId2)))
                .andExpect(jsonPath("publications[0].status", is("ingested")));

        mvc.perform(
                get("/api/publications")
                        .param("approved", "false")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(2)))
                .andExpect(jsonPath("publications", hasSize(2)))
                .andExpect(jsonPath("publications[0].persistentId", is(publicationId)))
                .andExpect(jsonPath("publications[0].id", is(publicationVersionId2)))
                .andExpect(jsonPath("publications[0].status", is("ingested")))
                .andExpect(jsonPath("publications[1].persistentId", is(publicationId)))
                .andExpect(jsonPath("publications[1].id", is(publicationVersionId1)))
                .andExpect(jsonPath("publications[1].status", is("suggested")));
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
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))));
    }

    @Test
    public void shouldCreatePublicationWithDateInZZone() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test publication with Z zone");
        publication.setDescription("Lorem ipsum");
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        publication.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 1, 2, 345000000), ZoneId.of("UTC"));
        publication.setDateLastUpdated(dateLastUpdated);

        String payload = new ZoneOffsetXXTestJsonMapper().serializingObjectMapper().writeValueAsString(publication);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/publications")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is("Test publication with Z zone")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));
    }


    private class ZoneOffsetXXTestJsonMapper {

        public ObjectMapper serializingObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneOffsetXXDateTimeSerializer());
            javaTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            objectMapper.registerModule(javaTimeModule);
            return objectMapper;
        }

    }

    private class ZoneOffsetXXDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX")));
        }
    }

    @Test
    public void shouldCreatePublicationWithDateInZZoneAndMilliseconds() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test publication with Z zone");
        publication.setDescription("Lorem ipsum");
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        publication.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 1, 2, 345000000), ZoneId.of("UTC"));
        publication.setDateLastUpdated(dateLastUpdated);

        String payload = new ZoneOffsetXXWithMillisecondsTestJsonMapper().serializingObjectMapper().writeValueAsString(publication);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/publications")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is("Test publication with Z zone")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));
    }

    private class ZoneOffsetXXWithMillisecondsTestJsonMapper {

        public ObjectMapper serializingObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneOffsetXXDateTimeSerializerWithMilliseconds());
            javaTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            objectMapper.registerModule(javaTimeModule);
            return objectMapper;
        }

    }

    private class ZoneOffsetXXDateTimeSerializerWithMilliseconds extends JsonSerializer<ZonedDateTime> {

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX")));
        }

    }


    @Test
    public void shouldCreatePublicationWithDateInOffsetZone() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test publication with offset zone");
        publication.setDescription("Lorem ipsum");
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        publication.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 1, 2, 345000000), ZoneId.of("UTC"));
        publication.setDateLastUpdated(dateLastUpdated);

        String payload = new ZoneOffsetZZTestJsonMapper().serializingObjectMapper().writeValueAsString(publication);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/publications")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is("Test publication with offset zone")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));
    }

    private class ZoneOffsetZZTestJsonMapper {

        public ObjectMapper serializingObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneOffsetZZDateTimeSerializer());
            javaTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            objectMapper.registerModule(javaTimeModule);
            return objectMapper;
        }

    }

    private class ZoneOffsetZZDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZ")));
        }

    }

    @Test
    public void shouldCreatePublicationWithDateInOffsetZoneAndMilliseconds() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test publication with offset zone");
        publication.setDescription("Lorem ipsum");
        ZonedDateTime dateCreated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 0), ZoneId.of("UTC"));
        publication.setDateCreated(dateCreated);
        ZonedDateTime dateLastUpdated = ZonedDateTime.of(LocalDate.of(2020, Month.APRIL, 15), LocalTime.of(12, 1, 2, 345000000), ZoneId.of("UTC"));
        publication.setDateLastUpdated(dateLastUpdated);

        String payload = new ZoneOffsetZZWithMillisecondsTestJsonMapper().serializingObjectMapper().writeValueAsString(publication);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/publications")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is("Test publication with offset zone")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("dateCreated", is(ApiDateTimeFormatter.formatDateTime(dateCreated))))
                .andExpect(jsonPath("dateLastUpdated", is(ApiDateTimeFormatter.formatDateTime(dateLastUpdated))));
    }

    private class ZoneOffsetZZWithMillisecondsTestJsonMapper {

        public ObjectMapper serializingObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(ZonedDateTime.class, new ZoneOffsetZZDateTimeSerializerWithMilliseconds());
            javaTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            objectMapper.registerModule(javaTimeModule);
            return objectMapper;
        }

    }

    private class ZoneOffsetZZDateTimeSerializerWithMilliseconds extends JsonSerializer<ZonedDateTime> {

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")));
        }

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

    @Test
    public void shouldCreatePublicationWithValidDateProperty() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test publication with time");
        publication.setDescription("Lorem ipsum");

        PropertyCore property = new PropertyCore(new PropertyTypeId("timestamp"), "2020-12-24T20:02:00+0001");
        publication.setProperties(List.of(property));

        String payload = mapper.writeValueAsString(publication);

        mvc.perform(
                post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is("Test publication with time")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(1)))
                .andExpect(jsonPath("properties[0].type.code", is("timestamp")))
                .andExpect(jsonPath("properties[0].type.label", is("Timestamp")))
                .andExpect(jsonPath("properties[0].type.type", is("date")))
                .andExpect(jsonPath("properties[0].value", is("2020-12-24T20:02:00+0001")));
    }

    @Test
    public void shouldRetrieveSuggestedPublication() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Suggested publication");
        publication.setDescription("This is a suggested publication");

        String payload = mapper.writeValueAsString(publication);

        String publicationJson = mvc.perform(
                post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("ingested")))
                .andReturn().getResponse().getContentAsString();

        PublicationDto publicationDto = mapper.readValue(publicationJson, PublicationDto.class);
        String publicationId = publicationDto.getPersistentId();
        int publicationVersionId = publicationDto.getId().intValue();

        mvc.perform(
                get("/api/publications/{id}", publicationId)
                        .param("approved", "false")
                        .header("Authorization", IMPORTER_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(publicationId)))
                .andExpect(jsonPath("id", is(publicationVersionId)))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("ingested")));

        mvc.perform(
                get("/api/publications/{id}", publicationId)
                        .param("approved", "false")
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/tools-services/{id}", publicationId))
                .andExpect(status().isNotFound());

        mvc.perform(
                get("/api/publications/{id}", publicationId)
                        .param("approved", "false")
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", is(publicationId)))
                .andExpect(jsonPath("id", is(publicationVersionId)))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("ingested")));
    }

    @Test
    public void shouldUpdatePublicationAndAddExternalIds() throws Exception {
        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test publication");
        publication.setDescription("New unknown publication");

        String payload = mapper.writeValueAsString(publication);

        String publicationJson = mvc.perform(
                post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(publication.getLabel())))
                .andExpect(jsonPath("description", is(publication.getDescription())))
                .andExpect(jsonPath("externalIds", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        PublicationDto publicationDto = mapper.readValue(publicationJson, PublicationDto.class);
        String publicationId = publicationDto.getPersistentId();

        PublicationCore publicationV2 = new PublicationCore();
        publicationV2.setLabel("Test Publication");
        publicationV2.setDescription("New recognized Publication");
        publicationV2.setExternalIds(
                List.of(
                        new ItemExternalIdCore(new ItemExternalIdId("GitHub"), "https://github.com/tesseract-ocr/tessdoc"),
                        new ItemExternalIdCore(new ItemExternalIdId("Wikidata"), "Q945242")
                )
        );

        String payloadV2 = mapper.writeValueAsString(publicationV2);

        mvc.perform(
                put("/api/publications/{id}", publicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadV2)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("label", is(publicationV2.getLabel())))
                .andExpect(jsonPath("description", is(publicationV2.getDescription())))
                .andExpect(jsonPath("externalIds", hasSize(2)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("GitHub")))
                .andExpect(jsonPath("externalIds[0].identifierService.urlTemplate", is("https://github.com/{source-item-id}")))
                .andExpect(jsonPath("externalIds[0].identifier", is(publicationV2.getExternalIds().get(0).getIdentifier())))
                .andExpect(jsonPath("externalIds[1].identifierService.code", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifierService.urlTemplate", is("https://www.wikidata.org/wiki/{source-item-id}")))
                .andExpect(jsonPath("externalIds[1].identifier", is(publicationV2.getExternalIds().get(1).getIdentifier())));
    }


    @Test
    public void shouldReturnPublicationInformationContributors() throws Exception {

        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test ingested publication 1");
        publication.setDescription("Lorem ipsum dolor sit");

        String payload = mapper.writeValueAsString(publication);

        String publicationJson = mvc.perform(
                post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is(publication.getLabel())))
                .andExpect(jsonPath("description", is(publication.getDescription())))
                .andReturn().getResponse().getContentAsString();

        String publicationPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(publicationJson, PublicationDto.class).getPersistentId();

        log.debug("publicationPersistentId: " + publicationPersistentId);

        mvc.perform(get("/api/publications/{id}/information-contributors", publicationPersistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].username", is("Contributor")))
                .andExpect(jsonPath("$[0].displayName", is("Contributor")))
                .andExpect(jsonPath("$[0].status", is("enabled")))
                .andExpect(jsonPath("$[0].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[0].role", is("contributor")))
                .andExpect(jsonPath("$[0].email", is("contributor@example.com")))
                .andExpect(jsonPath("$[0].config", is(true)));
    }

    @Test
    public void shouldReturnPublicationInformationContributorsForVersion() throws Exception {

        PublicationCore publication = new PublicationCore();
        publication.setLabel("Test ingested publication 1");
        publication.setDescription("Lorem ipsum dolor sit");

        String payload = mapper.writeValueAsString(publication);

        String publicationJson = mvc.perform(
                post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is(publication.getLabel())))
                .andExpect(jsonPath("description", is(publication.getDescription())))
                .andReturn().getResponse().getContentAsString();

        String publicationPersistentId = TestJsonMapper.serializingObjectMapper()
                .readValue(publicationJson, PublicationDto.class).getPersistentId();

        log.debug("publicationPersistentId: " + publicationPersistentId);

        PublicationCore publication2 = new PublicationCore();
        publication2.setLabel("Test ingested publication 2");
        publication2.setDescription("Lorem ipsum dolor sit");

        String payload2 = mapper.writeValueAsString(publication2);

        String publicationJson2 = mvc.perform(
                put("/api/publications/{id}", publicationPersistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2)
                        .header("Authorization", MODERATOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("persistentId", notNullValue()))
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("category", is("publication")))
                .andExpect(jsonPath("label", is(publication2.getLabel())))
                .andExpect(jsonPath("description", is(publication2.getDescription())))
                .andReturn().getResponse().getContentAsString();


        Long publicationId = TestJsonMapper.serializingObjectMapper()
                .readValue(publicationJson2, DatasetDto.class).getId();

        log.debug("datasetId: " + publicationId);

        mvc.perform(get("/api/publications/{id}/versions/{versionId}/information-contributors", publicationPersistentId, publicationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("Moderator")))
                .andExpect(jsonPath("$[0].displayName", is("Moderator")))
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[1].username", is("Contributor")))
                .andExpect(jsonPath("$[1].displayName", is("Contributor")))
                .andExpect(jsonPath("$[1].status", is("enabled")))
                .andExpect(jsonPath("$[1].registrationDate", is("2020-08-04T12:29:00+0200")))
                .andExpect(jsonPath("$[1].role", is("contributor")))
                .andExpect(jsonPath("$[1].email", is("contributor@example.com")))
                .andExpect(jsonPath("$[1].config", is(true)));
    }

}
