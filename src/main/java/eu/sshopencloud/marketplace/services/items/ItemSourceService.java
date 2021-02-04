package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemSourceCore;
import eu.sshopencloud.marketplace.dto.items.ItemSourceDto;
import eu.sshopencloud.marketplace.mappers.items.ItemSourceMapper;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import eu.sshopencloud.marketplace.repositories.items.ItemSourceRepository;
import eu.sshopencloud.marketplace.services.common.BaseOrderableEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class ItemSourceService extends BaseOrderableEntityService<ItemSource, String> {

    private final ItemSourceRepository itemSourceRepository;


    public List<ItemSourceDto> getAllItemSources() {
        return ItemSourceMapper.INSTANCE.toDto(loadAllEntries());
    }

    public ItemSourceDto getItemSource(String code) {
        ItemSource itemSource = loadItemSource(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item source with code %s not found", code)));

        return ItemSourceMapper.INSTANCE.toDto(itemSource);
    }

    public Optional<ItemSource> loadItemSource(String code) {
        return itemSourceRepository.findById(code);
    }

    public ItemSourceDto createItemSource(ItemSourceCore itemSourceCore) {
        ItemSource newItemSource = ItemSource.builder()
                .code(itemSourceCore.getCode())
                .label(itemSourceCore.getLabel())
                .build();

        if (newItemSource.getCode() == null)
            throw new IllegalArgumentException("Item's source's code is required.");

        placeEntryAtPosition(newItemSource, itemSourceCore.getOrd(), true);
        newItemSource = itemSourceRepository.save(newItemSource);

        return ItemSourceMapper.INSTANCE.toDto(newItemSource);
    }

    public ItemSourceDto updateItemSource(String code, ItemSourceCore itemSourceCore) {
        ItemSource itemSource = itemSourceRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item source with code %s not found", code)));

        itemSource.setLabel(itemSourceCore.getLabel());
        placeEntryAtPosition(itemSource, itemSourceCore.getOrd(), false);

        return ItemSourceMapper.INSTANCE.toDto(itemSource);
    }

    public void deleteItemSource(String code) {
        if (itemSourceRepository.isItemSourceInUse(code))
            throw new IllegalArgumentException(String.format("Item source %s is in use and it cannot be removed anymore", code));

        try {
            itemSourceRepository.deleteById(code);
            removeEntryFromPosition(code);
        }
        catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(String.format("Actor role with code %s not found", code));
        }
    }

    @Override
    protected JpaRepository<ItemSource, String> getEntityRepository() {
        return itemSourceRepository;
    }
}
