package eu.sshopencloud.marketplace.controllers.publications;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferenceDto;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.services.items.PublicationService;
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
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PageCoordsValidator pageCoordsValidator;

    private final PublicationService publicationService;

    @Operation(summary = "Retrieve all publications in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedPublications> getPublications(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage,
                                                                 @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {

        return ResponseEntity.ok(publicationService.getPublications(pageCoordsValidator.validate(page, perpage), approved));
    }

    @Operation(summary = "Get single publication by its persistentId")
    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublication(@PathVariable("persistentId") String persistentId,
                                                         @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                         @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(publicationService.getLatestPublication(persistentId, draft, approved));
    }

    @Operation(summary = "Get publication selected version by its persistentId and versionId")
    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublicationVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.getPublicationVersion(persistentId, versionId));
    }

    @Operation(summary = "Creating publication")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> createPublication(@Parameter(
            description = "Created publication",
            required = true,
            schema = @Schema(implementation = PublicationCore.class)) @RequestBody PublicationCore newPublication,
                                                            @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(publicationService.createPublication(newPublication, draft));
    }

    @Operation(summary = "Updating publication for given persistentId")
    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> updatePublication(@PathVariable("persistentId") String persistentId,
                                                            @Parameter(
                                                                    description = "Updated publication object",
                                                                    required = true,
                                                                    schema = @Schema(implementation = PublicationCore.class)) @RequestBody PublicationCore updatedPublication,
                                                            @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft,
                                                            @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(publicationService.updatePublication(persistentId, updatedPublication, draft, approved));
    }

    @Operation(summary = "Revert publication to target version by its persistentId and versionId that is reverted to")
    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> revertPublication(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.revertPublication(persistentId, versionId));
    }

    @Operation(summary = "Delete publication by its persistentId")
    @DeleteMapping(path = "/{persistentId}")
    public void deletePublication(@PathVariable("persistentId") String persistentId,
                                  @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        publicationService.deletePublication(persistentId, draft);
    }

    @Operation(summary = "Committing draft of publication by its persistentId")
    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> publishPublication(@PathVariable("persistentId") String persistentId) {
        PublicationDto publication = publicationService.commitDraftPublication(persistentId);
        return ResponseEntity.ok(publication);
    }

    @Operation(summary = "Retrieving history of publication by its persistentId")
    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getPublicationHistory(@PathVariable("persistentId") String persistentId,
                                                                       @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                       @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(publicationService.getPublicationVersions(persistentId, draft, approved));
    }

    @Operation(summary = "Retrieving list of information-contributors across the whole history of publication by its persistentId", operationId = "getPublicationInformationContributors")
    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(publicationService.getInformationContributors(persistentId));
    }

    @Operation(summary = "Retrieving list of information-contributors to the selected version of publication by its persistentId and versionId", operationId = "getPublicationVersionInformationContributors")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(publicationService.getInformationContributors(persistentId, versionId));
    }

    @Operation(summary = "Getting body of merged version of publication", operationId = "getPublicationMerge")
    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getMerge(@PathVariable("persistentId") String persistentId,
                                                   @RequestParam List<String> with) {
        return ResponseEntity.ok(publicationService.getMerge(persistentId, with));
    }

    @Operation(summary = "Performing merged into publication", operationId = "mergePublication")
    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> merge(@RequestParam List<String> with,
                                                @Parameter(
                                                        description = "Merged publication",
                                                        required = true,
                                                        schema = @Schema(implementation = PublicationCore.class)) @RequestBody PublicationCore mergePublication) {
        return ResponseEntity.ok(publicationService.merge(mergePublication, with));
    }

    @Operation(summary = "Getting list of sources of publication by its persistentId", operationId = "getPublicationSources")
    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(publicationService.getSources(persistentId));
    }


    @Operation(summary = "Getting differences between publication and target version of item", operationId = "getPublicationAndVersionedItemDifference")
    @GetMapping(path = "/{persistentId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferenceDto> getPublicationVersionedItemDifference(@PathVariable("persistentId") String persistentId,
                                                                                    @RequestParam String with,
                                                                                    @RequestParam Long otherVersionId) {

        return ResponseEntity.ok(publicationService.getDifference(persistentId, null, with, otherVersionId));
    }


    @Operation(summary = "Getting differences between target version of publication and target version of item", operationId = "getVersionedPublicationAndVersionedItemDifference")
    @GetMapping(path = "/{persistentId}/versions/{versionId}/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemsDifferenceDto> getVersionedPublicationVersionedItemDifference(@PathVariable("persistentId") String persistentId,
                                                                                             @PathVariable("versionId") long versionId,
                                                                                             @RequestParam String with,
                                                                                             @RequestParam Long otherVersionId) {

        return ResponseEntity.ok(publicationService.getDifference(persistentId, versionId, with, otherVersionId));
    }


}
