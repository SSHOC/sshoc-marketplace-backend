package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemContributorDto;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ItemContributorMapper {

    ItemContributorMapper INSTANCE = Mappers.getMapper(ItemContributorMapper.class);

    ItemContributorDto toDto(ItemContributor itemContributor);

    List<ItemContributorDto> toDto(List<ItemContributor> itemContributors);

}
