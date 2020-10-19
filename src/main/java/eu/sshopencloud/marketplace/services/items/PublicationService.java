package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.publications.PaginatedPublications;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.repositories.items.PublicationRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.publications.PublicationFactory;
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
    private final PublicationFactory publicationFactory;
    private final ItemCrudService itemService;
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
        return createNewPublicationVersion(publicationCore, null);
    }

    public PublicationDto updatePublication(Long id, PublicationCore publicationCore) throws ValidationException {
        if (!publicationRepository.existsById(id))
            throw new EntityNotFoundException("Unable to find " + Publication.class.getName() + " with id " + id);

        return createNewPublicationVersion(publicationCore, id);
    }

    private PublicationDto createNewPublicationVersion(PublicationCore publicationCore, Long prevPublicationId) {
        Publication prevPublication = (prevPublicationId != null) ? publicationRepository.getOne(prevPublicationId) : null;
        Publication publication = publicationFactory.create(publicationCore, prevPublication);

        publication = publicationRepository.save(publication);
        indexService.indexItem(publication);

        return itemService.completeItem(PublicationMapper.INSTANCE.toDto(publication));
    }

    public void deletePublication(Long id) {
        if (!publicationRepository.existsById(id))
            throw new EntityNotFoundException("Unable to find " + Publication.class.getName() + " with id " + id);

        Publication publication = publicationRepository.getOne(id);

        itemService.cleanupItem(publication);
        publicationRepository.delete(publication);
        indexService.removeItem(publication);
    }
}
