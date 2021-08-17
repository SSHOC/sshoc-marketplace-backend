package eu.sshopencloud.marketplace.controllers.publications;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.services.items.PublicationService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
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

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedPublications> getPublications(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage,
                                                                 @RequestParam(value = "approved", defaultValue = "true") boolean approved)
            throws PageTooLargeException {

        return ResponseEntity.ok(publicationService.getPublications(pageCoordsValidator.validate(page, perpage), approved));
    }

    @GetMapping(path = "/{persistentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublication(@PathVariable("persistentId") String persistentId,
                                                         @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                         @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(publicationService.getLatestPublication(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublicationVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.getPublicationVersion(persistentId, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> createPublication(@Parameter(
            description = "Created publication",
            required = true,
            schema = @Schema(implementation = PublicationCore.class)) @RequestBody PublicationCore newPublication,
                                                            @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(publicationService.createPublication(newPublication, draft));
    }

    @PutMapping(path = "/{persistentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> updatePublication(@PathVariable("persistentId") String persistentId,
                                                            @Parameter(
                                                                    description = "Update publication",
                                                                    required = true,
                                                                    schema = @Schema(implementation = PublicationCore.class)) @RequestBody PublicationCore updatedPublication,
                                                            @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(publicationService.updatePublication(persistentId, updatedPublication, draft));
    }

    @PutMapping(path = "/{persistentId}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> revertPublication(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.revertPublication(persistentId, versionId));
    }

    @DeleteMapping(path = "/{persistentId}")
    public void deletePublication(@PathVariable("persistentId") String persistentId,
                                  @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        publicationService.deletePublication(persistentId, draft);
    }

    @PostMapping(path = "/{persistentId}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> publishPublication(@PathVariable("persistentId") String persistentId) {
        PublicationDto publication = publicationService.commitDraftPublication(persistentId);
        return ResponseEntity.ok(publication);
    }

    @GetMapping(path = "/{persistentId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getPublicationHistory(@PathVariable("persistentId") String persistentId,
                                                                       @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                       @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(publicationService.getPublicationVersions(persistentId, draft, approved));
    }

    @GetMapping(path = "/{persistentId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(publicationService.getInformationContributors(persistentId));
    }

    @GetMapping(path = "/{persistentId}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("persistentId") String persistentId, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(publicationService.getInformationContributors(persistentId, versionId));
    }

    @GetMapping(path = "/{persistentId}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getMerge(@PathVariable("persistentId") String persistentId,
                                                   @RequestParam List<String> with) {
        return ResponseEntity.ok(publicationService.getMerge(persistentId, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> merge(@RequestParam List<String> with,
                                                @Parameter(
                                                        description = "Merged publication",
                                                        required = true,
                                                        schema = @Schema(implementation =PublicationCore.class))  @RequestBody PublicationCore mergePublication) {
        return ResponseEntity.ok(publicationService.merge(mergePublication, with));
    }

    @GetMapping(path = "/{persistentId}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("persistentId") String persistentId) {

        return ResponseEntity.ok(publicationService.getSources(persistentId));
    }

}
