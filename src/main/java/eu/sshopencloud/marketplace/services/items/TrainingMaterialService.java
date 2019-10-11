package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.items.TrainingMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    public List<TrainingMaterial> getAllTrainingMaterials() {
        return trainingMaterialRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public TrainingMaterial getTrainingMaterial(Long id) {
        return trainingMaterialRepository.getOne(id);
    }

}
