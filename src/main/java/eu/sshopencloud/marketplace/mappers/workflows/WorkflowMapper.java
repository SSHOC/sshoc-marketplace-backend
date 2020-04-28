package eu.sshopencloud.marketplace.mappers.workflows;

import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface WorkflowMapper {

    WorkflowMapper INSTANCE = Mappers.getMapper(WorkflowMapper.class);

    @Mapping(source = "steps", target = "composedOf", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    WorkflowDto toDto(Workflow workflow);

    List<WorkflowDto> toDto(List<Workflow> workflows);

    @Mapping(source = "substeps", target = "composedOf", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    StepDto toStepDto(Step step);

    List<StepDto> toStepDto(List<Step> steps);

}
