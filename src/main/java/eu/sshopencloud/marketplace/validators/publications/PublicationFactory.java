package eu.sshopencloud.marketplace.validators.publications;

import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PublicationFactory {

    private final ItemFactory itemFactory;


    public Publication create(PublicationCore publicationCore, Publication prevPublication, boolean conflict) throws ValidationException {
        Publication publication = (prevPublication != null) ? new Publication(prevPublication) : new Publication();
        return setPublicationValues(publicationCore, publication, conflict);
    }

    public Publication modify(PublicationCore publicationCore, Publication publication) throws ValidationException {
        return setPublicationValues(publicationCore, publication, false);
    }

    private Publication setPublicationValues(PublicationCore publicationCore, Publication publication, boolean conflict) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(publicationCore, "Publication");

        publication = itemFactory.initializeItem(publicationCore, publication, conflict, ItemCategory.PUBLICATION, errors);

        publication.setDateCreated(publicationCore.getDateCreated());
        publication.setDateLastUpdated(publicationCore.getDateLastUpdated());

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return publication;
    }

    public Publication makeNewVersion(Publication publication) {
        Publication newPublication = new Publication(publication);
        return itemFactory.initializeNewVersion(newPublication);
    }
}
