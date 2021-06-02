package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.services.items.*;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

    private final PageCoordsValidator pageCoordsValidator;

    private final ItemsService itemService;

    @GetMapping(path = "/draft-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemsBasic> getMyDraftItems(@RequestParam(value = "order", required = false) ItemOrder order,
                                                               @RequestParam(value = "page", required = false) Integer page,
                                                               @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(itemService.getMyDraftItems(order, pageCoordsValidator.validate(page, perpage)));
    }


    @GetMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemBasicDto>> getItems(@RequestParam(value = "sourceId", required = true) Long sourceId,
                                                       @RequestParam(value = "sourceItemId", required = true) String sourceItemId) {

        return ResponseEntity.ok(itemService.getItems(sourceId, sourceItemId));
    }


}
