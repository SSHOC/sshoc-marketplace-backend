package eu.sshopencloud.marketplace.controllers.inbox;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.inbox.PaginatedMessages;
import eu.sshopencloud.marketplace.services.inbox.MessagesService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final MessagesService messagesService;
    private final PageCoordsValidator pageCoordsValidator;

    @Operation(summary = "Get user massages")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedMessages> getUserMessages(
            @RequestParam(value = "page", required = false) @Schema(description = "Page numbers start at 1", minimum = "1") Integer page,
            @RequestParam(value = "perpage", required = false) @Schema(minimum = "1") Integer perpage) throws PageTooLargeException {

        return ResponseEntity.ok(
                messagesService.getUserMessages(
                        pageCoordsValidator.validate(page, perpage)));
    }


}
