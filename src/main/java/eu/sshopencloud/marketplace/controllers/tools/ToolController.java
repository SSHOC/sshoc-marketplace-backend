package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.services.DataViolationException;
import eu.sshopencloud.marketplace.services.tools.DisallowedToolTypeChangeException;
import eu.sshopencloud.marketplace.services.tools.PaginatedTools;
import eu.sshopencloud.marketplace.services.tools.ToolService;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptDisallowedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final ToolService toolService;

    @GetMapping(path = "/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTools> getTools(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        PaginatedTools tools = toolService.getTools(page, perpage);
        return ResponseEntity.ok(tools);
    }

    @GetMapping(path = "/tools/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tool> getTool(@PathVariable("id") long id) {
        Tool tool = toolService.getTool(id);
        return ResponseEntity.ok(tool);
    }

    @PostMapping(path = "/tools", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tool> createTool(@RequestBody ToolCore newTool)
            throws DataViolationException, ConceptDisallowedException {
        Tool tool = toolService.createTool(newTool);
        return ResponseEntity.ok(tool);
    }

    @PutMapping(path = "/tools/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tool> updateTool(@PathVariable("id") long id, @RequestBody ToolCore newTool)
            throws DataViolationException, ConceptDisallowedException, DisallowedToolTypeChangeException {
        Tool tool = toolService.updateTool(id, newTool);
        return ResponseEntity.ok(tool);
    }

    @DeleteMapping("/tools/{id}")
    public void deleteTool(@PathVariable("id") long id) {
        toolService.deleteTool(id);
    }

}
