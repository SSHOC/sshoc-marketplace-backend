package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.services.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.services.trainings.TrainingMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingMaterialController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final TrainingMaterialService trainingMaterialService;

    @GetMapping(path = "/training-materials", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTrainingMaterials> getTrainingMaterials(@RequestParam(value = "page", required = false) Integer page,
                                                                           @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            throw new PageTooLargeException(maximalPerpage);
        }
        page = page == null ? 1 : page;

        PaginatedTrainingMaterials trainingMaterials = trainingMaterialService.getTrainingMaterials(page, perpage);
        return ResponseEntity.ok(trainingMaterials);
    }

    @GetMapping(path = "/training-materials/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterial> getTrainingMaterial(@PathVariable("id") long id) {
        TrainingMaterial trainingMaterial = trainingMaterialService.getTrainingMaterial(id);
        return ResponseEntity.ok(trainingMaterial);
    }

    @PostMapping(path = "/training-materials", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterial> createTrainingMaterial(@RequestBody TrainingMaterial newTrainingMaterial) {
        TrainingMaterial trainingMaterial = trainingMaterialService.createTrainingMaterial(newTrainingMaterial);
        return ResponseEntity.ok(trainingMaterial);
    }

    @PutMapping(path = "/training-materials/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterial> updateTrainingMaterial(@PathVariable("id") long id, @RequestBody TrainingMaterial newTrainingMaterial) {
        TrainingMaterial trainingMaterial = trainingMaterialService.updateTrainingMaterial(id, newTrainingMaterial);
        return ResponseEntity.ok(trainingMaterial);
    }

    @DeleteMapping("/training-materials/{id}")
    public void deleteTrainingMaterial(@PathVariable("id") long id) {
        trainingMaterialService.deleteTrainingMaterial(id);
    }

}
