package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.services.tools.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @GetMapping("/tools")
    public ResponseEntity<List<Tool>> getAllTools() {
        List<Tool> tools = toolService.getAllTools();
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/tools/{id}")
    public ResponseEntity<Tool> getTool(@PathVariable("id") long id) {
        Tool tool = toolService.getTool(id);
        return ResponseEntity.ok(tool);
    }

}
