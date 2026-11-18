package eu.sshopencloud.marketplace.services.skg;

import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.mappers.skg.ItemToSkgMapper;
import eu.sshopencloud.marketplace.model.skg.JsonLdDocument;
import eu.sshopencloud.marketplace.services.items.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class SkgToolService {

    private final ToolService toolService;
    private final ItemToSkgMapper itemToSkgMapper;

    public SkgToolService(ToolService toolService, ItemToSkgMapper itemToSkgMapper) {
        this.toolService = toolService;
        this.itemToSkgMapper = itemToSkgMapper;
    }

    public JsonLdDocument getTool(String persistentId) {
        ToolDto latestTool = toolService.getLatestTool(persistentId, false, true, false);
        return itemToSkgMapper.toSkg(latestTool);
    }

}
