package eu.sshopencloud.marketplace.controllers.sources;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.sources.PaginatedSources;
import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.sources.SourceOrder;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final PageCoordsValidator pageCoordsValidator;

    private final SourceService sourceService;

    //Here
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedSources> getSources(
            //@RequestParam(value = "order", required = false) SourceOrder order,
                                                        @RequestParam(value = "q", required = false) String q,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(sourceService.getSources(q, pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDto> getSource(@PathVariable("id") Long id) {
        return ResponseEntity.ok(sourceService.getSource(id));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDto> createSource(@RequestBody SourceCore newSource) {
        return ResponseEntity.ok(sourceService.createSource(newSource));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDto> updateSource(@PathVariable("id") long id, @RequestBody SourceCore updatedSource) {
        return ResponseEntity.ok(sourceService.updateSource(id, updatedSource));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteSource(@PathVariable("id") long id) {
        sourceService.deleteSource(id);
    }

}
