package eu.sshopencloud.marketplace.controllers.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolTypeId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ToolControllerITCase {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnTools() throws Exception {

        mvc.perform(get("/api/tools")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnTool() throws Exception {
        Integer toolId = 1;

        mvc.perform(get("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(toolId)))
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Gephi")));
    }

    @Test
    public void shouldntReturnToolWhenNotExist() throws Exception {
        Integer toolId = 51;

        mvc.perform(get("/api/tools/{id}", toolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldCreateTool() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("software");
        tool.setToolType(toolType);
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("category", is("tool")))
                .andExpect(jsonPath("label", is("Test Software")));
    }

    @Test
    public void shouldCreateToolWhenTypeIsIncorrect() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("xxx");
        tool.setToolType(toolType);
        tool.setLabel("Test Software");
        tool.setDescription("Lorem ipsum");

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

    @Test
    public void shouldCreateToolWhenLabelIsNull() throws Exception {
        ToolCore tool = new ToolCore();
        ToolTypeId toolType = new ToolTypeId();
        toolType.setCode("service");
        tool.setToolType(toolType);
        tool.setDescription("Lorem ipsum");

        mvc.perform(post("/api/tools")
                .content(new ObjectMapper().writeValueAsString(tool))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", not(isEmptyOrNullString())));
    }

}
