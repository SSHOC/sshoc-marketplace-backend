package eu.sshopencloud.marketplace.controllers.datasets;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.services.datasets.DatasetService;
import eu.sshopencloud.marketplace.services.datasets.PaginatedDatasets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DatasetController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final DatasetService datasetService;

    @GetMapping(path = "/datasets", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedDatasets> getDatasets(@RequestParam(value = "page", required = false) Integer page,
                                                         @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        return ResponseEntity.ok(datasetService.getDatasets(page, perpage));
    }

    @GetMapping(path = "/datasets/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Dataset> getDataset(@PathVariable("id") long id) {
        return ResponseEntity.ok(datasetService.getDataset(id));
    }

    @PostMapping(path = "/datasets", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Dataset> createTrainingMaterial(@RequestBody DatasetCore newDataset) {
        return ResponseEntity.ok(datasetService.createDataset(newDataset));
    }

    @PutMapping(path = "/datasets/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Dataset> updateTrainingMaterial(@PathVariable("id") long id, @RequestBody DatasetCore updatedDataset) {
        return ResponseEntity.ok(datasetService.updateDataset(id, updatedDataset));
    }

    @DeleteMapping("/datasets/{id}")
    public void deleteTrainingMaterial(@PathVariable("id") long id) {
        datasetService.deleteDataset(id);
    }

}
