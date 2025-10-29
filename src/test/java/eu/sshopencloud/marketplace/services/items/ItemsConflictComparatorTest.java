package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptBasicDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


class ItemsConflictComparatorTest {

    @Test
    void shouldRecognizeItemsAsNonConflicted() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertFalse(ItemsConflictComparator.isConflict(differences1, differences2));

    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfDescription() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setDescription("new Description");

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfDescription_2() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item2.setDescription("new Description");

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfDescription_3() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setDescription("new description");
        item2.setDescription("NEW DESCRIPTION");

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfContributors() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setContributors(Collections.emptyList());

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfContributors_1() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item2.setContributors(null);

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfContributors_2() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item2.setContributors(Arrays.asList(new ItemContributorDto(), new ItemContributorDto(),
                new ItemContributorDto()));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfProperties() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setProperties(null);

        //them
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }


    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfProperties_1() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setProperties(List.of(new PropertyDto(), new PropertyDto()));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }


    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfProperties_2() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        PropertyDto propertyDto = new PropertyDto();
        propertyDto.setValue("example value");
        item1.setProperties(List.of(propertyDto));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfExternalIds() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setExternalIds(null);

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));

    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfExternalIds_1() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        ItemExternalIdDto externalIdDto1 = new ItemExternalIdDto();
        externalIdDto1.setIdentifier("new_identifier");
        externalIdDto1.setIdentifierService(new ItemSourceDto("code", "label", 1, "urlTemplate"));
        item2.setExternalIds(List.of(externalIdDto1));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfExternalIds_2() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        ItemExternalIdDto externalIdDto2 = new ItemExternalIdDto();
        externalIdDto2.setIdentifier("new_identifier");
        externalIdDto2.setIdentifierService(new ItemSourceDto("code", "label", 1, "urlTemplate"));
        item2.setExternalIds(List.of(externalIdDto2));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfExternalIds_3() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        ItemExternalIdDto externalIdDto1 = new ItemExternalIdDto();
        externalIdDto1.setIdentifier("new_identifier_1");
        externalIdDto1.setIdentifierService(new ItemSourceDto("code_1", "label", 1, "urlTemplate"));
        item2.setExternalIds(List.of(externalIdDto1));

        ItemExternalIdDto externalIdDto2 = new ItemExternalIdDto();
        externalIdDto2.setIdentifier("new_identifier_1");
        externalIdDto2.setIdentifierService(new ItemSourceDto("code_2", "label", 1, "urlTemplate"));
        item2.setExternalIds(List.of(externalIdDto2));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfAccessibleAt() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setAccessibleAt(Collections.emptyList());

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfAccessibleAt_1() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setAccessibleAt(List.of("accessible_at_1"));
        item2.setAccessibleAt(List.of("accessible_at_2"));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfRelatedItems() {

        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setRelatedItems(null);

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfMedia() {
        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        item1.setMedia(List.of(new ItemMediaDto()));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfMedia_1() {
        //given
        ItemDto item1 = prepareItemTemplate();
        ItemDto item2 = prepareItemTemplate();

        //when
        ItemMediaDto itemMediaDto = new ItemMediaDto(new MediaDetails(UUID.randomUUID(), MediaCategory.IMAGE,
                MediaLocation.builder().build(), "fileName", "mimeType", true), "caption", new ConceptBasicDto());
        item1.setMedia(List.of(itemMediaDto));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfDigitalObjectDates() {
        //given
        DigitalObjectDto item1 = prepareItemTemplate();
        DigitalObjectDto item2 = prepareItemTemplate();

        //when
        item1.setDateCreated(ZonedDateTime.of(LocalDate.of(2022, 1, 10), LocalTime.of(1, 1), ZoneId.systemDefault()));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }

    @Test
    void shouldRecognizeItemsAsConflictedBecauseOfDigitalObjectDates_1() {
        //given
        DigitalObjectDto item1 = prepareItemTemplate();
        DigitalObjectDto item2 = prepareItemTemplate();

        //when
        item1.setDateLastUpdated(ZonedDateTime.of(LocalDate.of(2022, 1, 10), LocalTime.of(1, 1),
                ZoneId.systemDefault()));

        //then
        ItemDifferencesCore<ItemCore, ItemDto> differences1 = new ItemDifferencesCore<>();
        ItemDifferencesCore<ItemCore, ItemDto> differences2 = new ItemDifferencesCore<>();
        differences1.setOther(item1);
        differences2.setOther(item2);

        Assertions.assertTrue(ItemsConflictComparator.isConflict(differences1, differences2));
    }


    private DigitalObjectDto prepareItemTemplate() {
        DigitalObjectDto item = new DigitalObjectDto();

        item.setDescription("testDescription");
        item.setCategory(ItemCategory.STEP);
        item.setLabel("testLabel");
        item.setVersion("testVersion");
        item.setContributors(List.of(new ItemContributorDto()));
        item.setAccessibleAt(List.of("accessibleAt"));
        item.setProperties(List.of(new PropertyDto()));

        //externalId
        ItemExternalIdDto externalIdDto = new ItemExternalIdDto();
        externalIdDto.setIdentifier("identifier_1");
        externalIdDto.setIdentifierService(new ItemSourceDto("code", "label", 1, "urlTemplate"));
        item.setExternalIds(List.of(externalIdDto));

        //accessibleAt
        item.setAccessibleAt(List.of("accessibleAt"));

        //relatedItems
        RelatedItemDto relatedItemDto = new RelatedItemDto();
        relatedItemDto.setId(12L);
        item.setRelatedItems(List.of(relatedItemDto));

        //media
        ItemMediaDto itemMediaDto = new ItemMediaDto(new MediaDetails(UUID.fromString("f9133f04-e3fc-40e1-85c6" +
                "-157f32621018"), MediaCategory.IMAGE, MediaLocation.builder().build(), "fileName", "mimeType", true)
                , "caption", new ConceptBasicDto());
        item.setMedia(List.of(itemMediaDto));

        //digitalObjectDate
        item.setDateCreated(ZonedDateTime.of(LocalDate.of(2020, 1, 10), LocalTime.of(1, 1), ZoneId.systemDefault()));
        item.setDateLastUpdated(ZonedDateTime.of(LocalDate.of(2020, 1, 10), LocalTime.of(1, 1),
                ZoneId.systemDefault()));

        return item;
    }

}