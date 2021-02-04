package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemSourceService;
import eu.sshopencloud.marketplace.validators.licenses.LicenseFactory;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.validators.sources.SourceFactory;
import eu.sshopencloud.marketplace.validators.vocabularies.PropertyFactory;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemFactory {

    private final LicenseFactory licenseFactory;
    private final ItemContributorFactory itemContributorFactory;
    private final PropertyFactory propertyFactory;
    private final SourceFactory sourceFactory;
    private final UserRepository userRepository;
    private final ItemSourceService itemSourceService;


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

        item.setLicenses(licenseFactory.create(itemCore.getLicenses(), item, errors, "licenses"));
        item.setContributors(itemContributorFactory.create(itemCore.getContributors(), item, errors, "contributors"));
        item.setProperties(propertyFactory.create(category, itemCore.getProperties(), item, errors, "properties"));

        List<URI> urls = parseAccessibleAtLinks(itemCore, errors);
        List<String> accessibleAtLinks = urls.stream()
                .filter(Objects::nonNull)
                .map(URI::toString)
                .collect(Collectors.toList());

        item.setAccessibleAt(accessibleAtLinks);

        URI accessibleAtUri = (!urls.isEmpty()) ? urls.get(0) : null;

        errors.pushNestedPath("source");
        item.setSource(sourceFactory.create(itemCore.getSource(), accessibleAtUri, errors));
        errors.popNestedPath();

        item.setSourceItemId(itemCore.getSourceItemId());

        if (StringUtils.isBlank(itemCore.getSourceItemId())) {
            if (item.getSource() != null) {
                if (itemCore.getSource() != null) {
                    errors.rejectValue(
                            "sourceItemId",
                            "field.requiredInCase",
                            "Source item id is required if Source is provided."
                    );
                }
                else {
                    errors.rejectValue(
                            "sourceItemId",
                            "field.requiredInCase",
                            String.format(
                                    "Source item id is required because source was matched with '%s' by Accessible at Url.",
                                    item.getSource().getLabel()
                            )
                    );
                }
            }
        }

        if (item.getSource() == null && StringUtils.isNotBlank(itemCore.getSourceItemId())) {
            errors.rejectValue(
                    "source", "field.requiredInCase",
                    "Source is required if Source item id is provided."
            );
        }

        setInfoDates(item, true);
        updateInformationContributor(item);

        return item;
    }

    public <T extends Item> T initializeNewVersion(T newVersion) {
        setInfoDates(newVersion, false);
        updateInformationContributor(newVersion);

        return newVersion;
    }

    private ItemExternalId prepareExternalId(ItemExternalIdCore externalId, Item item, Errors errors) {
        Optional<ItemSource> itemSource = itemSourceService.loadItemSource(externalId.getServiceIdentifier());

        if (itemSource.isEmpty()) {
            errors.rejectValue(
                    "serviceIdentifier", "field.notExist",
                    String.format("Unknown service identifier: %s", externalId.getServiceIdentifier())
            );

            return null;
        }

        return new ItemExternalId(itemSource.get(), externalId.getIdentifier(), item);
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

    private void setInfoDates(Item item, boolean harvest) {
        ZonedDateTime now = ZonedDateTime.now();
        item.setLastInfoUpdate(now);
        if (harvest && item.getSource() != null) {
            item.getSource().setLastHarvestedDate(now);
        }
    }

    private void updateInformationContributor(Item item) {
        if (LoggedInUserHolder.getLoggedInUser() == null)
            return;

        User contributor = userRepository.findByUsername(LoggedInUserHolder.getLoggedInUser().getUsername());
        if (contributor != null)
            item.setInformationContributor(contributor);
    }
}
