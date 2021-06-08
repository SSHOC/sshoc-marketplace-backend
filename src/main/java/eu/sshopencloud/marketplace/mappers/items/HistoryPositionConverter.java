package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.HistoryPositionDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HistoryPositionConverter {

    public HistoryPositionDto convertItem(Item item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(UserMapper.INSTANCE.toDto(item.getInformationContributor()));

        return historyPosition;
    }

}
