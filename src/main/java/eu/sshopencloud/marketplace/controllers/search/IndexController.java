package eu.sshopencloud.marketplace.controllers.search;

import eu.sshopencloud.marketplace.services.search.IndexActorService;
import eu.sshopencloud.marketplace.services.search.IndexConceptService;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IndexController {

    private final IndexItemService indexItemService;
    private final IndexConceptService indexConceptService;
    private final IndexActorService indexActorService;


    @PutMapping(path = "/item-reindex")
    public void reindexItems() {
        indexItemService.reindexItems();
    }

    @PutMapping(path = "/item-autocomplete-rebuild")
    public void rebuildAutocompleteIndex() {
        indexItemService.rebuildAutocompleteIndex();
    }

    @PutMapping(path = "/concept-reindex")
    public void reindexConcepts() {
        indexConceptService.reindexConcepts();
    }

    @PutMapping(path = "/actor-reindex")
    public void reindexActors() {
        indexActorService.reindexActors();
    }

}
