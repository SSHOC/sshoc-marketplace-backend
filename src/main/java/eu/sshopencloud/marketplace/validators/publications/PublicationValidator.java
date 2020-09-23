package eu.sshopencloud.marketplace.validators.publications;

import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.repositories.publications.PublicationRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.items.ItemValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PublicationValidator {

    private final PublicationRepository publicationRepository;

    private final ItemValidator itemValidator;


    public Publication validate(PublicationCore publicationCore, Long publicationId) throws ValidationException {
        Publication publication = getOrCreatePublication(publicationId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(publicationCore, "Publication");

        itemValidator.validate(publicationCore, ItemCategory.PUBLICATION, publication, errors);

        publication.setDateCreated(publicationCore.getDateCreated());
        publication.setDateLastUpdated(publicationCore.getDateLastUpdated());

        if (publicationCore.getPrevVersionId() != null) {
            if (publicationId != null && publication.getId().equals(publicationCore.getPrevVersionId())) {
                errors.rejectValue("prevVersionId", "field.cycle", "Previous publication cannot be the same as the current one.");
            }
            Optional<Publication> prevVersionHolder = publicationRepository.findById(publicationCore.getPrevVersionId());
            if (!prevVersionHolder.isPresent()) {
                errors.rejectValue("prevVersionId", "field.notExist", "Previous publication does not exist.");
            } else {
                publication.setNewPrevVersion(prevVersionHolder.get());
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return publication;
        }
    }

    private Publication getOrCreatePublication(Long publicationId) {
        if (publicationId != null) {
            return publicationRepository.getOne(publicationId);
        } else {
            return new Publication();
        }
    }

}
