package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.services.items.ToolService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/tools-services")
@RequiredArgsConstructor
public class ToolController {

    private final PageCoordsValidator pageCoordsValidator;

    private final ToolService toolService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTools> getTools(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage,
                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(toolService.getTools(pageCoordsValidator.validate(page, perpage), approved));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getTool(@PathVariable("id") String id,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(toolService.getLatestTool(id, draft, approved));
    }

    @GetMapping(path = "/{id}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getToolVersion(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(toolService.getToolVersion(id, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> createTool(@RequestBody ToolCore newTool,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(toolService.createTool(newTool, draft));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> updateTool(@PathVariable("id") String id, @RequestBody ToolCore updatedTool,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(toolService.updateTool(id, updatedTool, draft));
    }

    @PutMapping(path = "/{id}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> revertTool(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(toolService.revertTool(id, versionId));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteTool(@PathVariable("id") String id, @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        toolService.deleteTool(id, draft);
    }

    @PostMapping(path = "/{toolId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> publishTool(@PathVariable("toolId") String toolId) {
        ToolDto tool = toolService.commitDraftTool(toolId);
        return ResponseEntity.ok(tool);
    }

    @GetMapping(path = "/{id}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getToolHistory(@PathVariable("id") String id,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(toolService.getToolVersions(id, draft, approved));
    }

    @GetMapping(path = "/{id}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("id") String id) {

        return ResponseEntity.ok(toolService.getInformationContributors(id));
    }

    @GetMapping(path = "/{id}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(toolService.getInformationContributors(id, versionId));
    }

    @GetMapping(path = "/{id}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getMerge(@PathVariable("id") String id,
                                            @RequestParam List<String> with) {
        return ResponseEntity.ok(toolService.getMerge(id, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> merge(@RequestParam List<String> with,
                                         @RequestBody ToolCore mergePublication) {
        return ResponseEntity.ok(toolService.merge(mergePublication, with));
    }

}
