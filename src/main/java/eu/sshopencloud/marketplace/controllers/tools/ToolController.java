package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
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

    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getTool(@PathVariable("persistentId") String persistentId,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(toolService.getLatestTool(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getToolVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(toolService.getToolVersion(persistentId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> createTool(@RequestBody ToolCore newTool,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(toolService.createTool(newTool, draft));
    }

    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> updateTool(@PathVariable("persistentId") String persistentId, @RequestBody ToolCore updatedTool,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(toolService.updateTool(persistentId, updatedTool, draft));
    }

    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> revertTool(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(toolService.revertTool(persistentId, versionId));
    }

    @DeleteMapping(path = "/{persistentId}")
    public void deleteTool(@PathVariable("persistentId") String persistentId, @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        toolService.deleteTool(persistentId, draft);
    }

    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> publishTool(@PathVariable("persistentId") String persistentId) {
        ToolDto tool = toolService.commitDraftTool(persistentId);
        return ResponseEntity.ok(tool);
    }

    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getToolHistory(@PathVariable("persistentId") String persistentId,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(toolService.getToolVersions(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(toolService.getInformationContributors(persistentId));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(toolService.getInformationContributors(persistentId, versionId));
    }

    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getMerge(@PathVariable("persistentId") String persistentId,
                                            @RequestParam List<String> with) {
        return ResponseEntity.ok(toolService.getMerge(persistentId, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> merge(@RequestParam List<String> with,
                                         @RequestBody ToolCore mergeTool) {
        return ResponseEntity.ok(toolService.merge(mergeTool, with));
    }

    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(toolService.getSources(persistentId));
    }

}
