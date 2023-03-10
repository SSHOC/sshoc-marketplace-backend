package eu.sshopencloud.marketplace.mappers.datasets;

import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DatasetMapper {

    DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

    @Mapping(source = "versionedItem.persistentId", target = "persistentId")
    DatasetDto toDto(Dataset dataset);

    @Mapping(source = "versionedItem.persistentId", target = "persistentId")
    DatasetDto toDto(Item dataset);

    List<DatasetDto> toDto(List<Dataset> datasets);

}
