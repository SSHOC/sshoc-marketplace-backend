package eu.sshopencloud.marketplace.controllers.search;

import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IndexController {

    private final IndexService indexService;


    @PutMapping(path = "/item-reindex")
    public void reindexItems() {
        indexService.reindexItems();
    }

}
