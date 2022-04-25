package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.services.items.*;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemController {

    private final PageCoordsValidator pageCoordsValidator;

    private final ItemsService itemService;

    @Operation(summary = "Get all draft-items available in pages")
    @GetMapping(path = "/draft-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemsBasic> getMyDraftItems(@RequestParam(value = "order", required = false) ItemOrder order,
                                                               @RequestParam(value = "page", required = false) Integer page,
                                                               @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(itemService.getMyDraftItems(order, pageCoordsValidator.validate(page, perpage)));
    }


    @Operation(summary = "Get all deleted-items available in pages")
    @GetMapping(path = "/deleted-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedItemsBasic> getDeletedItems(@RequestParam(value = "order", required = false) ItemOrder order,
                                                               @RequestParam(value = "page", required = false) Integer page,
                                                               @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(itemService.getDeletedItems(order, pageCoordsValidator.validate(page, perpage)));
    }

}
