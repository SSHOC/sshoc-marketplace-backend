package eu.sshopencloud.marketplace.services.items;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.dto.items.DigitalObjectCore;
import eu.sshopencloud.marketplace.dto.items.DigitalObjectDto;
import eu.sshopencloud.marketplace.dto.items.ItemContributorDto;
import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdDto;
import eu.sshopencloud.marketplace.dto.items.ItemMediaDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.dto.items.ItemSourceDto;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptBasicDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;

class ItemsPatcherTest {

    /**
     * A test to ensure that dates are correctly preserved during patching.
     * This test was added specifically to help test development of the new
     * patching algorithm, as the Jackson based approach initially failed
     * to handle the ZonedDateTime instances used in DigitalObject[DTO|Core]
     */
    @Test
    public void testDateSerialization() {
        DigitalObjectDto itemDto = prepareItemDto();

        DigitalObjectCore itemCore = new DigitalObjectCore();

        ItemsPatcher.patchItemCore(itemDto, null, itemCore);

        Assertions.assertEquals(itemDto.getDateCreated(), itemCore.getDateCreated());
    }
    
    /**
     * This test ensures that when applying a patch which does not include a thumbnail,
     * the existing thumbnail in the ItemDto is retained in the resulting ItemCore.
     */
    @Test
    public void testThumbnailRetention() {
        
        // create a sample ItemDto, with a thumbnail, which we want to patch
        ItemDto itemDto = prepareItemDto();
        
        // sanity check to ensure the thumbnail is not null before patching
        Assertions.assertNotNull(itemDto.getThumbnail());

        // create a patch to update the label of the item, but not the thumbnail
        ItemCore patch = new ItemCore();
        patch.setLabel("updatedLabel");

        // apply the patch to the item.
        ItemsPatcher.patchItemCore(itemDto, null, patch);

        // verify that the label matches the patch value
        Assertions.assertEquals("updatedLabel", patch.getLabel());
        
        // verify that the thumbnail is retained from the original itemDto
        // by checking the mediaId of the thumbnail in both the original and patched objects.
        // We can't do an equality check as the object in ItemDto and ItemCore are different types.
        Assertions.assertEquals(
            itemDto.getThumbnail().getInfo().getMediaId(),
            patch.getThumbnail().getInfo().getMediaId()
        );
    }

    /**
     * This test ensures that when applying a patch, which does not contain
     * any related items, that the related items are correctly copied from
     * the ItemDto instance.
     */
    @Test
    public void testRelatedItemsRetention() {
        
        // create a sample ItemDto, with a related item, which we want to patch
        ItemDto itemDto = prepareItemDto();
        
        // sanity check to ensure there is a single related item prior to patching
        Assertions.assertEquals(1, itemDto.getRelatedItems().size());

        // create a patch to update the label of the item, but not the related items
        ItemCore patch = new ItemCore();
        patch.setLabel("updatedLabel");

        // apply the patch to the item.
        ItemsPatcher.patchItemCore(itemDto, null, patch);

        // verify that the label matches the patch value
        Assertions.assertEquals("updatedLabel", patch.getLabel());
        
        // verify that the related items are retained from the original itemDto
        // by checking the size of the related items list in both the original and patched objects.
        // We can't do an equality check as the object in ItemDto and ItemCore are different types.
        Assertions.assertEquals(1,patch.getRelatedItems().size());

        // verify that the persistentId of the related item is retained from the original itemDto
        Assertions.assertEquals(
            itemDto.getRelatedItems().get(0).getPersistentId(),
            patch.getRelatedItems().get(0).getPersistentId()
        );
    }

    /**
     * This test is setup to patch the description field, where the current item
     * has a valid value, but the first ingest version has a null description.
     * What actually happens is that the current value is retained due to the weird
     * logic in ItemsPatcher.determinePatchValue(). This seems counter-intuitive as the
     * patch value is ignored, hence the behaviour is documented through this test.
     * Note that the same logic applies when updating the label or version
     * field as well given that both pass through ItemsPatcher.determinePatchValue()
     */
    @Test
    public void testPatchNullDescription() {
        // create a sample ItemDto, with a related item, which we want to patch
        // note that this has a description field
        ItemDto itemDto = prepareItemDto();

        // create an ItemDto to represent the first ingest DTO. I still don't understand
        // exactly what this is used for inside the patcher
        ItemDto firstIngestDto = prepareItemDto();

        // moduify this so that the description field is null
        firstIngestDto.setDescription(null);

        // create a patch to update the description of the item
        ItemCore patch = new ItemCore();
        patch.setDescription("updatedDescription");

        // apply the patch to the item.
        ItemsPatcher.patchItemCore(itemDto, firstIngestDto, patch);

        // verify that the description remains unchanged
        Assertions.assertEquals("testDescription", patch.getDescription());
    }

    /**
     * This test is setup to patch the description field, where both the current item
     * and the first ingest version have none null but different values.
     * Whilst is may feel like the description should get updated to the value in the
     * patch, what actually happens is that the current value is retained
     * due to the weird logic in ItemsPatcher.determinePatchValue(), i.e. the value
     * in the match is completely ignnored.
     */
    @Test
    public void testPatchDifferentDescription() {
        // create a sample ItemDto, with a related item, which we want to patch
        // note that this has a description field
        ItemDto itemDto = prepareItemDto();

        // create an ItemDto to represent the first ingest DTO. I still don't understand
        // exactly what this is used for inside the patcher
        ItemDto firstIngestDto = prepareItemDto();

        // moduify this so that the description field is changed
        firstIngestDto.setDescription("firstDescription");

        // create a patch to update the description of the item
        ItemCore patch = new ItemCore();
        patch.setDescription("updatedDescription");

        // apply the patch to the item.
        ItemsPatcher.patchItemCore(itemDto, firstIngestDto, patch);

        // verify that the description remains unchanged
        Assertions.assertEquals("testDescription", patch.getDescription());
    }

    /**
     * Prepares an ItemDto object with test data for use in unit tests.
     * Note that not all fields are populated, only those relevant for the tests,
     * which ensures that the ItemsPatcher does not throw a NPE. A richer
     * test object would be beneficial for thorough testing, although that may
     * be better done during integration rather than unit testing.
     * @return An ItemDto object populated with test data.
     */
    private DigitalObjectDto prepareItemDto(){
        DigitalObjectDto item = new DigitalObjectDto();

        item.setDateCreated(ZonedDateTime.parse("2026-08-18T14:05:55+01:00"));

        item.setDescription("testDescription");
        item.setCategory(ItemCategory.STEP);
        item.setLabel("testLabel");
        item.setVersion("testVersion");
        item.setContributors(List.of());
        item.setAccessibleAt(List.of("accessibleAt"));
        item.setProperties(List.of());

        //externalId
        ItemExternalIdDto externalIdDto = new ItemExternalIdDto();
        externalIdDto.setIdentifier("identifier_1");
        externalIdDto.setIdentifierService(new ItemSourceDto("code", "label", 1, "urlTemplate"));
        item.setExternalIds(List.of(externalIdDto));

        //relatedItems
        ItemRelationDto itemRelationDto = new ItemRelationDto();
        itemRelationDto.setCode("relationCode");
        RelatedItemDto relatedItemDto = new RelatedItemDto();
        relatedItemDto.setId(12L);
        relatedItemDto.setPersistentId("persistentId");
        relatedItemDto.setRelation(itemRelationDto);
        item.setRelatedItems(List.of(relatedItemDto));

        ConceptBasicDto conceptBasicDto = new ConceptBasicDto();
        conceptBasicDto.setCode("conceptCode");
        conceptBasicDto.setLabel("label");
        conceptBasicDto.setDefinition("definition");
        conceptBasicDto.setNotation("notation");
        conceptBasicDto.setUri("uri");
        conceptBasicDto.setCandidate(true);
        
        VocabularyBasicDto vocabularyBasicDto = new VocabularyBasicDto();
        vocabularyBasicDto.setCode("vocabularyCode");
        conceptBasicDto.setVocabulary(vocabularyBasicDto);

        //media
        ItemMediaDto thumbnail = new ItemMediaDto(new MediaDetails(UUID.fromString("f9133f04-e3fc-40e1-85c6" +
                "-157f32621018"), MediaCategory.IMAGE, MediaLocation.builder().build(), "fileName", "mimeType", true)
                , "caption", conceptBasicDto);
        item.setMedia(List.of());
        item.setThumbnail(thumbnail);

        return item;
    }

    /**
     * Round-trip test for the fields declared on the base ItemDto/ItemCore types.
     *
     * The new patching algorithm relies on Jackson to map the DTO representation
     * of each field onto the (differently typed) Core representation. Because
     * unknown properties are ignored during that mapping, a renamed or reshaped
     * field would be silently dropped. This test catches that for the shared
     * base fields.
     *
     * Note: this deliberately covers only the base fields, which are shared by
     * all item subclasses. Subclass-specific fields are not covered here and
     * would need their own fixtures.
     */
    @Test
    public void testBaseItemFieldsRoundTrip() {

        ItemDto itemDto = prepareFullyPopulatedItemDto();

        // a patch that only changes the label; every other field is left
        // unspecified and should therefore be retained from the current item
        ItemCore patch = new ItemCore();
        patch.setLabel("updatedLabel");

        ItemsPatcher.patchItemCore(itemDto, null, patch);

        // the one field intentionally changed by the patch
        Assertions.assertEquals("updatedLabel", patch.getLabel());

        // simple fields retained from the current item
        Assertions.assertEquals("testDescription", patch.getDescription());
        Assertions.assertEquals("testVersion", patch.getVersion());

        // accessibleAt retained (list equality also checks size and order)
        Assertions.assertEquals(itemDto.getAccessibleAt(), patch.getAccessibleAt());

        // contributors: actor id and role code must survive the DTO -> Core mapping
        Assertions.assertEquals(1, patch.getContributors().size());
        Assertions.assertEquals(
            itemDto.getContributors().get(0).getActor().getId(),
            patch.getContributors().get(0).getActor().getId()
        );
        Assertions.assertEquals(
            itemDto.getContributors().get(0).getRole().getCode(),
            patch.getContributors().get(0).getRole().getCode()
        );

        // externalIds: identifier and identifier service code must survive
        Assertions.assertEquals(1, patch.getExternalIds().size());
        Assertions.assertEquals(
            itemDto.getExternalIds().get(0).getIdentifier(),
            patch.getExternalIds().get(0).getIdentifier()
        );
        Assertions.assertEquals(
            itemDto.getExternalIds().get(0).getIdentifierService().getCode(),
            patch.getExternalIds().get(0).getIdentifierService().getCode()
        );

        // properties: type, value and concept must survive.
        // first entry has a concept, second does not - both must survive
        Assertions.assertEquals(2, patch.getProperties().size());
        Assertions.assertEquals(
            itemDto.getProperties().get(0).getType().getCode(),
            patch.getProperties().get(0).getType().getCode()
        );
        Assertions.assertEquals(
            itemDto.getProperties().get(0).getValue(),
            patch.getProperties().get(0).getValue()
        );
        Assertions.assertEquals(
            itemDto.getProperties().get(0).getConcept().getCode(),
            patch.getProperties().get(0).getConcept().getCode()
        );
        Assertions.assertEquals(
            itemDto.getProperties().get(0).getConcept().getUri(),
            patch.getProperties().get(0).getConcept().getUri()
        );
        Assertions.assertEquals(
            itemDto.getProperties().get(0).getConcept().getVocabulary().getCode(),
            patch.getProperties().get(0).getConcept().getVocabulary().getCode()
        );
        Assertions.assertEquals(
            itemDto.getProperties().get(1).getValue(),
            patch.getProperties().get(1).getValue()
        );
        Assertions.assertNull(patch.getProperties().get(1).getConcept());

        // relatedItems: persistentId and relation code must survive
        Assertions.assertEquals(1, patch.getRelatedItems().size());
        Assertions.assertEquals(
            itemDto.getRelatedItems().get(0).getPersistentId(),
            patch.getRelatedItems().get(0).getPersistentId()
        );
        Assertions.assertEquals(
            itemDto.getRelatedItems().get(0).getRelation().getCode(),
            patch.getRelatedItems().get(0).getRelation().getCode()
        );

        // media: info id, caption and concept must survive.
        // first entry has a concept, second does not - both must survive
        Assertions.assertEquals(2, patch.getMedia().size());
        Assertions.assertEquals(
            itemDto.getMedia().get(0).getInfo().getMediaId(),
            patch.getMedia().get(0).getInfo().getMediaId()
        );
        Assertions.assertEquals(
            itemDto.getMedia().get(0).getCaption(),
            patch.getMedia().get(0).getCaption()
        );
        Assertions.assertEquals(
            itemDto.getMedia().get(0).getConcept().getCode(),
            patch.getMedia().get(0).getConcept().getCode()
        );
        Assertions.assertEquals(
            itemDto.getMedia().get(1).getInfo().getMediaId(),
            patch.getMedia().get(1).getInfo().getMediaId()
        );
        Assertions.assertEquals(
            itemDto.getMedia().get(1).getCaption(),
            patch.getMedia().get(1).getCaption()
        );
        Assertions.assertNull(patch.getMedia().get(1).getConcept());

        // thumbnail: info id, caption and concept must survive
        Assertions.assertEquals(
            itemDto.getThumbnail().getInfo().getMediaId(),
            patch.getThumbnail().getInfo().getMediaId()
        );
        Assertions.assertEquals(
            itemDto.getThumbnail().getCaption(),
            patch.getThumbnail().getCaption()
        );
        Assertions.assertEquals(
            itemDto.getThumbnail().getConcept().getCode(),
            patch.getThumbnail().getConcept().getCode()
        );
    }

    /**
     * Prepares a fully populated ItemDto in which every field that is
     * mapped onto the ItemCore representation carries a distinct value, so that
     * a mismatch between two similarly shaped fields cannot cancel out.
     * Note that only the fields consumed by ItemCore need distinct values;
     * DTO-only fields do not participate in the mapping.
     */
    private ItemDto prepareFullyPopulatedItemDto() {
        ItemDto item = new ItemDto();

        item.setLabel("testLabel");
        item.setDescription("testDescription");
        item.setVersion("testVersion");
        item.setCategory(ItemCategory.STEP);

        item.setAccessibleAt(List.of(
            "https://example.org/accessible-1",
            "https://example.org/accessible-2"
        ));

        //contributors
        ActorDto actorDto = new ActorDto();
        actorDto.setId(42L);

        ActorRoleDto roleDto = new ActorRoleDto();
        roleDto.setCode("contributorRoleCode");

        ItemContributorDto contributorDto = new ItemContributorDto();
        contributorDto.setActor(actorDto);
        contributorDto.setRole(roleDto);
        item.setContributors(List.of(contributorDto));

        //externalIds
        ItemExternalIdDto externalIdDto = new ItemExternalIdDto();
        externalIdDto.setIdentifier("identifier_1");
        externalIdDto.setIdentifierService(new ItemSourceDto("identifierServiceCode", "label", 1, "urlTemplate"));
        item.setExternalIds(List.of(externalIdDto));

        //properties - one with a concept, one without
        PropertyTypeDto propertyTypeDto = new PropertyTypeDto();
        propertyTypeDto.setCode("propertyTypeCode");

        ConceptBasicDto propertyConceptDto = new ConceptBasicDto();
        propertyConceptDto.setCode("propertyConceptCode");
        propertyConceptDto.setUri("https://example.org/concept/property");
        VocabularyBasicDto propertyVocabularyDto = new VocabularyBasicDto();
        propertyVocabularyDto.setCode("propertyVocabularyCode");
        propertyConceptDto.setVocabulary(propertyVocabularyDto);

        PropertyDto propertyWithConcept = new PropertyDto();
        propertyWithConcept.setType(propertyTypeDto);
        propertyWithConcept.setValue("propertyValue1");
        propertyWithConcept.setConcept(propertyConceptDto);

        PropertyDto propertyWithoutConcept = new PropertyDto();
        propertyWithoutConcept.setType(propertyTypeDto);
        propertyWithoutConcept.setValue("propertyValue2");

        item.setProperties(List.of(propertyWithConcept, propertyWithoutConcept));

        //relatedItems
        ItemRelationDto itemRelationDto = new ItemRelationDto();
        itemRelationDto.setCode("relationCode");

        RelatedItemDto relatedItemDto = new RelatedItemDto();
        relatedItemDto.setId(12L);
        relatedItemDto.setPersistentId("relatedPersistentId");
        relatedItemDto.setRelation(itemRelationDto);
        item.setRelatedItems(List.of(relatedItemDto));

        //media - one entry with a concept, one without
        ConceptBasicDto mediaConceptDto = new ConceptBasicDto();
        mediaConceptDto.setCode("mediaConceptCode");
        mediaConceptDto.setUri("https://example.org/concept/media");
        VocabularyBasicDto mediaVocabularyDto = new VocabularyBasicDto();
        mediaVocabularyDto.setCode("mediaVocabularyCode");
        mediaConceptDto.setVocabulary(mediaVocabularyDto);

        ItemMediaDto mediaWithConcept = new ItemMediaDto(
            new MediaDetails(UUID.fromString("f9133f04-e3fc-40e1-85c6-157f32621018"),
                MediaCategory.IMAGE, MediaLocation.builder().build(), "fileName1", "mimeType1", true),
            "mediaCaption1", mediaConceptDto);

        ItemMediaDto mediaWithoutConcept = new ItemMediaDto(
            new MediaDetails(UUID.fromString("0a4c8f52-6bd1-4a7e-9a3e-2f1d3b5c6a70"),
                MediaCategory.IMAGE, MediaLocation.builder().build(), "fileName2", "mimeType2", true),
            "mediaCaption2", null);

        item.setMedia(List.of(mediaWithConcept, mediaWithoutConcept));

        //thumbnail
        ConceptBasicDto thumbnailConceptDto = new ConceptBasicDto();
        thumbnailConceptDto.setCode("thumbnailConceptCode");
        thumbnailConceptDto.setUri("https://example.org/concept/thumbnail");
        VocabularyBasicDto thumbnailVocabularyDto = new VocabularyBasicDto();
        thumbnailVocabularyDto.setCode("thumbnailVocabularyCode");
        thumbnailConceptDto.setVocabulary(thumbnailVocabularyDto);

        ItemMediaDto thumbnail = new ItemMediaDto(
            new MediaDetails(UUID.fromString("c2f1a9e8-3b4d-4c5a-8d6e-7f0a1b2c3d4e"),
                MediaCategory.IMAGE, MediaLocation.builder().build(), "thumbnailFileName", "image/png", true),
            "thumbnailCaption", thumbnailConceptDto);
        item.setThumbnail(thumbnail);

        return item;
    }
}
