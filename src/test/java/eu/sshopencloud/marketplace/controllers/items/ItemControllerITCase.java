package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.conf.TestJsonMapper;
import eu.sshopencloud.marketplace.conf.auth.LogInTestClient;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class ItemControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String CONTRIBUTOR_JWT;
    private String ADMINISTRATOR_JWT;

    @Before
    public void init()
            throws Exception {
        CONTRIBUTOR_JWT = LogInTestClient.getJwt(mvc, "Contributor", "q1w2e3r4t5");
        ADMINISTRATOR_JWT = LogInTestClient.getJwt(mvc, "Administrator", "q1w2e3r4t5");
    }

    @Test
    public void shouldGetDraftsInTimeDescendingOrder() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(1L);
        tool.setSource(source);
        tool.setSourceItemId("000000");

        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(post("/api/tools-services?draft=true")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is("Tool to test search by source")))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));

        ToolCore tool2 = new ToolCore();
        tool2.setLabel("Tool second");
        tool2.setVersion("10.0");
        tool2.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(2L);
        tool2.setSource(source2);
        tool2.setSourceItemId("000000");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool2);
        log.debug("JSON: " + payload2);


        mvc.perform(post("/api/tools-services?draft=true")
                        .content(payload2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(tool2.getLabel())))
                .andExpect(jsonPath("description", is(tool2.getDescription())))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("source.id", is(2)))
                .andExpect(jsonPath("source.label", is("Programming Historian")))
                .andExpect(jsonPath("source.url", is("https://programminghistorian.org")));

        mvc.perform(get("/api/draft-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].label", is(tool2.getLabel())))
                .andExpect(jsonPath("items[0].lastInfoUpdate", notNullValue()))
                .andExpect(jsonPath("items[1].label", is(tool.getLabel())))
                .andExpect(jsonPath("items[1].lastInfoUpdate", notNullValue()));
    }


    @Test
    public void shouldCreateToolAnsSearchBySourceIdAndSourceItemId() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(1L);
        tool.setSource(source);
        tool.setSourceItemId("000000");


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
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));


        mvc.perform(get("/api/sources/1/items/000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("Tool to test search by source")))
                .andExpect(jsonPath("items[0].version", is("5.1")));

    }


    @Test
    public void shouldCreateToolAndSearchBySourceId() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(1L);
        tool.setSource(source);
        tool.setSourceItemId("000000");


        String payload = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool);
        log.debug("JSON: " + payload);

        mvc.perform(get("/api/sources/1/items")
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
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));


        mvc.perform(get("/api/sources/1/items")
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
    public void shouldCreateToolWithSourceAndFindOnlyApproved() throws Exception {
        ToolCore tool = new ToolCore();
        tool.setLabel("Tool to test search by source");
        tool.setVersion("5.1");
        tool.setDescription("Lorem ipsum");
        SourceId source = new SourceId();
        source.setId(1L);
        tool.setSource(source);
        tool.setSourceItemId("000000");

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
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));

        ToolCore tool2 = new ToolCore();
        tool2.setLabel("Tool to test search by source for approved (not draft)");
        tool2.setVersion("6.1");
        tool2.setDescription("Lorem ipsum");
        SourceId source2 = new SourceId();
        source2.setId(1L);
        tool2.setSource(source);
        tool2.setSourceItemId("000000");

        String payload2 = TestJsonMapper.serializingObjectMapper().writeValueAsString(tool2);
        log.debug("JSON: " + payload2);

        mvc.perform(post("/api/tools-services")
                        .content(payload2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", CONTRIBUTOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool-or-service")))
                .andExpect(jsonPath("label", is(tool2.getLabel())))
                .andExpect(jsonPath("description", is("Lorem ipsum")))
                .andExpect(jsonPath("properties", hasSize(0)))
                .andExpect(jsonPath("status", is("suggested")))
                .andExpect(jsonPath("source.id", is(1)))
                .andExpect(jsonPath("source.label", is("TAPoR")))
                .andExpect(jsonPath("source.url", is("http://tapor.ca")));

        mvc.perform(get("/api/sources/1/items/000000")
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(1)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("Tool to test search by source")))
                .andExpect(jsonPath("items[0].version", is("5.1")));

        mvc.perform(get("/api/sources/1/items/000000")
                        .param("approved", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ADMINISTRATOR_JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("items", hasSize(2)))
                .andExpect(jsonPath("items[0].category", is("tool-or-service")))
                .andExpect(jsonPath("items[0].label", is("Tool to test search by source")))
                .andExpect(jsonPath("items[0].version", is("5.1")))
                .andExpect(jsonPath("items[1].category", is("tool-or-service")))
                .andExpect(jsonPath("items[1].label", is("Tool to test search by source for approved (not draft)")))
                .andExpect(jsonPath("items[1].version", is("6.1")));

    }

}
