package eu.sshopencloud.marketplace.mappers.trainings;

import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TrainingMaterialMapper {

    TrainingMaterialMapper INSTANCE = Mappers.getMapper(TrainingMaterialMapper.class);

    TrainingMaterialDto toDto(TrainingMaterial trainingMaterial);

    List<TrainingMaterialDto> toDto(List<TrainingMaterial> trainingMaterials);

}
