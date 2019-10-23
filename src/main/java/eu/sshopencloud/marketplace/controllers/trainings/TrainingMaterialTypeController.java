package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterialType;
import eu.sshopencloud.marketplace.services.trainings.TrainingMaterialTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingMaterialTypeController {

    private final TrainingMaterialTypeService trainingMaterialTypeService;

    @GetMapping(path = "/training-material-types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainingMaterialType>> getAllTrainingMaterialTypes() {
        List<TrainingMaterialType> trainingMaterialTypes = trainingMaterialTypeService.getAllTrainingMaterialTypes();
        return ResponseEntity.ok(trainingMaterialTypes);
    }

}
