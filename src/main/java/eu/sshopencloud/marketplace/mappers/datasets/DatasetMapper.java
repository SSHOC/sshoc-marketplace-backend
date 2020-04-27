package eu.sshopencloud.marketplace.mappers.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DatasetMapper {

    DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

    DatasetDto toDto(Dataset dataset);

    List<DatasetDto> toDto(List<Dataset> datasets);

}
