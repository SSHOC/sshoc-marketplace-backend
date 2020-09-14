package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemCategoryService {

    public Map<String, String> getAllItemCategories() {
        return Arrays.stream(ItemCategory.values()).collect(
                Collectors.toMap(c -> c.getValue(), c -> c.getLabel(),
                        (u, v) -> u,
                        LinkedHashMap::new
                )
        );
    }

}
