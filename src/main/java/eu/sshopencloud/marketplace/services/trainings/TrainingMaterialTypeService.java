package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterialType;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TrainingMaterialTypeService {

    private final TrainingMaterialTypeRepository trainingMaterialTypeRepository;

    public List<TrainingMaterialType> getAllTrainingMaterialTypes() {
        return trainingMaterialTypeRepository.findAll(new Sort(Sort.Direction.ASC, "ord"));
    }

}
