package eu.sshopencloud.marketplace.validators.sources;

import eu.sshopencloud.marketplace.dto.sources.SourceCore;
import eu.sshopencloud.marketplace.dto.sources.SourceId;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SourceValidator {

    private final SourceRepository sourceRepository;


    public Source validate(SourceCore sourceCore, Long sourceId) throws ValidationException {
        Source source = getOrCreateSource(sourceId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(sourceCore, "Source");

        if (StringUtils.isBlank(sourceCore.getLabel())) {
            errors.rejectValue("label", "field.required", "Label is required.");
        } else {
            source.setLabel(sourceCore.getLabel());
        }

        if (StringUtils.isBlank(sourceCore.getUrl())) {
            errors.rejectValue("url", "field.required", "Url is required.");
        } else {
            try {
                URI uri = new URL(sourceCore.getUrl()).toURI();
                source.setUrl(uri.toString());
                source.setDomain(uri.getHost());
            } catch (MalformedURLException | URISyntaxException e) {
                errors.rejectValue("url", "field.invalid", "Url is malformed.");
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return source;
        }
    }


    public Source validate(SourceId sourceId, URI accessibleAtUri, Errors errors) {
        // explicit source has priority
        if (sourceId != null) {
            if (sourceId.getId() == null) {
                errors.rejectValue("code", "field.required", "Source id is required.");
                return null;
            }
            Optional<Source> sourceHolder = sourceRepository.findById(sourceId.getId());
            if (!sourceHolder.isPresent()) {
                errors.rejectValue("id", "field.notExist", "Source does not exist.");
                return null;
            } else {
                return sourceHolder.get();
            }
        }
        if (accessibleAtUri != null) {
            return sourceRepository.findByDomain(accessibleAtUri.getHost());
        }
        return null;
    }


    private Source getOrCreateSource(Long sourceId) {
        if (sourceId != null) {
            return sourceRepository.getOne(sourceId);
        } else {
            return new Source();
        }
    }

}
