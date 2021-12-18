package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.tools.PaginatedTools;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.services.items.ToolService;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(summary = "Retrieve all tools in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTools> getTools(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage,
                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(toolService.getTools(pageCoordsValidator.validate(page, perpage), approved));
    }

    @Operation(summary = "Get single tool by its persistentId")
    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getTool(@PathVariable("persistentId") String persistentId,
                                           @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(toolService.getLatestTool(persistentId, draft, approved));
    }

    @Operation(summary = "Get tool selected version by its persistentId and versionId")
    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getToolVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(toolService.getToolVersion(persistentId, versionId));
    }

    @Operation(summary = "Creating tool")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> createTool(@Parameter(
            description = "Created tool",
            required = true,
            schema = @Schema(implementation = ToolCore.class)) @RequestBody ToolCore newTool,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(toolService.createTool(newTool, draft));
    }


    @Operation(summary = "Updating tool for given persistentId")
    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> updateTool(@PathVariable("persistentId") String persistentId,
                                              @Parameter(
                                                      description = "Updated tool",
                                                      required = true,
                                                      schema = @Schema(implementation = ToolCore.class)) @RequestBody ToolCore updatedTool,
                                              @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                              @RequestParam(value = "approved", defaultValue = "true") boolean approved) throws VersionNotChangedException {

        return ResponseEntity.ok(toolService.updateTool(persistentId, updatedTool, draft, approved));
    }

    @Operation(summary = "Revert tool to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> revertTool(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(toolService.revertTool(persistentId, versionId));
    }

    @Operation(summary = "Delete tool by its persistentId")
    @DeleteMapping(path = "/{persistentId}")
    public void deleteTool(@PathVariable("persistentId") String persistentId, @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        toolService.deleteTool(persistentId, draft);
    }

    @Operation(summary = "Delete tool by its persistentId and versionId")
    @DeleteMapping(path = "/{persistentId}/versions/{versionId}")
    public void deleteTool(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        toolService.deleteTool(persistentId, versionId);
    }


    @Operation(summary = "Committing draft of tool by its persistentId")
    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> publishTool(@PathVariable("persistentId") String persistentId) {
        ToolDto tool = toolService.commitDraftTool(persistentId);
        return ResponseEntity.ok(tool);
    }

    @Operation(summary = "Retrieving history of tool by its persistentId")
    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getToolHistory(@PathVariable("persistentId") String persistentId,
                                                                @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(toolService.getToolVersions(persistentId, draft, approved));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of tool by its persistentId", operationId = "getToolInformationContributors")
    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(toolService.getInformationContributors(persistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of tool by its persistentId and versionId", operationId = "getToolVersionInformationContributors")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(toolService.getInformationContributors(persistentId, versionId));
    }

    @Operation(summary = "Getting body of merged version of tool", operationId = "getToolMerge")
    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> getMerge(@PathVariable("persistentId") String persistentId,
                                            @RequestParam List<String> with) {
        return ResponseEntity.ok(toolService.getMerge(persistentId, with));
    }

    @Operation(summary = "Performing merge into tool", operationId = "mergeTool")
    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolDto> merge(@RequestParam List<String> with,
                                         @Parameter(
                                                 description = "Performing merge into tool",
                                                 required = true,
                                                 schema = @Schema(implementation = ToolCore.class)) @RequestBody ToolCore mergeTool)
            throws ItemIsAlreadyMergedException {
        return ResponseEntity.ok(toolService.merge(mergeTool, with));
    }

    @Operation(summary = "Getting list of sources of tool by its persistentId", operationId = "getToolSources")
    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(toolService.getSources(persistentId));
    }

    @Operation(summary = "Getting differences between tool and target version of item", operationId = "getToolAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getToolVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                               @RequestParam(required = true) String with,
                                                                               @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(toolService.getDifferences(persistentId, null, with, otherVersionId));
    }


    @Operation(summary = "Getting differences between target version of tool and target version of item", operationId = "getVersionedToolAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getVersionedToolVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                                        @PathVariable("versionId") long versionId,
                                                                                        @RequestParam(required = true) String with,
                                                                                        @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(toolService.getDifferences(persistentId, versionId, with, otherVersionId));
    }


}
