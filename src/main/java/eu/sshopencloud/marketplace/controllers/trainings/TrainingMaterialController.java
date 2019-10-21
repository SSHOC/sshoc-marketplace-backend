package eu.sshopencloud.marketplace.controllers.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.services.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.services.trainings.TrainingMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainingMaterialController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    @Value("${marketplace.pagination.maximal-perpage}")
    private Integer maximalPerpage;

    private final TrainingMaterialService trainingMaterialService;


    @GetMapping("/training-materials")
    public ResponseEntity<PaginatedTrainingMaterials> getTrainingMaterials(@RequestParam(value = "page", required = false) Integer page,
                                                                           @RequestParam(value = "perpage", required = false) Integer perpage) {
        perpage = perpage == null ? defualtPerpage : perpage;
        if (perpage > maximalPerpage) {
            return ResponseEntity.badRequest().build();
        }
        page = page == null ? 1 : page;

        PaginatedTrainingMaterials trainingMaterials = trainingMaterialService.getTrainingMaterials(page, perpage);
        return ResponseEntity.ok(trainingMaterials);
    }

    @GetMapping("/training-materials/{id}")
    public ResponseEntity<TrainingMaterial> getTool(@PathVariable("id") long id) {
        TrainingMaterial trainingMaterial = trainingMaterialService.getTrainingMaterial(id);
        return ResponseEntity.ok(trainingMaterial);
    }

    @PostMapping("/training-materials")
    public ResponseEntity<TrainingMaterial> createTool(@RequestBody TrainingMaterial newTrainingMaterial) {
        TrainingMaterial trainingMaterial = trainingMaterialService.addTrainingMaterial(newTrainingMaterial);
        return ResponseEntity.ok(newTrainingMaterial);
    }

    @DeleteMapping("/training-materials/{id}")
    public void deleteTool(@PathVariable("id") long id) {
        trainingMaterialService.deleteTrainingMaterial(id);
    }

}
