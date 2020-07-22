package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.validators.licenses.LicenceFactory;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.validators.sources.SourceValidator;
import eu.sshopencloud.marketplace.validators.vocabularies.PropertyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemFactory {

    private final LicenceFactory licenceFactory;

    private final ItemContributorValidator itemContributorValidator;

    private final PropertyValidator propertyValidator;

    private final SourceValidator sourceValidator;


    public <T extends Item> T initializeItem(ItemCore itemCore, T item, ItemCategory category, Errors errors) {
        item.setCategory(category);
        if (StringUtils.isBlank(itemCore.getLabel())) {
            errors.rejectValue("label", "field.required", "Label is required.");
        } else {
            item.setLabel(itemCore.getLabel());
        }

        item.setVersion(itemCore.getVersion());

        if (StringUtils.isBlank(itemCore.getDescription())) {
            errors.rejectValue("description", "field.required", "Description is required.");
        } else {
            item.setDescription(MarkdownConverter.convertHtmlToMarkdown(itemCore.getDescription()));
        }

        if (item.getLicenses() != null) {
            item.getLicenses().addAll(licenceFactory.create(itemCore.getLicenses(), item, errors, "licenses"));
        } else {
            item.setLicenses(licenceFactory.create(itemCore.getLicenses(), item, errors, "licenses"));
        }

        if (item.getContributors() != null) {
            item.getContributors().addAll(itemContributorValidator.validate(itemCore.getContributors(), item, errors, "contributors"));
        } else {
            item.setContributors(itemContributorValidator.validate(itemCore.getContributors(), item, errors, "contributors"));
        }

        item.setProperties(propertyValidator.validate(category, itemCore.getProperties(), item, errors, "properties"));

        List<URI> urls = parseAccessibleAtLinks(itemCore, errors);
        item.clearAccessibleAtLinks();

        urls.stream()
                .filter(Objects::nonNull)
                .map(URI::toString)
                .forEachOrdered(item::addAccessibleAtLink);

        URI accessibleAtUri = (!urls.isEmpty()) ? urls.get(0) : null;

        errors.pushNestedPath("source");
        item.setSource(sourceValidator.validate(itemCore.getSource(), accessibleAtUri, errors));
        errors.popNestedPath();

        item.setSourceItemId(itemCore.getSourceItemId());

        if (StringUtils.isBlank(itemCore.getSourceItemId())) {
            if (item.getSource() != null) {
                if (itemCore.getSource() != null) {
                    errors.rejectValue("sourceItemId", "field.requiredInCase", "Source item id is required if Source is provided.");
                } else {
                    errors.rejectValue("sourceItemId", "field.requiredInCase", "Source item id is required because source was matched with '" + item.getSource().getLabel() + "' by Accessible at Url.");
                }
            }
        }

        if (item.getSource() == null && StringUtils.isNotBlank(itemCore.getSourceItemId())) {
            errors.rejectValue("source", "field.requiredInCase", "Source is required if Source item id is provided.");
        }

        if (itemCore.getStatus() == null) {
            item.setStatus(ItemStatus.INGESTED);
        } else {
            item.setStatus(itemCore.getStatus());
        }

        return item;
    }

    private List<URI> parseAccessibleAtLinks(ItemCore itemCore, Errors errors) {
        if (itemCore.getAccessibleAt() == null)
            return Collections.emptyList();

        return itemCore.getAccessibleAt()
                .stream()
                .map(url -> {
                    try {
                        if (StringUtils.isNotBlank(url))
                            return new URL(url).toURI();
                    }
                    catch (MalformedURLException | URISyntaxException e) {
                        errors.rejectValue("accessibleAt", "field.invalid", "Accessible at is malformed URL.");
                    }

                    return null;
                })
                .collect(Collectors.toList());
    }
}
