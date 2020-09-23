package eu.sshopencloud.marketplace.services.publications;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.repositories.publications.PublicationRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.publications.PublicationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PublicationService {

    private final PublicationRepository publicationRepository;

    private final PublicationValidator publicationValidator;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;


    public PaginatedPublications getPublications(PageCoords pageCoords) {
        Page<Publication> publicationPage = publicationRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<PublicationDto> publications = publicationPage.stream().map(PublicationMapper.INSTANCE::toDto)
                .map(publication -> {
                    itemService.completeItem(publication);
                    return publication;
                })
                .collect(Collectors.toList());

        return PaginatedPublications.builder().publications(publications)
                .count(publicationPage.getContent().size()).hits(publicationPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(publicationPage.getTotalPages())
                .build();
    }

    public PublicationDto getPublication(Long id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Publication.class.getName() + " with id " + id));
        return itemService.completeItem(PublicationMapper.INSTANCE.toDto(publication));
    }

    public PublicationDto createPublication(PublicationCore publicationCore) throws ValidationException {
        Publication publication = publicationValidator.validate(publicationCore, null);
        itemService.updateInfoDates(publication);

        itemService.addInformationContributorToItem(publication, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(publication);
        publication = publicationRepository.save(publication);
        itemService.switchVersion(publication, nextVersion);
        indexService.indexItem(publication);
        return itemService.completeItem(PublicationMapper.INSTANCE.toDto(publication));
    }

    public PublicationDto updatePublication(Long id, PublicationCore publicationCore) throws ValidationException {
        if (!publicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Publication.class.getName() + " with id " + id);
        }
        Publication publication = publicationValidator.validate(publicationCore, id);
        itemService.updateInfoDates(publication);

        itemService.addInformationContributorToItem(publication, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = publication.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(publication);
        publication = publicationRepository.save(publication);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(publication);
        return itemService.completeItem(PublicationMapper.INSTANCE.toDto(publication));
    }

    public void deletePublication(Long id) {
        if (!publicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + Publication.class.getName() + " with id " + id);
        }
        Publication publication = publicationRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(publication);
        Item prevVersion = publication.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(publication);
        publicationRepository.delete(publication);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.removeItem(publication);
    }

}
