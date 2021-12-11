package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ItemsComparator {

    @SuppressWarnings(value = "rawtypes")
    public ItemsDifferencesDto differentiateItems(ItemDto item, ItemDto other) {
        ItemsDifferencesDto differences = createItemsDifferenceDto(item, other);

        differentiateCategories(item, other, differences);

        differentiateLabels(item, other, differences);
        differentiateVersions(item, other, differences);
        differentiateDescriptions(item, other, differences);
        differentiateSources(item, other, differences);
        differentiateContributors(item, other, differences);
        differentiateAccessibleAt(item, other, differences);
        differentiateExternalIds(item, other, differences);
        differentiateProperties(item, other, differences);
        differentiateRelatedItems(item, other, differences);
        differentiateMedia(item, other, differences);

        differentiateDigitalObjectDates(item, other, differences);

        return differences;
    }

    private void differentiateCategories(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (!item.getCategory().equals(other.getCategory()))
            differences.setEqual(false);
    }

    private void differentiateLabels(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item.getLabel().equals(other.getLabel()))
            other.setLabel(null);
        else
            differences.setEqual(false);
    }

    private void differentiateVersions(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item.getVersion() != null) {
            if (item.getVersion().equals(other.getVersion()))
                other.setVersion(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getVersion() != null)
                differences.setEqual(false);
        }
    }

    private void differentiateDescriptions(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item.getDescription().equals(other.getDescription()))
            other.setDescription(null);
        else
            differences.setEqual(false);
    }

    private void differentiateSources(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item.getSource() != null) {
            if (item.getSource().equals(other.getSource()))
                other.setSource(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getSource() != null)
                differences.setEqual(false);
        }

        if (item.getSourceItemId() != null) {
            if (item.getSourceItemId().equals(other.getSourceItemId()))
                other.setSourceItemId(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getSourceItemId() != null)
                differences.setEqual(false);
        }
    }

    private void differentiateContributors(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        int itemSize = item.getContributors() != null ? item.getContributors().size() : 0;
        int otherSize = other.getContributors() != null ? other.getContributors().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getContributors().get(i).equals(other.getContributors().get(i))) {
                    other.getContributors().set(i, null);
                } else {
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        }
        if (i < otherSize)
            differences.setEqual(false);
    }

    private void differentiateAccessibleAt(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        int itemSize = item.getAccessibleAt() != null ? item.getAccessibleAt().size() : 0;
        int otherSize = other.getAccessibleAt() != null ? other.getAccessibleAt().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getAccessibleAt().get(i).equals(other.getAccessibleAt().get(i))) {
                    other.getAccessibleAt().set(i, null);
                } else {
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        }
        if (i < otherSize)
            differences.setEqual(false);
    }

    private void differentiateExternalIds(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        int itemSize = item.getExternalIds() != null ? item.getExternalIds().size() : 0;
        int otherSize = other.getExternalIds() != null ? other.getExternalIds().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getExternalIds().get(i).equals(other.getExternalIds().get(i))) {
                    other.getExternalIds().set(i, null);
                } else {
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        }
        if (i < otherSize)
            differences.setEqual(false);
    }

    private void differentiateProperties(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        int itemSize = item.getProperties() != null ? item.getProperties().size() : 0;
        int otherSize = other.getProperties() != null ? other.getProperties().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getProperties().get(i).equals(other.getProperties().get(i))) {
                    other.getProperties().set(i, null);
                } else {
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        }
        if (i < otherSize)
            differences.setEqual(false);
    }

    private void differentiateRelatedItems(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        int itemSize = item.getRelatedItems() != null ? item.getRelatedItems().size() : 0;
        int otherSize = other.getRelatedItems() != null ? other.getRelatedItems().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getRelatedItems().get(i).equals(other.getRelatedItems().get(i))) {
                    other.getRelatedItems().set(i, null);
                } else {
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        }
        if (i < otherSize)
            differences.setEqual(false);
    }

    private void differentiateMedia(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        int itemSize = item.getMedia() != null ? item.getMedia().size() : 0;
        int otherSize = other.getMedia() != null ? other.getMedia().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getMedia().get(i).equals(other.getMedia().get(i))) {
                    other.getMedia().set(i, null);
                } else {
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        }
        if (i < otherSize)
            differences.setEqual(false);

        if (item.getThumbnail() != null) {
            if (item.getThumbnail().equals(other.getThumbnail()))
                other.setThumbnail(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getThumbnail() != null)
                differences.setEqual(false);
        }
    }

    private void differentiateDigitalObjectDates(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item instanceof DigitalObjectDto) {
            DigitalObjectDto itemDigitalObject = (DigitalObjectDto) item;
            if (other instanceof DigitalObjectDto) {
                DigitalObjectDto otherDigitalObject = (DigitalObjectDto) other;
                if (itemDigitalObject.getDateCreated() != null) {
                    if (itemDigitalObject.getDateCreated().equals(otherDigitalObject.getDateCreated()))
                        otherDigitalObject.setDateCreated(null);
                    else
                        differences.setEqual(false);
                } else {
                    if (otherDigitalObject.getDateCreated() != null)
                        differences.setEqual(false);
                }
                if (itemDigitalObject.getDateLastUpdated() != null) {
                    if (itemDigitalObject.getDateLastUpdated().equals(otherDigitalObject.getDateLastUpdated()))
                        otherDigitalObject.setDateLastUpdated(null);
                    else
                        differences.setEqual(false);
                } else {
                    if (otherDigitalObject.getDateLastUpdated() != null)
                        differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        } else {
            if (other instanceof DigitalObjectDto) {
                differences.setEqual(false);
            }
        }
    }


    @SuppressWarnings(value = "unchecked, rawtypes")
    private ItemsDifferencesDto<? extends ItemDto, ? extends ItemDto> createItemsDifferenceDto(ItemDto item, ItemDto other) {
        ItemsDifferencesDto itemsDifferencesDto = new ItemsDifferencesDto();
        itemsDifferencesDto.setItem(item);
        itemsDifferencesDto.setOther(other);
        itemsDifferencesDto.setEqual(true);
        return itemsDifferencesDto;
    }

    public ItemDto toDto(Item item) {
        switch (item.getCategory()) {
            case TOOL_OR_SERVICE:
                return ToolMapper.INSTANCE.toDto((Tool) item);
            case TRAINING_MATERIAL:
                return TrainingMaterialMapper.INSTANCE.toDto((TrainingMaterial) item);
            case PUBLICATION:
                return PublicationMapper.INSTANCE.toDto((Publication) item);
            case DATASET:
                return DatasetMapper.INSTANCE.toDto((Dataset) item);
            case WORKFLOW:
                return WorkflowMapper.INSTANCE.toDto((Workflow) item);
            case STEP:
                return StepMapper.INSTANCE.toDto((Step) item);
            default:
                return null;
        }
    }

}
