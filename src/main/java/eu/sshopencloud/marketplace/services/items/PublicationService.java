package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.publications.PublicationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@Slf4j
public class PublicationService extends ItemCrudService<Publication, PublicationDto, PaginatedPublications, PublicationCore> {

    private final PublicationRepository publicationRepository;
    private final PublicationFactory publicationFactory;


    public PublicationService(PublicationRepository publicationRepository, PublicationFactory publicationFactory,
                              ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                              ItemRelatedItemService itemRelatedItemService, PropertyTypeService propertyTypeService,
                              IndexService indexService) {

        super(itemRepository, versionedItemRepository, itemRelatedItemService, propertyTypeService, indexService);

        this.publicationRepository = publicationRepository;
        this.publicationFactory = publicationFactory;
    }


    public PaginatedPublications getPublications(PageCoords pageCoords) {
        return super.getItemsPage(pageCoords);
    }

    public PublicationDto getLatestPublication(String persistentId) {
        return super.getLatestItem(persistentId);
    }

    public PublicationDto getPublicationVersion(String persistentId, long versionId) {
        return super.getItemVersion(persistentId, versionId);
    }

    public PublicationDto createPublication(PublicationCore publicationCore) {
        return super.createItem(publicationCore);
    }

    public PublicationDto updatePublication(String persistentId, PublicationCore publicationCore) {
        return super.updateItem(persistentId, publicationCore);
    }

    public void deletePublication(String persistentId) {
        super.deleteItem(persistentId);
    }


    @Override
    protected ItemVersionRepository<Publication> getItemRepository() {
        return publicationRepository;
    }

    @Override
    protected Publication makeItem(PublicationCore publicationCore, Publication prevPublication) {
        return publicationFactory.create(publicationCore, prevPublication);
    }

    @Override
    protected PaginatedPublications wrapPage(Page<Publication> publicationsPage, List<PublicationDto> publications) {
        return PaginatedPublications.builder()
                .publications(publications)
                .count(publicationsPage.getContent().size())
                .hits(publicationsPage.getTotalElements())
                .page(publicationsPage.getNumber())
                .perpage(publicationsPage.getSize())
                .pages(publicationsPage.getTotalPages())
                .build();
    }

    @Override
    protected PublicationDto convertItemToDto(Publication publication) {
        return PublicationMapper.INSTANCE.toDto(publication);
    }

    @Override
    protected String getItemTypeName() {
        return Publication.class.getName();
    }
}
