package eu.sshopencloud.marketplace.controllers.collections;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.collections.CollectionCreationDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.dto.collections.PaginatedCollections;
import eu.sshopencloud.marketplace.services.collections.CollectionService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final PageCoordsValidator pageCoordsValidator;

    @Operation(summary = "Get all collections in pages")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedCollections> getCollections(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "perpage", required = false) Integer perpage, @RequestParam(value = "private", defaultValue = "false") boolean privateOnly) throws PageTooLargeException {

        return ResponseEntity.ok(collectionService.getCollections(pageCoordsValidator.validate(page, perpage),
                privateOnly));
    }

    @Operation(summary = "Get single collection by its id")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> getCollection(@PathVariable("id") long id) {

        return ResponseEntity.ok(collectionService.getCollection(id));
    }

    @Operation(summary = "Creating collection")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> createCollection(@Parameter(description = "Created collection",
            required = true, schema = @Schema(implementation = CollectionCreationDto.class)) @RequestBody CollectionCreationDto newCollection) {


        return ResponseEntity.ok(collectionService.createCollection(newCollection));
    }

    @Operation(summary = "Updating collection for given id")
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> updateCollection(@PathVariable("id") long collectionId,
                                                          @Parameter(description = "Updated collection", required =
                                                                  true, schema = @Schema(implementation =
                                                                  CollectionCreationDto.class)) @RequestBody CollectionCreationDto collectionCreationDto

    ) {
        return ResponseEntity.ok(collectionService.updateCollection(collectionId, collectionCreationDto));
    }

}