package eu.sshopencloud.marketplace.services.items;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import eu.sshopencloud.marketplace.dto.items.DigitalObjectCore;
import eu.sshopencloud.marketplace.dto.items.DigitalObjectDto;
import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.items.ItemExternalIdDto;
import eu.sshopencloud.marketplace.dto.items.ItemMediaDto;
import eu.sshopencloud.marketplace.dto.items.ItemRelationDto;
import eu.sshopencloud.marketplace.dto.items.ItemSourceDto;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptBasicDto;
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
     * My assumption is that the description should get updated to the value in the
     * patch. What actually happens though is that the current value is retained
     * due to the weird logic in ItemsPatcher.determinePatchValue(). This causes
     * the test to fail and hence it's currently annotated with @ExpectedToFail.
     * Note that the same failure would happen when updating the label or version
     * field as well given that both pass through ItemsPatcher.determinePatchValue()
     */
    @Test
    @ExpectedToFail
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
        patch.setLabel("updatedDescription");

        // apply the patch to the item.
        ItemsPatcher.patchItemCore(itemDto, firstIngestDto, patch);

        // verify that the label matches the patch value
        // Note that currently this will fail due to the odd logic
        // in ItemPatcher.determinePatchValue
        Assertions.assertEquals("updatedDescription", patch.getDescription());
    }

    /**
     * This test is setup to patch the description field, where both the current item
     * and the first ingest version have none null but different values.
     * My assumption is that the description should get updated to the value in the
     * patch. What actually happens though is that the current value is retained
     * due to the weird logic in ItemsPatcher.determinePatchValue(). This causes
     * the test to fail and hence it's currently annotated with @ExpectedToFail.
     * Note that the same failure would happen when updating the label or version
     * field as well given that both pass through ItemsPatcher.determinePatchValue()
     */
    @Test
    @ExpectedToFail
    public void testPatchDifferentDescription() {
        // create a sample ItemDto, with a related item, which we want to patch
        // note that this has a description field
        ItemDto itemDto = prepareItemDto();

        // create an ItemDto to represent the first ingest DTO. I still don't understand
        // exactly what this is used for inside the patcher
        ItemDto firstIngestDto = prepareItemDto();

        // moduify this so that the description field is null
        firstIngestDto.setDescription("firstDescription");

        // create a patch to update the description of the item
        ItemCore patch = new ItemCore();
        patch.setLabel("updatedDescription");

        // apply the patch to the item.
        ItemsPatcher.patchItemCore(itemDto, firstIngestDto, patch);

        // verify that the label matches the patch value
        // Note that currently this will fail due to the odd logic
        // in ItemPatcher.determinePatchValue
        Assertions.assertEquals("updatedDescription", patch.getDescription());
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
}
