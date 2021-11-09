package eu.sshopencloud.marketplace.controllers.datasets;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.services.items.DatasetService;
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
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final PageCoordsValidator pageCoordsValidator;

    private final DatasetService datasetService;

    @Operation(summary = "Retrieve all datasets in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedDatasets> getDatasets(@RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "perpage", required = false) Integer perpage,
                                                         @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(datasetService.getDatasets(pageCoordsValidator.validate(page, perpage), approved));
    }

    @Operation(summary = "Get single dataset by its persistentId")
    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDataset(@PathVariable("persistentId") String persistentId,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                 @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(datasetService.getLatestDataset(persistentId, draft, approved));
    }

    @Operation(summary = "Get dataset selected version by its persistentId and versionId")
    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDatasetVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(datasetService.getDatasetVersion(persistentId, versionId));
    }

    @Operation(summary = "Creating dataset")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> createDataset(@Parameter(
            description = "Created dataset",
            required = true,
            schema = @Schema(implementation = DatasetCore.class)) @RequestBody DatasetCore newDataset,
                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(datasetService.createDataset(newDataset, draft));
    }

    @Operation(summary = "Updating dataset for given persistentId")
    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> updateDataset(@PathVariable("persistentId") String persistentId,
                                                    @Parameter(
                                                            description = "Updated dataset",
                                                            required = true,
                                                            schema = @Schema(implementation = DatasetCore.class)) @RequestBody DatasetCore updatedDataset,
                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                    @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(datasetService.updateDataset(persistentId, updatedDataset, draft, approved));
    }

    @Operation(summary = "Revert dataset to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> revertDataset(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(datasetService.revertDataset(persistentId, versionId));
    }

    @Operation(summary = "Delete dataset by its persistentId")
    @DeleteMapping(path = "/{persistentId}")
    public void deleteDataset(@PathVariable("persistentId") String persistentId,
                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        datasetService.deleteDataset(persistentId, draft);
    }

    @Operation(summary = "Delete dataset by its persistentId and versionId")
    @DeleteMapping(path = "/{persistentId}/versions/{versionId}")
    public void deleteDataset(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        datasetService.deleteDataset(persistentId, versionId);
    }


    @Operation(summary = "Committing draft of dataset by its persistentId")
    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> publishDataset(@PathVariable("persistentId") String persistentId) {
        DatasetDto dataset = datasetService.commitDraftDataset(persistentId);
        return ResponseEntity.ok(dataset);
    }

    @Operation(summary = "Retrieving history of dataset by its persistentId")
    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getDatasetHistory(@PathVariable("persistentId") String persistentId,
                                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(datasetService.getDatasetVersions(persistentId, draft, approved));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of dataset by its persistentId", operationId = "getDatasetInformationContributors")
    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(datasetService.getInformationContributors(persistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of dataset by its persistentId and versionId", operationId = "getDatasetVersionInformationContributors")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(datasetService.getInformationContributors(persistentId, versionId));
    }

    @Operation(summary = "Getting body of merged version of dataset", operationId = "getDatasetMerge")
    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getMerge(@PathVariable("persistentId") String persistentId,
                                               @RequestParam List<String> with) {
        return ResponseEntity.ok(datasetService.getMerge(persistentId, with));
    }

    @Operation(summary = "Performing merge into dataset", operationId = "mergeDataset")
    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> merge(@RequestParam List<String> with,
                                            @Parameter(
                                                    description = "Merged dataset",
                                                    required = true,
                                                    schema = @Schema(implementation = DatasetCore.class)) @RequestBody DatasetCore mergeDataset) {
        return ResponseEntity.ok(datasetService.merge(mergeDataset, with));
    }

    @Operation(summary = "Getting list of sources of dataset by its persistentId", operationId = "getDatasetSources")
    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(datasetService.getSources(persistentId));
    }

}
