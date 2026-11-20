package eu.sshopencloud.marketplace.controllers.collections;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.collections.*;
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
    public ResponseEntity<PaginatedCollections> getCollections(
            @RequestParam(value = "page", required = false) @Schema(description = "Page numbers start at 1", minimum = "1") Integer page,
            @RequestParam(value = "perpage", required = false) @Schema(minimum = "1") Integer perpage,
            @RequestParam(value = "readMode", defaultValue = "PUBLIC") CollectionService.CollectionsReadMode mode) throws PageTooLargeException {

        return ResponseEntity.ok(
                collectionService.getCollections(
                        pageCoordsValidator.validate(page, perpage), mode));
    }

    @Operation(summary = "Get single collection by its id")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable("id") long id,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "perpage", required = false) Integer perpage) throws PageTooLargeException {

        return ResponseEntity.ok(collectionService.getCollection(id, pageCoordsValidator.validate(page, perpage)));
    }

    @Operation(summary = "Creating collection")
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> createCollection(@Parameter(description = "Created collection",
            required = true, schema = @Schema(implementation = CollectionCreationDto.class)) @RequestBody CollectionCreationDto newCollection) {

        return ResponseEntity.ok(collectionService.createCollection(newCollection));
    }

    @Operation(
        summary = "Updating collection for given id",
        description = """
            Note that leaving out a field in the request body will result in that field being
            cleared in the updated collection. For example, if you leave out the list of items
            in the collection then it is assumed that you wish to remove all existing items from
            the collection. If you wish to keep a field unchanged, you must include it in the
            request body with its current value."""
    )
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> updateCollection(@PathVariable("id") long collectionId,
                                                          @Parameter(description = "Updated collection", required =
                                                                  true, schema = @Schema(implementation =
                                                                  CollectionCreationDto.class)) @RequestBody CollectionCreationDto collectionCreationDto

    ) {
        return ResponseEntity.ok(collectionService.updateCollection(collectionId, collectionCreationDto));
    }

    @Operation(summary = "Add suggestion to given collection")
    @PostMapping(path = "/{collectionId}/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addCollectionSuggestions(@PathVariable("collectionId") long collectionId,
                                                         @Parameter(description = "Suggestion for collection", required =
                                                                                              true, schema = @Schema(implementation =
                                                                                              CollectionSuggestionCreationDto.class)) @RequestBody CollectionSuggestionCreationDto suggestionCreationDto) {

        collectionService.addSuggestionToCollection(collectionId, suggestionCreationDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change the suggestion status for given collection")
    @PostMapping(path = "/{collectionId}/suggestions/{suggestionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changeCollectionSuggestionStatus(
            @PathVariable("collectionId") long collectionId,
            @PathVariable("suggestionId") long suggestionId,
            @Parameter(
                    description = "Suggestion for collection",
                    required = true,
                    schema = @Schema(implementation = CollectionSuggestionStatusActionDto.class))
            @RequestBody CollectionSuggestionStatusActionDto collectionSuggestionStatusActionDto) {

        collectionService.changeCollectionSuggestionStatus(collectionId, suggestionId, collectionSuggestionStatusActionDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove collection")
    @DeleteMapping(path = "/{collectionId}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable("collectionId") long collectionId) {

        collectionService.deleteCollection(collectionId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Retrieve the current status of recommended flag for this collection"
    )
    @GetMapping(path = "/{collectionId}/recommended", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionRecommendedDto> isCollectionRecommended(
            @PathVariable("collectionId") long collectionId) {

        return ResponseEntity.ok(collectionService.isCollectionRecommended(collectionId));
    }

    @Operation(
        summary = "Allows moderators to change the recommended status of a collection",
        description = """
            Allows moderators to change the recommended status of a collection.
            Note that this is the only way of changing the status as the recommended
            flag cannot be set during either creation or updates to a collection."""
    )
    @PutMapping(path = "/{collectionId}/recommended", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionRecommendedDto> updateCollectionIsRecommended(
            @PathVariable("collectionId") long collectionId,
            @RequestBody CollectionRecommendedDto recommendedDto) {

        return ResponseEntity.ok(collectionService.updateRecommendedStatus(collectionId, recommendedDto));
    }
}
