package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.services.trainings.TrainingMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingMaterialController {

    private final TrainingMaterialService trainingMaterialService;

    @GetMapping("/training-materials")
    public ResponseEntity<List<TrainingMaterial>> getAllTrainingMaterials() {
        List<TrainingMaterial> trainingMaterials = trainingMaterialService.getAllTrainingMaterials();
        return ResponseEntity.ok(trainingMaterials);
    }

    @GetMapping("/training-materials/{id}")
    public ResponseEntity<TrainingMaterial> getTool(@PathVariable("id") long id) {
        TrainingMaterial trainingMaterial = trainingMaterialService.getTrainingMaterial(id);
        return ResponseEntity.ok(trainingMaterial);
    }

}
