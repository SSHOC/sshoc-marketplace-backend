package eu.sshopencloud.marketplace.controllers.tools;

import eu.sshopencloud.marketplace.model.tools.EaseOfUse;
import eu.sshopencloud.marketplace.services.tools.EaseOfUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EaseOfUseController {

    private final EaseOfUseService easeOfUseService;

    @GetMapping("/ease-of-use")
    public ResponseEntity<List<EaseOfUse>> getAllEaseOfUse() {
        List<EaseOfUse> easeOfUse = easeOfUseService.getAllEaseOfUse();
        return ResponseEntity.ok(easeOfUse);
    }

}
