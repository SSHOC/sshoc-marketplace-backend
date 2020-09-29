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


    public Publication create(PublicationCore publicationCore, Publication prevPublication) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(publicationCore, "Publication");

        Publication publication = itemFactory.initializeItem(
                publicationCore, new Publication(), prevPublication, ItemCategory.PUBLICATION, errors
        );

        publication.setDateCreated(publicationCore.getDateCreated());
        publication.setDateLastUpdated(publicationCore.getDateLastUpdated());

        if (errors.hasErrors())
            throw new ValidationException(errors);

        return publication;
    }
}
