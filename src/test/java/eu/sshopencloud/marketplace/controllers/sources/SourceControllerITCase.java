package eu.sshopencloud.marketplace.controllers.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
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
public class SourceControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String CONTRIBUTOR_JWT;
    private String SYSTEM_IMPORTER_JWT;
    private String MODERATOR_JWT;
    private String ADMINISTRATOR_JWT;

    @Before
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        SYSTEM_IMPORTER_JWT = LogInTestClient.getJwt(mvc, "System importer", "q1w2e3r4t5");
        MODERATOR_JWT = LogInTestClient.getJwt(mvc, "Moderator", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    public void shouldReturnSources() throws Exception {

        mvc.perform(get("/api/sources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnSourcesSortedByLabel() throws Exception {

        SourceCore source = new SourceCore();
        source.setLabel("Source Test");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/sources?order=name")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)))
                .andExpect(jsonPath("sources[0].label", notNullValue()))
                .andExpect(jsonPath("sources[1].label", is(source.getLabel())))
                .andExpect(jsonPath("sources[1].url", is(source.getUrl())))
                .andExpect(jsonPath("sources[2].label", is("TAPoR")))
                .andExpect(jsonPath("sources[2].url", is("http://tapor.ca")))
                .andExpect(jsonPath("sources[2].urlTemplate", is("http://tapor.ca/tools/{source-item-id}")));
    }

    @Test
    public void shouldReturnSourcesSortedByDate() throws Exception {

        SourceCore source = new SourceCore();
        source.setLabel("Test");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/sources?order=date")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(3)));
    }

    @Test
    public void shouldReturnSourcesByLabel() throws Exception {

        mvc.perform(get("/api/sources?q=tapor")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("sources[0].label", is("TAPoR")))
                .andExpect(jsonPath("sources[0].url", is("http://tapor.ca")))
                .andExpect(jsonPath("sources[0].urlTemplate", is("http://tapor.ca/tools/{source-item-id}")));
    }

    @Test
    public void shouldReturnSourcesByPartOfUrl() throws Exception {

        mvc.perform(get("/api/sources?q=historian")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hits", is(1)))
                .andExpect(jsonPath("sources[0].label", is("Programming Historian")))
                .andExpect(jsonPath("sources[0].url", is("https://programminghistorian.org")))
                .andExpect(jsonPath("sources[0].urlTemplate", is("https://programminghistorian.org/en/lessons/{source-item-id}")));
    }

    @Test
    public void shouldReturnSource() throws Exception {
        Integer sourceId = 1;

        mvc.perform(get("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(sourceId)))
                .andExpect(jsonPath("label", is("TAPoR")))
                .andExpect(jsonPath("url", is("http://tapor.ca")))
                .andExpect(jsonPath("urlTemplate", is("http://tapor.ca/tools/{source-item-id}")));
    }

    @Test
    public void shouldNotReturnSourceWhenNotExist() throws Exception {
        Integer sourceId = -1;

        mvc.perform(get("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotCreateSourceWhenUrlIsMalformed() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("url")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldNotCreateSourceWhenUrlTemplateHasNoSourceItemId() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/xxx/yyy/zzz");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", MODERATOR_JWT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field", is("urlTemplate")))
                .andExpect(jsonPath("errors[0].code", is("field.invalid")))
                .andExpect(jsonPath("errors[0].message", notNullValue()));
    }

    @Test
    public void shouldCreateUpdateAndDeleteSource() throws Exception {
        SourceCore source = new SourceCore();
        source.setLabel("Test source");
        source.setUrl("http://example.com");
        source.setUrlTemplate("http://example.com/{source-item-id}");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        String jsonResponse = mvc.perform(post("/api/sources")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long sourceId = TestJsonMapper.serializingObjectMapper().readValue(jsonResponse, SourceDto.class).getId();

        source = new SourceCore();
        source.setLabel("Test another source");
        source.setUrl("http://other.example.com");
        source.setUrlTemplate("http://other.example.com/{source-item-id}");

        payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(source);
        log.debug("JSON: " + payload);

        mvc.perform(put("/api/sources/{id}", sourceId)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(sourceId.intValue())))
                .andExpect(jsonPath("label", is("Test another source")))
                .andExpect(jsonPath("url", is("http://other.example.com")))
                .andExpect(jsonPath("urlTemplate", is("http://other.example.com/{source-item-id}")));

        mvc.perform(delete("/api/sources/{id}", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk());
    }



    @Test
    public void shouldGetItemsBySourceIdAndSourceItemId() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        Long sourceId = 1L;
        source.setId(sourceId);
        tool.setSource(source);
        String sourceItemId = "000000";
        tool.setSourceItemId(sourceItemId);


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to test search by source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("Tool to test search by source")))
                .andExpect(jsonPath("items[0].version", is("5.1")));
    }


    @Test
    public void shouldGetItemsBySourceId() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        Long sourceId = 1L;
        source.setId(sourceId);
        tool.setSource(source);
        String sourceItemId = "000000";
        tool.setSourceItemId(sourceItemId);


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("WebSty")));

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to test search by source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("Tool to test search by source")))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is("WebSty")));
    }


    @Test
    public void shouldGetApprovedItemsBySourceIdAndSourceItemId() throws Exception {
        ToolCore approvedTool = new ToolCore();
        approvedTool.setLabel("Tool to test search by source");
        approvedTool.setVersion("5.1");
        approvedTool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        Long sourceId = 1L;
        source.setId(sourceId);
        approvedTool.setSource(source);
        String sourceItemId = "000000";
        approvedTool.setSourceItemId(sourceItemId);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(approvedTool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        ToolCore suggestedTool = new ToolCore();
        suggestedTool.setLabel("Tool to test search by source for approved (suggested)");
        suggestedTool.setVersion("6.1");
        suggestedTool.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(sourceId);
        suggestedTool.setSource(source);
        suggestedTool.setSourceItemId(sourceItemId);

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestedTool);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/tools-services")
                .content(payload2)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(suggestedTool.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is(suggestedTool.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is(suggestedTool.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", SYSTEM_IMPORTER_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")));
    }


    @Test
    public void shouldGetApprovedItemsWithHistoryBySourceIdAndSourceItemId() throws Exception {
        ToolCore approvedTool = new ToolCore();
        approvedTool.setLabel("Tool to test search by source");
        approvedTool.setVersion("5.1");
        approvedTool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        Long sourceId = 1L;
        source.setId(sourceId);
        approvedTool.setSource(source);
        String sourceItemId = "000000";
        approvedTool.setSourceItemId(sourceItemId);

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(approvedTool);
        log.debug("JSON: " + payload);

        String toolJson = mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();

        ToolCore suggestedVersion = new ToolCore();
        suggestedVersion.setLabel("Tool to test search by source for approved (suggested)");
        suggestedVersion.setVersion("6.1");
        suggestedVersion.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(sourceId);
        suggestedVersion.setSource(source);
        suggestedVersion.setSourceItemId(sourceItemId);

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestedVersion);
        log.debug("JSON: " + payload2);

        mvc.perform(put("/api/tools-services/{toolId}", toolId)
                .content(payload2)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(suggestedVersion.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].persistentId", is(toolId)))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].persistentId", is(toolId)))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].persistentId", is(toolId)))
                .andExpect(jsonPath("items[1].label", is(suggestedVersion.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].persistentId", is(toolId)))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].persistentId", is(toolId)))
                .andExpect(jsonPath("items[1].label", is(suggestedVersion.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")));

        mvc.perform(get("/api/sources/{sourceId}/items/{sourceItemId}", sourceId, sourceItemId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", SYSTEM_IMPORTER_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].persistentId", is(toolId)))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")));
    }


    @Test
    public void shouldGetApprovedItemsBySourceId() throws Exception {
        ToolCore approvedTool = new ToolCore();
        approvedTool.setLabel("Tool to test search by source");
        approvedTool.setVersion("5.1");
        approvedTool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        Long sourceId = 1L;
        source.setId(sourceId);
        approvedTool.setSource(source);
        String sourceItemId = "000000";
        approvedTool.setSourceItemId(sourceItemId);

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("WebSty")));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(approvedTool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to test search by source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        ToolCore suggestedTool = new ToolCore();
        suggestedTool.setLabel("Tool to test search by source for approved (suggested)");
        suggestedTool.setVersion("6.1");
        suggestedTool.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(sourceId);
        suggestedTool.setSource(source);
        suggestedTool.setSourceItemId(sourceItemId);

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestedTool);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/tools-services")
                .content(payload2)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(suggestedTool.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is("WebSty")));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is(suggestedTool.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")))
                .andExpect(jsonPath("items[2].category", is("tool-or-service")))
                .andExpect(jsonPath("items[2].label", is("WebSty")));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is(suggestedTool.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")))
                .andExpect(jsonPath("items[2].category", is("tool-or-service")))
                .andExpect(jsonPath("items[2].label", is("WebSty")));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", SYSTEM_IMPORTER_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is("WebSty")));

    }


    @Test
    public void shouldGetApprovedItemsWithHistoryBySourceId() throws Exception {
        ToolCore approvedTool = new ToolCore();
        approvedTool.setLabel("Tool to test search by source");
        approvedTool.setVersion("5.1");
        approvedTool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        Long sourceId = 1L;
        source.setId(sourceId);
        approvedTool.setSource(source);
        String sourceItemId = "000000";
        approvedTool.setSourceItemId(sourceItemId);

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("WebSty")));

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(approvedTool);
        log.debug("JSON: " + payload);

        String toolJson = mvc.perform(post("/api/tools-services")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to test search by source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)))
                .andReturn().getResponse().getContentAsString();

        ToolDto toolDto = mapper.readValue(toolJson, ToolDto.class);
        String toolId = toolDto.getPersistentId();


        ToolCore suggestedVersion = new ToolCore();
        suggestedVersion.setLabel("Tool to test search by source for approved (suggested)");
        suggestedVersion.setVersion("6.1");
        suggestedVersion.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(sourceId);
        suggestedVersion.setSource(source);
        suggestedVersion.setSourceItemId(sourceItemId);

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(suggestedVersion);
        log.debug("JSON: " + payload2);

        mvc.perform(put("/api/tools-services/{toolId}", toolId)
                .content(payload2)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(suggestedVersion.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("source.id", is(sourceId.intValue())))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")))
                .andExpect(jsonPath("sourceItemId", is(sourceItemId)));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is("WebSty")));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is(suggestedVersion.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")))
                .andExpect(jsonPath("items[2].category", is("tool-or-service")))
                .andExpect(jsonPath("items[2].label", is("WebSty")));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(3)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is(suggestedVersion.getLabel())))
                .andExpect(jsonPath("items[1].version", is("6.1")))
                .andExpect(jsonPath("items[2].category", is("tool-or-service")))
                .andExpect(jsonPath("items[2].label", is("WebSty")));

        mvc.perform(get("/api/sources/{sourceId}/items", sourceId)
                .param("approved", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", SYSTEM_IMPORTER_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is(approvedTool.getLabel())))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is("WebSty")));

    }

}
