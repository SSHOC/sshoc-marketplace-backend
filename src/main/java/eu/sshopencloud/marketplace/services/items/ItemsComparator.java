package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.publications.PublicationCore;
import eu.sshopencloud.marketplace.dto.tools.ToolCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.dto.workflows.StepCore;
import eu.sshopencloud.marketplace.dto.workflows.StepDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowCore;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.mappers.publications.PublicationMapper;
import eu.sshopencloud.marketplace.mappers.tools.ToolMapper;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.mappers.workflows.StepMapper;
import eu.sshopencloud.marketplace.mappers.workflows.WorkflowMapper;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.publications.Publication;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.model.workflows.Step;
import eu.sshopencloud.marketplace.model.workflows.Workflow;
import lombok.experimental.UtilityClass;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@UtilityClass
public class ItemsComparator {

    private static final String notChangedField = "unaltered";

    private static final ZonedDateTime notChangedDate = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

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

    public ItemDifferencesCore differentiateItems(ItemCore item, ItemDto other) {
        ItemDifferencesCore differences = createItemsDifferenceCore(item, other);

        differentiateCategories(item, other, differences);

        differentiateLabels(item, other, differences);
        differentiateVersions(item, other, differences);
        differentiateDescriptions(item, other, differences);
        // skip comparing sources, they don't matter when updating
        differentiateContributors(item, other, differences);
        differentiateAccessibleAt(item, other, differences);
        differentiateExternalIds(item, other, differences);
        differentiateProperties(item, other, differences);
        differentiateRelatedItems(item, other, differences);
        differentiateMedia(item, other, differences);

        differentiateDigitalObjectDates(item, other, differences);
        differentiateStepNo(item, other, differences);

        return differences;
    }

    private void differentiateCategories(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (!item.getCategory().equals(other.getCategory()))
            differences.setEqual(false);
    }

    private void differentiateCategories(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        if (item instanceof ToolCore && other.getCategory() != ItemCategory.TOOL_OR_SERVICE) {
            differences.setEqual(false);
        }
        if (item instanceof TrainingMaterialCore && other.getCategory() != ItemCategory.TRAINING_MATERIAL) {
            differences.setEqual(false);
        }
        if (item instanceof DatasetCore && other.getCategory() != ItemCategory.DATASET) {
            differences.setEqual(false);
        }
        if (item instanceof PublicationCore && other.getCategory() != ItemCategory.PUBLICATION) {
            differences.setEqual(false);
        }
        if (item instanceof WorkflowCore && other.getCategory() != ItemCategory.WORKFLOW) {
            differences.setEqual(false);
        }
        if (item instanceof StepCore && other.getCategory() != ItemCategory.STEP) {
            differences.setEqual(false);
        }
    }


    private void differentiateLabels(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item.getLabel().equals(other.getLabel()))
            other.setLabel(null);
        else
            differences.setEqual(false);
    }

    private void differentiateLabels(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        if (item.getLabel() != null) {
            if (item.getLabel().equals(other.getLabel()))
                other.setLabel(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getLabel() != null)
                differences.setEqual(false);
        }
    }

    private void differentiateVersions(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item.getVersion() != null) {
            if (item.getVersion().equals(other.getVersion()))
                other.setVersion(notChangedField);
            else
                differences.setEqual(false);
        } else {
            if (other.getVersion() != null)
                differences.setEqual(false);
        }
    }

    private void differentiateVersions(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        if (item.getVersion() != null) {
            if (item.getVersion().equals(other.getVersion()))
                other.setVersion(notChangedField);
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

    private void differentiateDescriptions(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        if (item.getDescription() != null) {
            if (item.getDescription().equals(other.getDescription()))
                other.setDescription(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getDescription() != null)
                differences.setEqual(false);
        }
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
                other.setSourceItemId(notChangedField);
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

    private void differentiateContributors(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        int itemSize = item.getContributors() != null ? item.getContributors().size() : 0;
        int otherSize = other.getContributors() != null ? other.getContributors().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getContributors().get(i).getActor().getId().equals(other.getContributors().get(i).getActor().getId())
                        && item.getContributors().get(i).getRole().getCode().equals(other.getContributors().get(i).getRole().getCode())) {
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

    private void differentiateAccessibleAt(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
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

    private void differentiateExternalIds(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        int itemSize = item.getExternalIds() != null ? item.getExternalIds().size() : 0;
        int otherSize = other.getExternalIds() != null ? other.getExternalIds().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getExternalIds().get(i).getIdentifierService().getCode().equals(other.getExternalIds().get(i).getIdentifierService().getCode())
                        && item.getExternalIds().get(i).getIdentifier().equals(other.getExternalIds().get(i).getIdentifier())) {
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

    private void differentiateProperties(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        int itemSize = item.getProperties() != null ? item.getProperties().size() : 0;
        int otherSize = other.getProperties() != null ? other.getProperties().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (arePropertiesEqual(item.getProperties().get(i), other.getProperties().get(i))) {
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

    private boolean arePropertiesEqual(PropertyCore propertyCore, PropertyDto propertyDto) {
        return (propertyCore.getType().getCode().equals(propertyDto.getType().getCode()))
                && ((propertyCore.getValue() != null && propertyCore.getValue().equals(propertyDto.getValue()))
                || (propertyCore.getValue() == null && propertyDto.getValue() == null))
                && (areConceptsEqual(propertyCore.getConcept(), propertyDto.getConcept()));
    }

    private boolean areConceptsEqual(ConceptId conceptId, ConceptBasicDto conceptDto) {
        return !(!(conceptId == null && conceptDto == null)
                && !(
                (conceptId != null && conceptId.getCode() != null && conceptId.getCode().equals(conceptDto.getCode()))
                        && (conceptId != null && conceptId.getVocabulary() != null && conceptId.getVocabulary().getCode() != null && conceptId.getVocabulary().getCode().equals(conceptDto.getVocabulary().getCode()))
                        && (conceptId != null && conceptId.getUri() != null && conceptId.getUri().equals(conceptDto.getUri()))));
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

    private void differentiateRelatedItems(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        int itemSize = item.getRelatedItems() != null ? item.getRelatedItems().size() : 0;
        int otherSize = other.getRelatedItems() != null ? other.getRelatedItems().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (item.getRelatedItems().get(i).getPersistentId().equals(other.getRelatedItems().get(i).getPersistentId())
                        && item.getRelatedItems().get(i).getRelation().getCode().equals(other.getRelatedItems().get(i).getRelation().getCode())) {
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
            if (other.getThumbnail() != null && item.getThumbnail().equals(other.getThumbnail()))
                other.setThumbnail(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getThumbnail() != null)
                differences.setEqual(false);
        }
    }

    private void differentiateMedia(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        int itemSize = item.getMedia() != null ? item.getMedia().size() : 0;
        int otherSize = other.getMedia() != null ? other.getMedia().size() : 0;
        int i;
        for (i = 0; i < itemSize; i++) {
            if (i < otherSize) {
                if (areMediaEqual(item.getMedia().get(i), other.getMedia().get(i))) {
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
            if (other.getThumbnail() != null && areMediaEqual(item.getThumbnail(), other.getThumbnail()))
                other.setThumbnail(null);
            else
                differences.setEqual(false);
        } else {
            if (other.getThumbnail() != null)
                differences.setEqual(false);
        }
    }

    private boolean areMediaEqual(ItemMediaCore itemMediaCore, ItemMediaDto itemMediaDto) {
        return itemMediaCore.getInfo().getMediaId().equals(itemMediaDto.getInfo().getMediaId())
                && ((itemMediaCore.getCaption() != null && itemMediaCore.getCaption().equals(itemMediaDto.getCaption()))
                || (itemMediaCore.getCaption() == null && itemMediaDto.getCaption() == null))
                && (areConceptsEqual(itemMediaCore.getConcept(), itemMediaDto.getConcept()));
    }


    private void differentiateDigitalObjectDates(ItemDto item, ItemDto other, ItemsDifferencesDto differences) {
        if (item instanceof DigitalObjectDto) {
            DigitalObjectDto itemDigitalObject = (DigitalObjectDto) item;
            if (other instanceof DigitalObjectDto) {
                DigitalObjectDto otherDigitalObject = (DigitalObjectDto) other;
                if (itemDigitalObject.getDateCreated() != null) {
                    if (itemDigitalObject.getDateCreated().equals(otherDigitalObject.getDateCreated()))
                        otherDigitalObject.setDateCreated(notChangedDate);
                    else
                        differences.setEqual(false);
                } else {
                    if (otherDigitalObject.getDateCreated() != null)
                        differences.setEqual(false);
                }
                if (itemDigitalObject.getDateLastUpdated() != null) {
                    if (itemDigitalObject.getDateLastUpdated().equals(otherDigitalObject.getDateLastUpdated())) {
                        otherDigitalObject.setDateLastUpdated(notChangedDate);
                    } else
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

    private void differentiateDigitalObjectDates(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        if (item instanceof DigitalObjectCore) {
            DigitalObjectCore itemDigitalObject = (DigitalObjectCore) item;
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


    private void differentiateStepNo(ItemCore item, ItemDto other, ItemDifferencesCore differences) {
        if (item instanceof StepCore) {
            StepCore itemStep = (StepCore) item;
            if (other instanceof StepDto) {
                if (itemStep.getStepNo() != null) { // set the step number (order) should be possible event with the same data of the step
                    differences.setEqual(false);
                }
            } else {
                differences.setEqual(false);
            }
        } else {
            if (other instanceof StepDto) {
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

    @SuppressWarnings(value = "unchecked, rawtypes")
    private ItemDifferencesCore<? extends ItemCore, ? extends ItemDto> createItemsDifferenceCore(ItemCore item, ItemDto other) {
        ItemDifferencesCore itemsDifferencesCore = new ItemDifferencesCore();
        itemsDifferencesCore.setItem(item);
        itemsDifferencesCore.setOther(other);
        itemsDifferencesCore.setEqual(true);
        return itemsDifferencesCore;
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

    //Eliza
    public ItemsDifferencesDto differentiateComposedOf(WorkflowDto item, WorkflowDto other, ItemsDifferencesDto differences) {

        int itemSize = item.getComposedOf() != null ? item.getComposedOf().size() : 0;
        int otherSize = other.getComposedOf() != null ? other.getComposedOf().size() : 0;
        int i;
        if (item.getComposedOf() != null) {
            for (i = 0; i < itemSize; i++) {
                if (i < otherSize) {
                    if (areStepsEqual(item.getComposedOf().get(i), other.getComposedOf().get(i))) {
                        other.getComposedOf().set(i, null);
                    } else {
                        differences.setEqual(false);
                    }
                } else {
                    differences.setEqual(false);
                }
            }
        } else {
            if (other.getComposedOf() != null) {
                differences.setEqual(false);
            }
        }
        differences.setOther(other);
        return differences;
    }

    private boolean areStepsEqual(StepDto itemStepDto, StepDto otherStepDto) {
        return itemStepDto.getId().equals(otherStepDto.getId()) &&
                itemStepDto.getPersistentId().equals(otherStepDto.getPersistentId());
    }

}
