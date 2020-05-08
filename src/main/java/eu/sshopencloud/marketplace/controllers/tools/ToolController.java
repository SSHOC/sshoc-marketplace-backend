package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.services.tools.ToolService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final PageCoordsValidator pageCoordsValidator;

    private final ToolService toolService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTools> getTools(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(toolService.getTools(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getTool(@PathVariable("id") long id) {
        return ResponseEntity.ok(toolService.getTool(id));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> createTool(@RequestBody ToolCore newTool) {
        return ResponseEntity.ok(toolService.createTool(newTool));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> updateTool(@PathVariable("id") long id, @RequestBody ToolCore updatedTool) {
        return ResponseEntity.ok(toolService.updateTool(id, updatedTool));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteTool(@PathVariable("id") long id) {
        toolService.deleteTool(id);
    }

}
