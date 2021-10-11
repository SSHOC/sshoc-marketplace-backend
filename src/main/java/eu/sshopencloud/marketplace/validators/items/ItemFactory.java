package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemMedia;
import eu.sshopencloud.marketplace.model.items.ItemMediaType;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.text.LineBreakConverter;
import eu.sshopencloud.marketplace.services.text.MarkdownConverter;
import eu.sshopencloud.marketplace.validators.CollectionUtils;
import eu.sshopencloud.marketplace.validators.sources.SourceFactory;
import eu.sshopencloud.marketplace.validators.vocabularies.PropertyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class ItemFactory {

    private final ItemContributorFactory itemContributorFactory;
    private final ItemExternalIdFactory itemExternalIdFactory;
    private final PropertyFactory propertyFactory;
    private final SourceFactory sourceFactory;
    private final ItemMediaFactory itemMediaFactory;

    private final UserRepository userRepository;

    public <T extends Item> T initializeItem(ItemCore itemCore, T item, ItemCategory category, Errors errors) {
        item.setCategory(category);
        if (StringUtils.isBlank(itemCore.getLabel())) {
            errors.rejectValue("label", "field.required", "Label is required.");
        } else {
            item.setLabel(LineBreakConverter.removeLineBreaks(itemCore.getLabel()));
        }

        item.setVersion(itemCore.getVersion());

        if (StringUtils.isBlank(itemCore.getDescription())) {
            errors.rejectValue("description", "field.required", "Description is required.");
        } else {
            item.setDescription(MarkdownConverter.convertHtmlToMarkdown(itemCore.getDescription()));
        }

        item.setContributors(itemContributorFactory.create(itemCore.getContributors(), item, errors, "contributors"));
        item.setProperties(propertyFactory.create(itemCore.getProperties(), item, errors, "properties"));

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
                } else {
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
        item.addExternalIds(itemExternalIdFactory.create(itemCore.getExternalIds(), item, errors));

        //if (!itemCore.getMedia().isEmpty())
        item.addMedia(itemMediaFactory.create(itemCore.getMedia(), item, errors));


        if (itemCore.getThumbnail() != null && itemCore.getThumbnail().getInfo() != null) {
            UUID thumbnailId = itemCore.getThumbnail().getInfo().getMediaId();
            Optional<ItemMedia> itemThumbnail = item.getMedia().stream()
                    .filter(media -> media.getMediaId().equals(thumbnailId))
                    .findFirst();

            if (itemThumbnail.isPresent()) {
                itemThumbnail.get().setItemMediaThumbnail(ItemMediaType.THUMBNAIL);
            } else {
                item.addMedia(itemMediaFactory.create(itemCore.getThumbnail().getInfo().getMediaId(), item, errors, ItemMediaType.THUMBNAIL_ONLY, itemCore.getThumbnail().getCaption(), itemCore.getThumbnail().getConcept()));
            }
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

    private List<URI> parseAccessibleAtLinks(ItemCore itemCore, Errors errors) {
        if (itemCore.getAccessibleAt() == null)
            return Collections.emptyList();

        return itemCore.getAccessibleAt()
                .stream()
                .map(url -> {
                    try {
                        if (StringUtils.isNotBlank(url))
                            return new URL(url).toURI();
                    } catch (MalformedURLException | URISyntaxException e) {
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
