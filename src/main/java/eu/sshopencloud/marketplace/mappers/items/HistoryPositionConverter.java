package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.items.HistoryPositionDto;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.publications.PublicationDto;
import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapperImpl;
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
        historyPosition.setInformationContributor(UserMapperImpl.INSTANCE.toDto(item.getInformationContributor()));

        return historyPosition;
    }

    public HistoryPositionDto convertItem(DatasetDto item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(item.getInformationContributor());

        return historyPosition;
    }

    public HistoryPositionDto convertItem(ToolDto item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(item.getInformationContributor());

        return historyPosition;
    }

    public HistoryPositionDto convertItem(TrainingMaterialDto item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(item.getInformationContributor());

        return historyPosition;
    }

    public HistoryPositionDto convertItem(WorkflowDto item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(item.getInformationContributor());

        return historyPosition;
    }

    public HistoryPositionDto convertItem(StepDto item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(item.getInformationContributor());

        return historyPosition;
    }

    public HistoryPositionDto convertItem(PublicationDto item) {
        HistoryPositionDto historyPosition = new HistoryPositionDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(item.getInformationContributor());

        return historyPosition;
    }
}
