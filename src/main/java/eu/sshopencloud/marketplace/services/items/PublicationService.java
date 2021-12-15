package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.publications.PublicationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
                              ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<Publication> itemUpgradeRegistry,
                              DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                              PropertyTypeService propertyTypeService, IndexItemService indexItemService, UserService userService,
                              MediaStorageService mediaStorageService, SourceService sourceService, ApplicationEventPublisher eventPublisher) {

        super(
                itemRepository, versionedItemRepository, itemVisibilityService, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexItemService, userService, mediaStorageService, sourceService,
                eventPublisher
        );

        this.publicationRepository = publicationRepository;
        this.publicationFactory = publicationFactory;
    }


    public PaginatedPublications getPublications(PageCoords pageCoords, boolean approved) {
        return getItemsPage(pageCoords, approved);
    }

    public PublicationDto getLatestPublication(String persistentId, boolean draft, boolean approved) {
        return getLatestItem(persistentId, draft, approved);
    }

    public PublicationDto getPublicationVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public PublicationDto createPublication(PublicationCore publicationCore, boolean draft) {
        Publication publication = createItem(publicationCore, draft);
        return prepareItemDto(publication);
    }

    public PublicationDto updatePublication(String persistentId, PublicationCore publicationCore, boolean draft, boolean approved) {
        Publication publication = updateItem(persistentId, publicationCore, draft, approved);
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

    public void deletePublication(String persistentId, long versionId) {
        deleteItem(persistentId, versionId);
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
    protected PublicationDto convertToDto(Item item) {
        return PublicationMapper.INSTANCE.toDto(item);
    }

    @Override
    protected String getItemTypeName() {
        return Publication.class.getName();
    }

    public List<ItemExtBasicDto> getPublicationVersions(String persistentId, boolean draft, boolean approved) {
        return getItemHistory(persistentId, getLatestPublication(persistentId, draft, approved).getId());
    }

    public List<UserDto> getInformationContributors(String id) {
        return super.getInformationContributors(id);
    }

    public List<UserDto> getInformationContributors(String id, Long versionId) {
        return super.getInformationContributors(id, versionId);
    }

    public PublicationDto getMerge(String persistentId, List<String> mergeList) {
        return prepareMergeItems(persistentId, mergeList);
    }

    public PublicationDto merge(PublicationCore mergePublication, List<String> mergeList) throws ItemIsAlreadyMergedException {
        checkIfMergeIsPossible(mergeList);
        Publication publication = createItem(mergePublication, false);
        publication = mergeItem(publication.getPersistentId(), mergeList);
        return prepareItemDto(publication);
    }

    public List<SourceDto> getSources(String persistentId) {
        return getAllSources(persistentId);
    }

    public ItemsDifferencesDto getDifferences(String publicationPersistentId, Long publicationVersionId,
                                              String otherPersistentId, Long otherVersionId) {

        return super.getDifferences(publicationPersistentId, publicationVersionId, otherPersistentId, otherVersionId);
    }

}
