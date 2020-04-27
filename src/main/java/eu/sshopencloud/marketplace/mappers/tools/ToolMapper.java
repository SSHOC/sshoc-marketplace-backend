package eu.sshopencloud.marketplace.mappers.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.model.tools.Tool;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ToolMapper {

    ToolMapper INSTANCE = Mappers.getMapper(ToolMapper.class);

    ToolDto toDto(Tool tool);

    List<ToolDto> toDto(List<Tool> tools);

}
