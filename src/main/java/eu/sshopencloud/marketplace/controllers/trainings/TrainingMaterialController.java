package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.services.items.TrainingMaterialService;
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
@RequestMapping("/api/training-materials")
@RequiredArgsConstructor
public class TrainingMaterialController {

    private final PageCoordsValidator pageCoordsValidator;

    private final TrainingMaterialService trainingMaterialService;

    @Operation(summary = "Retrieve all training materials in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedTrainingMaterials> getTrainingMaterials(@RequestParam(value = "page", required = false) Integer page,
                                                                           @RequestParam(value = "perpage", required = false) Integer perpage,
                                                                           @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {
        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterials(pageCoordsValidator.validate(page, perpage), approved));
    }

    @Operation(summary = "Get single training material by its persistentId")
    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                                                   @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                   @RequestParam(value = "approved", defaultValue = "true") boolean approved,
                                                                   @RequestParam(value = "redirect", defaultValue = "false") boolean redirect) {

        return ResponseEntity.ok(trainingMaterialService.getLatestTrainingMaterial(persistentId, draft, approved, redirect));
    }

    @Operation(summary = "Get training material selected version by its persistentId and versionId")
    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getTrainingMaterialVersion(@PathVariable("persistentId") String persistentId,
                                                                          @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterialVersion(persistentId, versionId));
    }

    @Operation(summary = "Creating training material")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> createTrainingMaterial(@Parameter(
            description = "Created training material",
            required = true,
            schema = @Schema(implementation = TrainingMaterialCore.class)) @RequestBody TrainingMaterialCore newTrainingMaterial,
                                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(trainingMaterialService.createTrainingMaterial(newTrainingMaterial, draft));
    }

    @Operation(summary = "Updating training material for given persistentId")
    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> updateTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                                                      @Parameter(
                                                                              description = "Updated training material",
                                                                              required = true,
                                                                              schema = @Schema(implementation = TrainingMaterialCore.class)) @RequestBody TrainingMaterialCore updatedTrainingMaterial,
                                                                      @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft,
                                                                      @RequestParam(value = "approved", defaultValue = "true") boolean approved) throws VersionNotChangedException {

        return ResponseEntity.ok(trainingMaterialService.updateTrainingMaterial(persistentId, updatedTrainingMaterial, draft, approved));
    }

    @Operation(summary = "Revert training material to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> revertTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                                                      @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.revertTrainingMaterial(persistentId, versionId));
    }

    @Operation(summary = "Delete training material by its persistentId")
    @DeleteMapping(path = "/{persistentId}")
    public void deleteTrainingMaterial(@PathVariable("persistentId") String persistentId,
                                       @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        trainingMaterialService.deleteTrainingMaterial(persistentId, draft);
    }

    @Operation(summary = "Delete training material by its persistentId and versionId")
    @DeleteMapping(path = "/{persistentId}/versions/{versionId}")
    public void deleteTrainingMaterialVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        trainingMaterialService.deleteTrainingMaterial(persistentId, versionId);
    }

    @Operation(summary = "Committing draft of training material by its persistentId")
    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> publishTrainingMaterial(@PathVariable("persistentId") String persistentId) {
        TrainingMaterialDto trainingMaterial = trainingMaterialService.commitDraftTrainingMaterial(persistentId);
        return ResponseEntity.ok(trainingMaterial);
    }

    @Operation(summary = "Retrieving history of training material by its persistentId")
    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getTrainingMaterialHistory(@PathVariable("persistentId") String persistentId,
                                                                            @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                            @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(trainingMaterialService.getTrainingMaterialVersions(persistentId, draft, approved));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of training material by its persistentId", operationId = "getTrainingMaterialInformationContributors")
    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(trainingMaterialService.getInformationContributors(persistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of training material by its persistentId and versionId", operationId = "getTrainingMaterialVersionInformationContributors")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(trainingMaterialService.getInformationContributors(persistentId, versionId));
    }


    @Operation(summary = "Getting body of merged version of training material", operationId = "getTrainingMaterialMerge")
    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> getMerge(@PathVariable("persistentId") String persistentId,
                                                        @RequestParam List<String> with) {
        return ResponseEntity.ok(trainingMaterialService.getMerge(persistentId, with));
    }

    @Operation(summary = "Performing merge into training material", operationId = "mergeTrainingMaterial")
    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingMaterialDto> merge(@RequestParam List<String> with,
                                                     @Parameter(
                                                             description = "Merged training material",
                                                             required = true,
                                                             schema = @Schema(implementation = TrainingMaterialCore.class)) @RequestBody TrainingMaterialCore mergeTrainingMaterial)
            throws ItemIsAlreadyMergedException {
        return ResponseEntity.ok(trainingMaterialService.merge(mergeTrainingMaterial, with));
    }

    @Operation(summary = "Getting list of sources of training material by its persistentId", operationId = "getTrainingMaterialSources")
    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(trainingMaterialService.getSources(persistentId));
    }

    @Operation(summary = "Getting differences between training material and target version of item ('unaltered' string response means for the single field that remained unchanged)", operationId = "getTrainingMaterialAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getTrainingMaterialVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                                           @RequestParam(required = true) String with,
                                                                                           @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(trainingMaterialService.getDifferences(persistentId, null, with, otherVersionId));
    }


    @Operation(summary = "Getting differences between target version of training material and target version of item ('unaltered' string response means for the single field that remained unchanged)", operationId = "getVersionedTrainingMaterialAndVersionedItemDifferences")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferencesDto> getVersionedTrainingMaterialVersionedItemDifferences(@PathVariable("persistentId") String persistentId,
                                                                                                    @PathVariable("versionId") long versionId,
                                                                                                    @RequestParam(required = true) String with,
                                                                                                    @RequestParam(required = false) Long otherVersionId) {

        return ResponseEntity.ok(trainingMaterialService.getDifferences(persistentId, versionId, with, otherVersionId));
    }


}
