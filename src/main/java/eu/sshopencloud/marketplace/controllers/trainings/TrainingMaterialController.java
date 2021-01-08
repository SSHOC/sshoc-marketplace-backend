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
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterial(@PathVariable("id") String id,
                                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(trainingMaterialService.getLatestTrainingMaterial(id, draft, approved));
    }

    @GetMapping(path = "/{id}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterialVersion(@PathVariable("id") String id,
                                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterialVersion(id, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> createTrainingMaterial(@RequestBody TrainingMaterialCore newTrainingMaterial,
                                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(trainingMaterialService.createTrainingMaterial(newTrainingMaterial, draft));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> updateTrainingMaterial(@PathVariable("id") String id,
                                                                      @RequestBody TrainingMaterialCore updatedTrainingMaterial,
                                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(trainingMaterialService.updateTrainingMaterial(id, updatedTrainingMaterial, draft));
    }

    @PutMapping(path = "/{id}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> revertTrainingMaterial(@PathVariable("id") String id,
                                                                      @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.revertTrainingMaterial(id, versionId));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteTrainingMaterial(@PathVariable("id") String id,
                                       @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        trainingMaterialService.deleteTrainingMaterial(id, draft);
    }

    @PostMapping(path = "/{trainingMaterialId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> publishTrainingMaterial(@PathVariable("trainingMaterialId") String trainingMaterialId) {
        TrainingMaterialDto trainingMaterial = trainingMaterialService.commitDraftTrainingMaterial(trainingMaterialId);
        return ResponseEntity.ok(trainingMaterial);
    }
}
