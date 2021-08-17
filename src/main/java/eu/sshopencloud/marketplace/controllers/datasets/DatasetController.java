package eu.sshopencloud.marketplace.controllers.datasets;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.services.items.DatasetService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final PageCoordsValidator pageCoordsValidator;

    private final DatasetService datasetService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedDatasets> getDatasets(@RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "perpage", required = false) Integer perpage,
                                                         @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(datasetService.getDatasets(pageCoordsValidator.validate(page, perpage), approved));
    }

    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDataset(@PathVariable("persistentId") String persistentId,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                 @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(datasetService.getLatestDataset(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDatasetVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(datasetService.getDatasetVersion(persistentId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> createDataset(@Parameter(
            description = "Created dataset object",
            required = true,
            schema = @Schema(implementation = DatasetCore.class)) @RequestBody DatasetCore newDataset,
                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(datasetService.createDataset(newDataset, draft));
    }

    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> updateDataset(@PathVariable("persistentId") String persistentId,
                                                    @Parameter(
                                                            description = "Update dataset object",
                                                            required = true,
                                                            schema = @Schema(implementation = DatasetCore.class)) @RequestBody DatasetCore updatedDataset,
                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(datasetService.updateDataset(persistentId, updatedDataset, draft));
    }

    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> revertDataset(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(datasetService.revertDataset(persistentId, versionId));
    }

    @DeleteMapping(path = "/{persistentId}")
    public void deleteDataset(@PathVariable("persistentId") String persistentId,
                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        datasetService.deleteDataset(persistentId, draft);
    }

    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> publishDataset(@PathVariable("persistentId") String persistentId) {
        DatasetDto dataset = datasetService.commitDraftDataset(persistentId);
        return ResponseEntity.ok(dataset);
    }

    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getDatasetHistory(@PathVariable("persistentId") String persistentId,
                                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(datasetService.getDatasetVersions(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(datasetService.getInformationContributors(persistentId));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(datasetService.getInformationContributors(persistentId, versionId));
    }


    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getMerge(@PathVariable("persistentId") String persistentId,
                                               @RequestParam List<String> with) {
        return ResponseEntity.ok(datasetService.getMerge(persistentId, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> merge(@RequestParam List<String> with,
                                            @Parameter(
                                                    description = "Merge into dataset object",
                                                    required = true,
                                                    schema = @Schema(implementation = DatasetCore.class)) @RequestBody DatasetCore mergeDataset) {
        return ResponseEntity.ok(datasetService.merge(mergeDataset, with));
    }

    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(datasetService.getSources(persistentId));
    }

}
