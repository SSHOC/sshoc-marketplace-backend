package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.auth.UserService;
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
                              ItemUpgradeRegistry<Publication> itemUpgradeRegistry,
                              DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                              PropertyTypeService propertyTypeService, IndexService indexService, UserService userService) {

        super(
                itemRepository, versionedItemRepository, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService
        );

        this.publicationRepository = publicationRepository;
        this.publicationFactory = publicationFactory;
    }


    public PaginatedPublications getPublications(PageCoords pageCoords) {
        return getItemsPage(pageCoords);
    }

    public PublicationDto getLatestPublication(String persistentId, boolean draft) {
        return getLatestItem(persistentId, draft);
    }

    public PublicationDto getPublicationVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public PublicationDto createPublication(PublicationCore publicationCore, boolean draft) {
        Publication publication = createItem(publicationCore, draft);
        return prepareItemDto(publication);
    }

    public PublicationDto updatePublication(String persistentId, PublicationCore publicationCore, boolean draft) {
        Publication publication = updateItem(persistentId, publicationCore, draft);
        return prepareItemDto(publication);
    }

    public PublicationDto revertPublication(String persistentId, long versionId) {
        Publication publication = revertItemVersion(persistentId, versionId);
        return prepareItemDto(publication);
    }

    public PublicationDto commitDraftPublication(String persistentId) {
        Publication publication = publishDraftItem(persistentId);
        return prepareItemDto(publication);
    }

    public void deletePublication(String persistentId, boolean draft) {
        deleteItem(persistentId, draft);
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
    protected Publication modifyItem(PublicationCore publicationCore, Publication publication) {
        return publicationFactory.modify(publicationCore, publication);
    }

    @Override
    protected Publication makeItemCopy(Publication publication) {
        return publicationFactory.makeNewVersion(publication);
    }

    @Override
    protected PaginatedPublications wrapPage(Page<Publication> publicationsPage, List<PublicationDto> publications) {
        return PaginatedPublications.builder()
                .publications(publications)
                .count(publicationsPage.getContent().size())
                .hits(publicationsPage.getTotalElements())
                .page(publicationsPage.getNumber() + 1)
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
