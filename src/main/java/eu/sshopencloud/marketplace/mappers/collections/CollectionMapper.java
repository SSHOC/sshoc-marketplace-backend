package eu.sshopencloud.marketplace.mappers.collections;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.collections.CollectionDto;
import eu.sshopencloud.marketplace.dto.collections.CollectionItemDto;
import eu.sshopencloud.marketplace.dto.collections.PaginatedCollectionItems;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.collections.CollectionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mapper used of single collection. It maps collection items too.
 */
public class CollectionMapper {

    public static final CollectionMapper INSTANCE = new CollectionMapper();

    private CollectionMapper() {
    }

    public CollectionDto toDto(Collection collection, PageCoords pageCoords) {
        if (collection == null) {
            return null;
        }

        CollectionDto collectionDto = new CollectionDto();

        collectionDto.setId(collection.getId());
        collectionDto.setTitle(collection.getTitle());
        collectionDto.setDescription(collection.getDescription());
        collectionDto.setVisible(collection.isVisible());

        collectionDto.setProperties(mapCollectionProperties(collection));
        List<CollectionItemDto> collectionItemDtos = mapCollectionItems(collection, pageCoords);

        collectionDto.setCollectionItems(PaginatedCollectionItems.builder().items(collectionItemDtos).count(collectionItemDtos.size()).hits(collection.getCollectionItems().size()).page(pageCoords.getPage()).perpage(pageCoords.getPerpage()).pages(0).build());

        collectionDto.setCreatedAt(collection.getCreatedAt());
        collectionDto.setUpdatedAt(collection.getUpdatedAt());

        return collectionDto;
    }


    private List<CollectionItemDto> mapCollectionItems(Collection collection, PageCoords pageCoords) {
        List<CollectionItem> collectionItems = getPage(collection.getCollectionItems(), pageCoords.getPage(),
                pageCoords.getPerpage());
        List<CollectionItemDto> collectionItemDtos = new ArrayList<>();
        collectionItems.forEach(collectionItem -> {
            CollectionItemDto collectionItemDto = new CollectionItemDto();
            collectionItemDto.setId(collectionItem.getId());
            collectionItemDto.setPersistentId(collectionItem.getItem().getPersistentId());
            collectionItemDto.setComment(collectionItem.getComment());
            collectionItemDto.setSuggested(collectionItem.isSuggested());
            collectionItemDto.setTitle(collectionItem.getItem().getLabel());
            collectionItemDto.setDescription(collectionItem.getItem().getDescription());
            collectionItemDto.setType(collectionItem.getItem().getCategory().getLabel());
            collectionItemDtos.add(collectionItemDto);
        });

        return collectionItemDtos;
    }

    private List<PropertyDto> mapCollectionProperties(Collection collection) {

        List<PropertyDto> collectionProperties = new ArrayList<>();

        collection.getProperties().forEach(property -> {
            PropertyTypeDto propertyTypeDto = new PropertyTypeDto();
            propertyTypeDto.setCode(property.getType().getCode());
            propertyTypeDto.setLabel(property.getType().getLabel());
            propertyTypeDto.setType(property.getType().getType());
            propertyTypeDto.setGroupName(property.getType().getGroupName());
            propertyTypeDto.setHidden(property.getType().isHidden());
            propertyTypeDto.setOrd(property.getType().getOrd());

            PropertyDto propertyDto = new PropertyDto();
            propertyDto.setType(propertyTypeDto);
            propertyDto.setValue(property.getValue());

            collectionProperties.add(propertyDto);
        });
        return collectionProperties;
    }

    public List<CollectionItem> getPage(List<CollectionItem> collectionItems, int pageNumber, int pageSize) {
        if (collectionItems == null || collectionItems.isEmpty()) {
            return Collections.emptyList();
        }

        int fromIndex = (pageNumber - 1) * pageSize;
        if (fromIndex >= collectionItems.size() || fromIndex < 0) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, collectionItems.size());
        return collectionItems.subList(fromIndex, toIndex);
    }
}
