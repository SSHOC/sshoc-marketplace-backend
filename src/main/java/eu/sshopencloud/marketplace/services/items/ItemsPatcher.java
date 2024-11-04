package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.actors.ActorId;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleId;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;

class ItemsPatcher {
    static void patchItemCore(ItemDto currentItemDto, ItemCore itemCore) {
        if (StringUtils.isBlank(itemCore.getLabel())) {
            itemCore.setLabel(currentItemDto.getLabel());
        }

        if (StringUtils.isBlank(itemCore.getDescription())) {
            itemCore.setDescription(currentItemDto.getDescription());
        }

        if (StringUtils.isBlank(itemCore.getVersion())) {
            itemCore.setVersion(currentItemDto.getVersion());
        }

        if (Objects.isNull(itemCore.getContributors()) || itemCore.getContributors().isEmpty()) {
            itemCore.setContributors(currentItemDto.getContributors().stream()
                    .map(cid -> new ItemContributorId(new ActorId(cid.getActor().getId()), new ActorRoleId(cid.getRole().getCode())))
                    .collect(Collectors.toList())
            );
        }

        if (Objects.isNull(itemCore.getAccessibleAt()) || itemCore.getAccessibleAt().isEmpty()) {
            itemCore.setAccessibleAt(currentItemDto.getAccessibleAt());
        }

        if (Objects.isNull(itemCore.getExternalIds()) || itemCore.getExternalIds().isEmpty()) {
            itemCore.setExternalIds(currentItemDto.getExternalIds().stream()
                    .map(eid -> new ItemExternalIdCore(new ItemExternalIdId(eid.getIdentifierService().getCode()), eid.getIdentifier()))
                    .collect(Collectors.toList()));
        }

        if (Objects.isNull(itemCore.getProperties()) || itemCore.getProperties().isEmpty()) {
            itemCore.setProperties(currentItemDto.getProperties().stream()
                    .map(cip -> {
                        PropertyCore pc = new PropertyCore(new PropertyTypeId(cip.getType().getCode()), cip.getValue());
                        if (Objects.nonNull(cip.getConcept())) {
                            pc.setConcept(new ConceptId(cip.getConcept().getCode(),
                                    new VocabularyId(cip.getConcept().getVocabulary().getCode()), cip.getConcept().getUri()));
                        }
                        return pc;
                    })
                    .collect(Collectors.toList()));
        }

        if (Objects.isNull(itemCore.getRelatedItems()) || itemCore.getRelatedItems().isEmpty()) {
            itemCore.setRelatedItems(currentItemDto.getRelatedItems().stream()
                    .map(rid -> new RelatedItemCore(rid.getPersistentId(),new ItemRelationId(rid.getRelation().getCode())))
                    .collect(Collectors.toList()));
        }

        if (Objects.isNull(itemCore.getMedia()) || itemCore.getMedia().isEmpty()) {
            itemCore.setMedia(currentItemDto.getMedia().stream()
                    .map(cim -> new ItemMediaCore(new MediaDetailsId(cim.getInfo().getMediaId()), cim.getCaption(),
                            new ConceptId(cim.getConcept().getCode(),
                                    new VocabularyId(cim.getConcept().getVocabulary().getCode()),
                                    cim.getConcept().getUri())))
                    .collect(Collectors.toList()));
        }

        if (itemCore instanceof DigitalObjectCore && currentItemDto instanceof DigitalObjectDto) {
            DigitalObjectCore itemDigitalObject = (DigitalObjectCore) itemCore;
            DigitalObjectDto digitalObjectDto = (DigitalObjectDto) currentItemDto;
            if (Objects.isNull(itemDigitalObject.getDateCreated())) {
                itemDigitalObject.setDateCreated(digitalObjectDto.getDateCreated());
            }

            if (Objects.isNull(itemDigitalObject.getDateLastUpdated())) {
                itemDigitalObject.setDateLastUpdated(digitalObjectDto.getDateLastUpdated());
            }
        }
    }
}
