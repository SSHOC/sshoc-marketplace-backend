package eu.sshopencloud.marketplace.validators.items;

import eu.sshopencloud.marketplace.dto.items.ItemExternalIdCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import eu.sshopencloud.marketplace.services.items.ItemSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;


@Component
@RequiredArgsConstructor
public class ItemExternalIdFactory {

    private final ItemSourceService itemSourceService;


    public List<ItemExternalId> create(List<ItemExternalIdCore> externalIds, Item item, Errors errors) {
        List<ItemExternalId> itemExternalIds = new ArrayList<>();
        Set<ItemExternalId> processedExternalIds = new HashSet<>();

        if (externalIds == null)
            return itemExternalIds;

        for (int i = 0; i < externalIds.size(); ++i) {
            String nestedPath = String.format("externalIds[%d]", i);
            errors.pushNestedPath(nestedPath);

            ItemExternalId externalId = create(externalIds.get(i), item, errors);
            if (externalId != null) {

                if (!processedExternalIds.contains(externalId)) {
                    itemExternalIds.add(externalId);
                    processedExternalIds.add(externalId);
                }
                else {
                    errors.popNestedPath();
                    errors.rejectValue(
                            nestedPath, "field.duplicateEntry",
                            String.format(
                                    "Duplicate item's external id: %s (from: %s)",
                                    externalId.getIdentifier(), externalId.getIdentifierService().getLabel()
                            )
                    );

                    continue;
                }
            }

            errors.popNestedPath();
        }

        return itemExternalIds;
    }

    public ItemExternalId create(ItemExternalIdCore externalId, Item item, Errors errors) {
        Optional<ItemSource> itemSource = itemSourceService.loadItemSource(externalId.getIdentifierService().getCode());

        if (itemSource.isEmpty()) {
            errors.rejectValue(
                    "identifierService", "field.notExist",
                    String.format("Unknown identifier service: %s", externalId.getIdentifierService())
            );

            return null;
        }

        return new ItemExternalId(itemSource.get(), externalId.getIdentifier(), item);
    }
}
