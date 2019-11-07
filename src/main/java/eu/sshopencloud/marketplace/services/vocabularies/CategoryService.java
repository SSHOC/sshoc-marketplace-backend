package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.tools.ToolTypeId;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialTypeId;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterialType;
import eu.sshopencloud.marketplace.repositories.tools.ToolTypeRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialTypeRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    // TODO change logic when toolTypes, trainingMaterialTypes, dataTypes will be one vocabulary with hierarchy

    private final ToolTypeRepository toolTypeRepository;

    private  final TrainingMaterialTypeRepository trainingMaterialTypeRepository;

    public String getToolCategoryCode(ToolTypeId toolType) throws DataViolationException {
        if (toolType == null || toolType.getCode() == null) {
            throw new DataViolationException("toolType.code", "null");
        }
        if (!toolTypeRepository.existsById(toolType.getCode())) {
            throw new DataViolationException("toolType.code", toolType.getCode());
        }
        return toolType.getCode();
    }

    public String getTrainingMaterialCategoryCode(TrainingMaterialTypeId trainingMaterialType) throws DataViolationException {
        if (trainingMaterialType == null || trainingMaterialType.getCode() == null) {
            throw new DataViolationException("trainingMaterialType.code", "null");
        }
        if (!trainingMaterialTypeRepository.existsById(trainingMaterialType.getCode())) {
            throw new DataViolationException("trainingMaterialType.code", trainingMaterialType.getCode());
        }
        return trainingMaterialType.getCode();
    }

}
