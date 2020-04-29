package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.validators.licenses.LicenseValidator;
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


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemValidator {

    private final LicenseValidator licenseValidator;

    private final ItemContributorValidator itemContributorValidator;

    private final PropertyValidator propertyValidator;

    private final SourceValidator sourceValidator;


    public Item validate(ItemCore itemCore, ItemCategory category, Item item, Errors errors) {
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
            item.getLicenses().addAll(licenseValidator.validate(itemCore.getLicenses(), item, errors, "licenses"));
        } else {
            item.setLicenses(licenseValidator.validate(itemCore.getLicenses(), item, errors, "licenses"));
        }

        if (item.getContributors() != null) {
            item.getContributors().addAll(itemContributorValidator.validate(itemCore.getContributors(), item, errors, "contributors"));
        } else {
            item.setContributors(itemContributorValidator.validate(itemCore.getContributors(), item, errors, "contributors"));
        }

        if (item.getProperties() != null) {
            item.getProperties().addAll(propertyValidator.validate(category, itemCore.getProperties(), item, errors, "properties"));
        } else {
            item.setProperties(propertyValidator.validate(category, itemCore.getProperties(), item, errors, "properties"));
        }

        URI accessibleAtUri = null;
        if (StringUtils.isNotBlank(itemCore.getAccessibleAt())) {
            try {
                accessibleAtUri = new URL(itemCore.getAccessibleAt()).toURI();
                item.setAccessibleAt(accessibleAtUri.toString());
            } catch (MalformedURLException | URISyntaxException e) {
                errors.rejectValue("accessibleAt", "field.invalid", "Accessible at is malformed URL.");
            }
        }

        errors.pushNestedPath("source");
        item.setSource(sourceValidator.validate(itemCore.getSource(), accessibleAtUri, errors));
        errors.popNestedPath();

        item.setSourceItemId(itemCore.getSourceItemId());

        return item;
    }

}
