package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.model.items.EasyOfUse;
import eu.sshopencloud.marketplace.services.items.EasyOfUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EasyOfUseController {

    private final EasyOfUseService easyOfUseService;

    @GetMapping("/easy-of-uses")
    public ResponseEntity<List<EasyOfUse>> getAllEasyOfUses() {
        List<EasyOfUse> easyOfUses = easyOfUseService.getAllEasyOfUses();
        return ResponseEntity.ok(easyOfUses);
    }

}
