package eu.sshopencloud.marketplace.controllers.publications;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.services.items.PublicationService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
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

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublication(@PathVariable("id") String id,
                                                         @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                         @RequestParam(value = "approved", defaultValue = "true") boolean approved) {

        return ResponseEntity.ok(publicationService.getLatestPublication(id, draft, approved));
    }

    @GetMapping(path = "/{id}/versions/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublicationVersion(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.getPublicationVersion(id, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> createPublication(@RequestBody PublicationCore newPublication,
                                                            @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(publicationService.createPublication(newPublication, draft));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> updatePublication(@PathVariable("id") String id, @RequestBody PublicationCore updatedPublication,
                                                            @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        return ResponseEntity.ok(publicationService.updatePublication(id, updatedPublication, draft));
    }

    @PutMapping(path = "/{id}/versions/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> revertPublication(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.revertPublication(id, versionId));
    }

    @DeleteMapping(path = "/{id}")
    public void deletePublication(@PathVariable("id") String id,
                                  @RequestParam(value = "draft", required = false, defaultValue = "false") boolean draft) {

        publicationService.deletePublication(id, draft);
    }

    @PostMapping(path = "/{id}/commit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> publishPublication(@PathVariable("id") String id) {
        PublicationDto publication = publicationService.commitDraftPublication(id);
        return ResponseEntity.ok(publication);
    }

    @GetMapping(path = "/{id}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemExtBasicDto>> getPublicationHistory(@PathVariable("id") String id,
                                                                       @RequestParam(value = "draft", defaultValue = "false") boolean draft,
                                                                       @RequestParam(value = "approved", defaultValue = "true") boolean approved) {
        return ResponseEntity.ok(publicationService.getPublicationVersions(id, draft, approved));
    }

    @GetMapping(path = "/{id}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributors(@PathVariable("id") String id) {

        return ResponseEntity.ok(publicationService.getInformationContributors(id));
    }

    @GetMapping(path = "/{id}/versions/{versionId}/information-contributors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getInformationContributorsForVersion(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {

        return ResponseEntity.ok(publicationService.getInformationContributors(id, versionId));
    }

    @GetMapping(path = "/{id}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getMerge(@PathVariable("id") String id,
                                                   @RequestParam List<String> with) {
        return ResponseEntity.ok(publicationService.getMerge(id, with));
    }

    @PostMapping(path = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> merge(@RequestParam List<String> with,
                                                @RequestBody PublicationCore mergePublication) {
        return ResponseEntity.ok(publicationService.merge(mergePublication, with));
    }

    @GetMapping(path = "/{id}/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SourceDto>> getSources(@PathVariable("id") String id) {

        return ResponseEntity.ok(publicationService.getSources(id));
    }
}
