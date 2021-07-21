package eu.sshopencloud.marketplace.controllers.datasets;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.services.items.DatasetService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
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

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDataset(@PathVariable("id") String id,
                                                 @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                 @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(datasetService.getLatestDataset(id, draft, approved));
    }

    @GetMapping(path = "/{id}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDatasetVersion(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(datasetService.getDatasetVersion(id, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> createDataset(@RequestBody DatasetCore newDataset,
                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(datasetService.createDataset(newDataset, draft));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> updateDataset(@PathVariable("id") String id, @RequestBody DatasetCore updatedDataset,
                                                    @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(datasetService.updateDataset(id, updatedDataset, draft));
    }

    @PutMapping(path = "/{id}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> revertDataset(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(datasetService.revertDataset(id, versionId));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteDataset(@PathVariable("id") String id,
                              @RequestParam(value = "draft", defaultValue = "false") boolean draft) {

        datasetService.deleteDataset(id, draft);
    }

    @PostMapping(path = "/{id}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> publishDataset(@PathVariable("id") String id) {
        DatasetDto dataset = datasetService.commitDraftDataset(id);
        return ResponseEntity.ok(dataset);
    }

    @GetMapping(path = "/{id}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getDatasetHistory(@PathVariable("id") String id,
                                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(datasetService.getDatasetVersions(id, draft, approved));
    }

    @GetMapping(path = "/{id}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("id") String id) {

        return ResponseEntity.ok(datasetService.getInformationContributors(id));
    }

    @GetMapping(path = "/{id}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(datasetService.getInformationContributors(id, versionId));
    }


    @GetMapping(path = "/{id}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getMerge(@PathVariable("id") String id,
                                               @RequestParam List<String> with) {
        return ResponseEntity.ok(datasetService.getMerge(id, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> merge(@RequestParam List<String> with,
                                            @RequestBody DatasetCore mergeDataset) {
        return ResponseEntity.ok(datasetService.merge(mergeDataset, with));
    }


}
