package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.services.tools.PaginatedTools;
import eu.sshopencloud.marketplace.services.tools.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @GetMapping("/tools")
    public ResponseEntity<PaginatedTools> getTools(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage) {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            return ResponseEntity.badRequest().build();
        }
        page = page == null ? 1 : page;

        PaginatedTools tools = toolService.getTools(page, perpage);
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/tools/{id}")
    public ResponseEntity<Tool> getTool(@PathVariable("id") long id) {
        Tool tool = toolService.getTool(id);
        return ResponseEntity.ok(tool);
    }

    @DeleteMapping("/tools/{id}")
    public void deleteTool(@PathVariable("id") long id) {
        toolService.deleteTool(id);
    }

}
