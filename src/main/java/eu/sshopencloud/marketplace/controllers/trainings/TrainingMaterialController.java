package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.services.items.TrainingMaterialService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/training-materials")
@RequiredArgsConstructor
public class TrainingMaterialController {

    private final PageCoordsValidator pageCoordsValidator;

    private final TrainingMaterialService trainingMaterialService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTrainingMaterials> getTrainingMaterials(@RequestParam(value = "page", required = false) Integer page,
                                                                           @RequestParam(value = "perpage", required = false) Integer perpage,
                                                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterials(pageCoordsValidator.validate(page, perpage), approved));
    }

    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(trainingMaterialService.getLatestTrainingMaterial(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterialVersion(@PathVariable("persistentId") String persistentId,
                                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterialVersion(persistentId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> createTrainingMaterial(@Parameter(
            description = "Created training material",
            required = true,
            schema = @Schema(implementation = TrainingMaterialCore.class)) @RequestBody TrainingMaterialCore newTrainingMaterial,
                                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(trainingMaterialService.createTrainingMaterial(newTrainingMaterial, draft));
    }

    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> updateTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                                                      @Parameter(
                                                                              description = "Updated training material",
                                                                              required = true,
                                                                              schema = @Schema(implementation = TrainingMaterialCore.class)) @RequestBody TrainingMaterialCore updatedTrainingMaterial,
                                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(trainingMaterialService.updateTrainingMaterial(persistentId, updatedTrainingMaterial, draft));
    }

    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> revertTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                                                      @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.revertTrainingMaterial(persistentId, versionId));
    }

    @DeleteMapping(path = "/{persistentId}")
    public void deleteTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                       @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        trainingMaterialService.deleteTrainingMaterial(persistentId, draft);
    }

    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> publishTrainingMaterial(@PathVariable("persistentId") String persistentId) {
        TrainingMaterialDto trainingMaterial = trainingMaterialService.commitDraftTrainingMaterial(persistentId);
        return ResponseEntity.ok(trainingMaterial);
    }

    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getTrainingMaterialHistory(@PathVariable("persistentId") String persistentId,
                                                                            @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                            @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterialVersions(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(trainingMaterialService.getInformationContributors(persistentId));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.getInformationContributors(persistentId, versionId));
    }


    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getMerge(@PathVariable("persistentId") String persistentId,
                                                        @RequestParam List<String> with) {
        return ResponseEntity.ok(trainingMaterialService.getMerge(persistentId, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> merge(@RequestParam List<String> with,
                                                     @Parameter(
                                                             description = "Merged training material",
                                                             required = true,
                                                             schema = @Schema(implementation = TrainingMaterialCore.class)) @RequestBody TrainingMaterialCore mergeTrainingMaterial) {
        return ResponseEntity.ok(trainingMaterialService.merge(mergeTrainingMaterial, with));
    }

    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(trainingMaterialService.getSources(persistentId));
    }

}
