package eu.sshopencloud.marketplace.controllers.datasets;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.services.items.DatasetService;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final PageCoordsValidator pageCoordsValidator;

    private final DatasetService datasetService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedDatasets> getDatasets(@RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(datasetService.getDatasets(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> getDataset(@PathVariable("id") String id) {
        return ResponseEntity.ok(datasetService.getLatestDataset(id));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> createDataset(@RequestBody DatasetCore newDataset) {
        return ResponseEntity.ok(datasetService.createDataset(newDataset));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DatasetDto> updateDataset(@PathVariable("id") String id, @RequestBody DatasetCore updatedDataset) {
        return ResponseEntity.ok(datasetService.updateDataset(id, updatedDataset));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteDataset(@PathVariable("id") String id) {
        datasetService.deleteDataset(id);
    }

}
