package eu.sshopencloud.marketplace.controllers.publications;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.services.items.PublicationService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PageCoordsValidator pageCoordsValidator;

    private final PublicationService publicationService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedPublications> getPublications(@RequestParam(value = "page", required = false) Integer page,
                                                                 @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(publicationService.getPublications(pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublication(@PathVariable("id") String id) {
        return ResponseEntity.ok(publicationService.getLatestPublication(id));
    }

    @GetMapping(path = "/{id}/{versionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> getPublication(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.getPublicationVersion(id, versionId));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> createPublication(@RequestBody PublicationCore newPublication) {
        return ResponseEntity.ok(publicationService.createPublication(newPublication));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> updatePublication(@PathVariable("id") String id, @RequestBody PublicationCore updatedPublication) {
        return ResponseEntity.ok(publicationService.updatePublication(id, updatedPublication));
    }

    @PutMapping(path = "/{id}/{versionId}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PublicationDto> revertPublication(@PathVariable("id") String id, @PathVariable("versionId") long versionId) {
        return ResponseEntity.ok(publicationService.revertPublication(id, versionId));
    }

    @DeleteMapping(path = "/{id}")
    public void deletePublication(@PathVariable("id") String id) {
        publicationService.deletePublication(id);
    }
}
