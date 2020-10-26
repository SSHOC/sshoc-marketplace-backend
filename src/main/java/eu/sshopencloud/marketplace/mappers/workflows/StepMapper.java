package eu.sshopencloud.marketplace.mappers.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.model.workflows.Step;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StepMapper {

    StepMapper INSTANCE = Mappers.getMapper(StepMapper.class);

    @Mapping(source = "versionedItem.persistentId", target = "persistentId")
    StepDto toDto(Step step);

    List<StepDto> toDto(List<Step> steps);

}
