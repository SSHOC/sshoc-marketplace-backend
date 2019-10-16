package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.model.tools.ToolType;
import eu.sshopencloud.marketplace.services.tools.ToolTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolTypeController {

    private final ToolTypeService toolTypeService;

    @GetMapping("/tool-types")
    public ResponseEntity<List<ToolType>> getAllToolTypes() {
        List<ToolType> toolTypes = toolTypeService.getAllToolTypes();
        return ResponseEntity.ok(toolTypes);
    }

}
