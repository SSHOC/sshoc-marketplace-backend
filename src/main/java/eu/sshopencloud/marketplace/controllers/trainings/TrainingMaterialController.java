package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.services.items.TrainingMaterialService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/training-materials")
@RequiredArgsConstructor
public class TrainingMaterialController {

    private final PageCoordsValidator pageCoordsValidator;

    private final TrainingMaterialService trainingMaterialService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTrainingMaterials> getTrainingMaterials(@RequestParam(value = "page", required = false) Integer page,
                                                                           @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterials(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterial(@PathVariable("id") String id) {
        return ResponseEntity.ok(trainingMaterialService.getLatestTrainingMaterial(id));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> createTrainingMaterial(@RequestBody TrainingMaterialCore newTrainingMaterial) {
        return ResponseEntity.ok(trainingMaterialService.createTrainingMaterial(newTrainingMaterial));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> updateTrainingMaterial(@PathVariable("id") String id,
                                                                      @RequestBody TrainingMaterialCore updatedTrainingMaterial) {

        return ResponseEntity.ok(trainingMaterialService.updateTrainingMaterial(id, updatedTrainingMaterial));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteTrainingMaterial(@PathVariable("id") String id) {
        trainingMaterialService.deleteTrainingMaterial(id);
    }
}
