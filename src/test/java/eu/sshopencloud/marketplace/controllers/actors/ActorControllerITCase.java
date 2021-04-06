package eu.sshopencloud.marketplace.controllers.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.actors.*;
import eu.sshopencloud.marketplace.model.actors.Actor;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class ActorControllerITCase {

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
    public void shouldReturnActors() throws Exception {

        mvc.perform(get("/api/actors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnActorsByName() throws Exception {

        mvc.perform(get("/api/actors?q=CESSDA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("actors[0].name", is("CESSDA")))
                .andExpect(jsonPath("actors[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("actors[0].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldReturnActorsByWebsite() throws Exception {

        mvc.perform(get("/api/actors?q=https://www.cessda.eu/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("actors[0].name", is("CESSDA")))
                .andExpect(jsonPath("actors[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("actors[0].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldReturnActorsByEmail() throws Exception {

        mvc.perform(get("/api/actors?q=cessda@cessda.eu")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("actors[0].name", is("CESSDA")))
                .andExpect(jsonPath("actors[0].website", is("https://www.cessda.eu/")))
                .andExpect(jsonPath("actors[0].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldReturnActor() throws Exception {
        Integer actorId = 5;

        mvc.perform(get("/api/actors/{id}", actorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorId)))
                .andExpect(jsonPath("name", is("John Smith")))
                .andExpect(jsonPath("website", is("https://example.com/")))
                .andExpect(jsonPath("email", is("john@example.com")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].id", is(3)))
                .andExpect(jsonPath("affiliations[0].name", is("SSHOC project consortium")))
                .andExpect(jsonPath("affiliations[0].website", is("https://sshopencloud.eu/")))
                .andExpect(jsonPath("affiliations[0].email", blankOrNullString()));
    }

    @Test
    public void shouldNotReturnActorWhenNotExist() throws Exception {
        Integer actorId = 51;

        mvc.perform(get("/api/actors/{id}", actorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateActorWithoutAffiliations() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setWebsite("http://www.example.org");
        actor.setEmail("test@example.org");

        String payload = mapper.writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/actors")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", is("http://www.example.org")))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(0)));
    }

    @Test
    public void shouldCreateActorWithAffiliations() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(1l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        String payload = mapper.writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/actors")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", blankOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(2)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("affiliations[0].email", blankOrNullString()))
                .andExpect(jsonPath("affiliations[1].name", is("CESSDA")))
                .andExpect(jsonPath("affiliations[1].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldCreateActorWithExternalId() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setAffiliations(List.of(new ActorId(1L)));
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("DBLP"), "https://dblp.org/pers/g/Gray_0001:Jim.html")
        ));

        String payload = mapper.writeValueAsString(actor);

        String actorJson = mvc.perform(
                post("/api/actors")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", blankOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("externalIds", hasSize(1)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("DBLP")))
                .andExpect(jsonPath("externalIds[0].identifierService.label", is("DBLP")))
                .andExpect(jsonPath("externalIds[0].identifier", is("https://dblp.org/pers/g/Gray_0001:Jim.html")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("affiliations[0].email", blankOrNullString()))
                .andReturn().getResponse().getContentAsString();

        ActorDto actorDto = mapper.readValue(actorJson, ActorDto.class);

        mvc.perform(get("/api/actors/{actorId}", actorDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorDto.getId().intValue())))
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", blankOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("externalIds", hasSize(1)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("DBLP")))
                .andExpect(jsonPath("externalIds[0].identifierService.label", is("DBLP")))
                .andExpect(jsonPath("externalIds[0].identifier", is("https://dblp.org/pers/g/Gray_0001:Jim.html")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("affiliations[0].email", blankOrNullString()));
    }

    @Test
    public void shouldCreateActorWithExternalIds() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setAffiliations(List.of(new ActorId(4L)));
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("ORCID"), "0000-0000-0000-1234"),
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42")
        ));

        String payload = mapper.writeValueAsString(actor);

        String actorJson = mvc.perform(
                post("/api/actors")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", blankOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("externalIds", hasSize(2)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("ORCID")))
                .andExpect(jsonPath("externalIds[0].identifierService.label", is("ORCID")))
                .andExpect(jsonPath("externalIds[0].identifier", is("0000-0000-0000-1234")))
                .andExpect(jsonPath("externalIds[1].identifierService.code", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifierService.label", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifier", is("https://www.wikidata.org/wiki/Q42")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].name", is("CESSDA")))
                .andExpect(jsonPath("affiliations[0].email", is("cessda@cessda.eu")))
                .andReturn().getResponse().getContentAsString();

        ActorDto actorDto = mapper.readValue(actorJson, ActorDto.class);

        mvc.perform(get("/api/actors/{actorId}", actorDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorDto.getId().intValue())))
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", blankOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("externalIds", hasSize(2)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("ORCID")))
                .andExpect(jsonPath("externalIds[0].identifierService.label", is("ORCID")))
                .andExpect(jsonPath("externalIds[0].identifier", is("0000-0000-0000-1234")))
                .andExpect(jsonPath("externalIds[1].identifierService.code", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifierService.label", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifier", is("https://www.wikidata.org/wiki/Q42")))
                .andExpect(jsonPath("affiliations", hasSize(1)))
                .andExpect(jsonPath("affiliations[0].name", is("CESSDA")))
                .andExpect(jsonPath("affiliations[0].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldNotCreateActorWithUnknownExternalId() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setAffiliations(List.of(new ActorId(2L)));
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42"),
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42")
        ));

        String payload = mapper.writeValueAsString(actor);

        mvc.perform(
                post("/api/actors")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].code", is("field.duplicateEntry")));
    }

    @Test
    public void shouldNotCreateActorWithDuplicateExternalId() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setAffiliations(List.of(new ActorId(2L)));
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("None"), "Void")
        ));

        String payload = mapper.writeValueAsString(actor);

        mvc.perform(
                post("/api/actors")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateActorWithMalformedWebsiteAndEmail() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test malformed actor");
        actor.setWebsite("Malformed Website");
        actor.setEmail("Malformed Email");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/actors")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("website")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()))
                .andExpect(jsonPath("errors[1].field", is("email")))
                .andExpect(jsonPath("errors[1].code", is("field.invalid")))
                .andExpect(jsonPath("errors[1].message", notNullValue()));

    }

    @Test
    public void shouldNotCreateActorWhenAffiliationNotExist() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(100l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/actors")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("affiliations[0].id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldUpdateActorWithoutAffiliations() throws Exception {
        Integer actorId = 2;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setWebsite("http://www.example.org");
        actor.setEmail("test@example.org");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorId)))
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", is("http://www.example.org")))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("externalIds", hasSize(0)))
                .andExpect(jsonPath("affiliations", hasSize(0)));
    }

    @Test
    public void shouldUpdateActorWithAffiliations() throws Exception {
        Integer actorId = 2;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(1l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("Test actor")))
                .andExpect(jsonPath("website", blankOrNullString()))
                .andExpect(jsonPath("email", is("test@example.org")))
                .andExpect(jsonPath("affiliations", hasSize(2)))
                .andExpect(jsonPath("affiliations[0].name", is("Austrian Academy of Sciences")))
                .andExpect(jsonPath("affiliations[0].email", blankOrNullString()))
                .andExpect(jsonPath("affiliations[1].name", is("CESSDA")))
                .andExpect(jsonPath("affiliations[1].email", is("cessda@cessda.eu")));
    }

    @Test
    public void shouldNotUpdateActorWhenAffiliationNotExist() throws Exception {
        Integer actorId = 2;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(100l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("affiliations[0].id")))
                .andExpect(jsonPath("errors[0].code", is("field.notExist")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldUpdateActorAndModifyExternalIds() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setAffiliations(List.of(new ActorId(2L)));

        String payload = mapper.writeValueAsString(actor);

        String actorJson = mvc.perform(
                post("/api/actors")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", notNullValue()))
                .andExpect(jsonPath("externalIds", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        ActorDto actorDto = mapper.readValue(actorJson, ActorDto.class);
        int actorId = actorDto.getId().intValue();

        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("ORCID"), "0000-0000-0000-1234"),
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42")
        ));

        payload = mapper.writeValueAsString(actor);

        mvc.perform(
                put("/api/actors/{id}", actorId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorId)))
                .andExpect(jsonPath("externalIds", hasSize(2)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("ORCID")))
                .andExpect(jsonPath("externalIds[0].identifierService.label", is("ORCID")))
                .andExpect(jsonPath("externalIds[0].identifier", is("0000-0000-0000-1234")))
                .andExpect(jsonPath("externalIds[1].identifierService.code", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifierService.label", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifier", is("https://www.wikidata.org/wiki/Q42")));

        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("DBLP"), "https://dblp.org/pers/g/Gray_0001:Jim.html"),
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42")
        ));

        payload = mapper.writeValueAsString(actor);

        mvc.perform(
                put("/api/actors/{id}", actorId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(actorId)))
                .andExpect(jsonPath("externalIds", hasSize(2)))
                .andExpect(jsonPath("externalIds[0].identifierService.code", is("DBLP")))
                .andExpect(jsonPath("externalIds[0].identifierService.label", is("DBLP")))
                .andExpect(jsonPath("externalIds[0].identifier", is("https://dblp.org/pers/g/Gray_0001:Jim.html")))
                .andExpect(jsonPath("externalIds[1].identifierService.code", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifierService.label", is("Wikidata")))
                .andExpect(jsonPath("externalIds[1].identifier", is("https://www.wikidata.org/wiki/Q42")));
    }

    @Test
    public void shouldNotUpdateActorWithUnknownExternalId() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setEmail("test@example.org");
        actor.setAffiliations(List.of(new ActorId(2L)));
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("ORCID"), "0000-0000-0000-1234"),
                new ActorExternalIdCore(new ActorSourceId("None"), "Void")
        ));

        String payload = mapper.writeValueAsString(actor);

        mvc.perform(
                put("/api/actors/{actorId}", 2)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotUpdateActorWhenNotExist() throws Exception {
        Integer actorId = 99;

        ActorCore actor = new ActorCore();
        actor.setName("Test actor");
        actor.setWebsite("http://www.example.org");
        actor.setEmail("test@example.org");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/actors/{id}", actorId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteActor() throws Exception {
        ActorCore actor = new ActorCore();
        actor.setName("Actor to delete");
        actor.setEmail("test@example.org");
        List<ActorId> affiliations = new ArrayList<ActorId>();
        ActorId affiliation1 = new ActorId();
        affiliation1.setId(1l);
        affiliations.add(affiliation1);
        ActorId affiliation2 = new ActorId();
        affiliation2.setId(4l);
        affiliations.add(affiliation2);
        actor.setAffiliations(affiliations);
        actor.setExternalIds(List.of(
                new ActorExternalIdCore(new ActorSourceId("ORCID"), "0000-0000-0000-1234"),
                new ActorExternalIdCore(new ActorSourceId("Wikidata"), "https://www.wikidata.org/wiki/Q42")
        ));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(actor);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/actors")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long actorId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, Actor.class).getId();

        mvc.perform(delete("/api/actors/{id}", actorId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }
}

